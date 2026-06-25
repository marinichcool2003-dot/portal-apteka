package com.apteka.portal.services;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.apteka.portal.components.servicesecurity.NewsSecurityService;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.NewsRequestDTO;
import com.apteka.portal.dtos.request.NewsUpdateRequestDTO;
import com.apteka.portal.dtos.response.NewsResponseDTO;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidNewsTextException;
import com.apteka.portal.exceptions.InvalidNewsTitleException;
import com.apteka.portal.exceptions.NewsNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.News;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.NewsRepository;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;
    private final ClientRepository clientRepository;
    private final NewsSecurityService newsSecurityService;
    private final UserGroupRepository userGroupRepository;

    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<NewsResponseDTO> getByUserGroup(Integer userGroupId) {
        return newsRepository.findByUserGroupId(userGroupId).stream().map(NewsResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public NewsResponseDTO getOne(Integer id) {
        return newsRepository.findById(id).map(NewsResponseDTO::from)
                .orElseThrow(() -> new NewsNotFoundException("Новость не найдена"));
    }

    @Transactional
    public NewsResponseDTO create(NewsRequestDTO dto, AppUserDetails currentUser) {
        validateTitle(dto.title());
        validateNewsText(dto.newsText());
        newsSecurityService.validateCanCreateNews(currentUser, dto);
        Client client = clientRepository.findById(currentUser.getInternalId())
                .orElseThrow(() -> new ClientNotFoundException(currentUser.getInternalId()));
        UserGroup userGroup = userGroupRepository.findById(dto.userGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.userGroupId()));
        News news = News.builder()
                .title(dto.title())
                .newsText(dto.newsText())
                .author(client)
                .userGroup(userGroup)
                .creationDate(Instant.now())
                .build();
        News savedNews = newsRepository.save(news);

        Integer groupId = userGroup.getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.NewsSignalDTO(groupId,
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_NEWS, signal);
                }
            });
        }
        return NewsResponseDTO.from(savedNews);
    }

    @Transactional
    public NewsResponseDTO update(Integer id, NewsUpdateRequestDTO dto, AppUserDetails currentUser) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException("Новость не найдена"));

        boolean hasChange = false;

        newsSecurityService.validateCanUpdate(currentUser, news);

        if (dto.title() != null && !Objects.equals(news.getTitle(), dto.title())) {
            validateTitle(dto.title());
            news.setTitle(dto.title());
            hasChange = true;
        }
        if (dto.newsText() != null && !Objects.equals(news.getNewsText(), dto.newsText())) {
            validateNewsText(dto.newsText());
            news.setNewsText(dto.newsText());
            hasChange = true;
        }

        if (hasChange) {
            news.setUpdatedAt(Instant.now());
            news.setUpdatedBy(currentUser.getDisplayName());
            Integer newsId = news.getId();

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var signal = new SseEventNames.EntityUpdateSignalDTO(newsId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_NEWS, signal);
                    }
                });
            }
        }

        return NewsResponseDTO.from(news);
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        News news = newsRepository.findById(id)
                .orElseThrow((() -> new NewsNotFoundException("Новость не найдена")));
        newsSecurityService.validateCanDelete(currentUser, news);
        newsRepository.delete(news);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.NewsSignalDTO(news.getUserGroup().getId(), SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_NEWS, signal);
                }
            });
        }
    }

    private void validateTitle(String title) {
        if (title.isBlank() || title == null) {
            throw new InvalidNewsTitleException("Заголовок новости не может быть пустым");
        }
        if (title.length() < 3 || title.length() > 50) {
            throw new InvalidNewsTitleException("Заголовок должен содержать от 3 до 50 символов");
        }
    }

    private void validateNewsText(String newsText) {
        if (newsText.isBlank() || newsText == null) {
            throw new InvalidNewsTextException("Текст новости не может быть пустым");
        }

        if (newsText.length() < 10 || newsText.length() > 2000) {
            throw new InvalidNewsTextException("Текст новости должен содержать от 3 до 2000 символов");
        }
    }
}
