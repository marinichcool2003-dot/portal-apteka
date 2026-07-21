package com.apteka.portal.services;

import com.apteka.portal.components.validators.IsActiveValidator;
import java.util.List;
import java.util.Objects;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.servicesecurity.MainPageLinksSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.mainpagelinks.MainPageLinkRequestDTO;
import com.apteka.portal.dtos.request.mainpagelinks.MainPageLinkUpdateRequestDTO;
import com.apteka.portal.dtos.response.mainpagelink.MainPageLinkResponseDTO;
import com.apteka.portal.exceptions.GroupMainPageLinksNotFoundException;
import com.apteka.portal.exceptions.InvalidMainPageLinkException;
import com.apteka.portal.exceptions.InvalidMainPageLinkNameException;
import com.apteka.portal.exceptions.MainPageLinkAlreadyExistsException;
import com.apteka.portal.exceptions.MainPageLinkNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupMainPageLinks;
import com.apteka.portal.models.MainPageLink;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.repository.GroupMainPageLinksRepository;
import com.apteka.portal.repository.MainPageLinkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainPageLinksService {
    private final IsActiveValidator isActiveValidator;
    private final MainPageLinkRepository mainPageLinkRepository;
    private final MainPageLinksSecurityService mainPageLinksSecurityService;
    private final TypeNameValidator typeNameValidator;
    private final GroupMainPageLinksRepository groupMainPageLinksRepository;
    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<MainPageLinkResponseDTO> getByGroup(Integer groupId, Boolean isActive, AppUserDetails currentUser) {
        if (Boolean.FALSE.equals(isActive)) {
            mainPageLinksSecurityService.validateCanSelectNonActive(currentUser);
        }
        return mainPageLinkRepository.findByGroupMainPageLinksIdAndIsActive(groupId, isActive)
                .stream().map(MainPageLinkResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public MainPageLinkResponseDTO getOne(Integer id, AppUserDetails currentUser) {
        MainPageLink mainPageLink = mainPageLinkRepository.findById(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка не найдена!"));
        if (!isActiveValidator.isMainPageLinkActive(mainPageLink)) {
            mainPageLinksSecurityService.validateCanSelectNonActive(currentUser);
        }
        return MainPageLinkResponseDTO.from(mainPageLink);
    }

    @Cacheable(value = CacheNames.MAIN_PAGE_LINKS, key = "'active_only'", condition = "isActive == true", sync = true)
    @Transactional(readOnly = true)
    public List<MainPageLinkResponseDTO> getAll() {
        return mainPageLinkRepository.findAll().stream().map(MainPageLinkResponseDTO::from).toList();
    }

    @CacheEvict(value = CacheNames.MAIN_PAGE_LINKS, allEntries = true)
    @Transactional
    public MainPageLinkResponseDTO create(MainPageLinkRequestDTO dto) {
        if (!StringUtils.hasText(dto.name())) {
            throw new InvalidMainPageLinkNameException("Наименование ссылки не может быть пустым!");
        }
        if (!StringUtils.hasText(dto.link())) {
            throw new InvalidMainPageLinkException("Ссылка не может быть пустой!");
        }
        GroupMainPageLinks group = groupMainPageLinksRepository.findById(dto.groupMainPageLinkId())
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена!"));
        String cleanName = typeNameValidator.getCleanName(dto.name());
        String cleanLink = dto.link().replaceAll("\\s", "");
        validateName(cleanName);

        MainPageLink savedLink = mainPageLinkRepository
                .save(MainPageLink.builder().name(cleanName).link(cleanLink).groupMainPageLinks(group).isActive(true).build());
        Integer groupmainPageLinkId = savedLink.getGroupMainPageLinks().getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.MainPageLinkSignalDTO(groupmainPageLinkId,
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
                }
            });
        }
        return MainPageLinkResponseDTO.from(savedLink);
    }

    @CacheEvict(value = CacheNames.MAIN_PAGE_LINKS, allEntries = true)
    @Transactional
    public MainPageLinkResponseDTO update(Integer id, MainPageLinkUpdateRequestDTO dto, AppUserDetails currentUser) {
        MainPageLink mainPageLink = mainPageLinkRepository.findById(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка на главной странице не найдена!"));

        if (!isActiveValidator.isMainPageLinkActive(mainPageLink)) {
            mainPageLinksSecurityService.validateCanSelectNonActive(currentUser);
        }

        boolean hasChange = false;

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, mainPageLink.getName())) {
                validateName(cleanName);
                mainPageLink.setName(cleanName);
                hasChange = true;
            }
        }

        if (dto.link() != null) {
            String cleanLink = dto.link().replaceAll("\\s", "");
            if (!Objects.equals(cleanLink, mainPageLink.getLink())) {
                mainPageLink.setLink(cleanLink);
                hasChange = true;
            }
        }

        if (dto.groupMainPageLinkId() != null && dto.groupMainPageLinkId() > 0) {
            GroupMainPageLinks group = groupMainPageLinksRepository.findById(dto.groupMainPageLinkId())
                    .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена!"));

            mainPageLink.setGroupMainPageLinks(group);
            hasChange = true;
        }

        if (hasChange) {
            mainPageLink.setUpdatedBy(currentUser.getDisplayName());
        }

        MainPageLinkResponseDTO response = MainPageLinkResponseDTO.from(mainPageLink);
        Integer mainPageLinkId = mainPageLink.getId();

        if (hasChange) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var signal = new SseEventNames.EntityUpdateSignalDTO(mainPageLinkId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
                    }
                });
            }
        }
        return response;
    }

    @CacheEvict(value = CacheNames.MAIN_PAGE_LINKS, allEntries = true)
    @Transactional
    public void safeDelete(Integer id) {
        MainPageLink deletedLink = mainPageLinkRepository.findById(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка на главной странице не найдена!"));

        if (isActiveValidator.isMainPageLinkActive(deletedLink)) {
            deletedLink.setActive(false);
            Integer mainPageLinkId = deletedLink.getId();

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var signal = new SseEventNames.EntityUpdateSignalDTO(mainPageLinkId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
                    }
                });
            }
        }
    }

    @CacheEvict(value = CacheNames.MAIN_PAGE_LINKS, allEntries = true)
    @Transactional
    public void restore(Integer id) {
        MainPageLink restoredLink = mainPageLinkRepository.findById(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка на главной странице не найдена!"));

        if (!isActiveValidator.isMainPageLinkActive(restoredLink)) {
            restoredLink.setActive(true);
            Integer mainPageLinkId = restoredLink.getId();

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var signal = new SseEventNames.EntityUpdateSignalDTO(mainPageLinkId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
                    }
                });
            }
        }
    }

    @CacheEvict(value = CacheNames.MAIN_PAGE_LINKS, allEntries = true)
    @Transactional
    public void delete(Integer id) {
        MainPageLink mainPageLink = mainPageLinkRepository.findById(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка не найдена"));

        mainPageLinkRepository.delete(mainPageLink);

        Integer groupMainPageLinkId = mainPageLink.getGroupMainPageLinks().getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.MainPageLinkSignalDTO(groupMainPageLinkId,
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
                }
            });
        }
    }

    private void validateName(String name) {
        if (mainPageLinkRepository.existsByName(name)) {
            throw new MainPageLinkAlreadyExistsException("Ссылка с таким именем уже существует!");
        }
    }
}
