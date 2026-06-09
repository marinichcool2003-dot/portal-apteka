package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.NewsSecurityService;
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
        Client client = clientRepository.findById(currentUser.getClientId())
                .orElseThrow(() -> new ClientNotFoundException(currentUser.getClientId()));
        UserGroup userGroup = userGroupRepository.findById(dto.userGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.userGroupId()));
        News news = News.builder()
                .title(dto.title())
                .newsText(dto.newsText())
                .author(client)
                .userGroup(userGroup)
                .creationDate(LocalDateTime.now())
                .build();
        News savedNews = newsRepository.save(news);
        return NewsResponseDTO.from(savedNews);
    }

    @Transactional
    public NewsResponseDTO update(Integer id, NewsUpdateRequestDTO dto, AppUserDetails currentUser) {
        News news = newsRepository.findById(id)
                .orElseThrow((() -> new NewsNotFoundException("Новость не найдена")));

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
            news.setUpdatedDate(LocalDateTime.now());
            news.setLastModifiedBy(currentUser.getDisplayName());
        }

        return NewsResponseDTO.from(news);
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        News news = newsRepository.findById(id)
                .orElseThrow((() -> new NewsNotFoundException("Новость не найдена")));
        newsSecurityService.validateCanUpdate(currentUser, news);
        newsRepository.delete(news);
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
