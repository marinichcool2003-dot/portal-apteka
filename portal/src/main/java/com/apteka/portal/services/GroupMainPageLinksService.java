package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.servicesecurity.MainPageLinksSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.mainpagelinks.GroupMainPageLinkUpdateRequestDTO;
import com.apteka.portal.dtos.request.mainpagelinks.GroupMainPageLinksRequestDTO;
import com.apteka.portal.dtos.response.mainpagelink.GroupMainPageLinksResponseDTO;
import com.apteka.portal.exceptions.GroupMainPageLinksAlreadyExistsException;
import com.apteka.portal.exceptions.GroupMainPageLinksNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupMainPageLinksDescriptionException;
import com.apteka.portal.exceptions.InvalidGroupMainPageLinksNameException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupMainPageLinks;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.repository.GroupMainPageLinksRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMainPageLinksService {
    private final GroupMainPageLinksRepository groupMainPageLinksRepository;
    private final TypeNameValidator typeNameValidator;
    private final MainPageLinksSecurityService groupMainPageLinksSecurityService;

    private final SseController sseController;

    @Transactional
    public List<GroupMainPageLinksResponseDTO> getAll(Boolean isActive, AppUserDetails currentUser) {
        if (Boolean.FALSE.equals(isActive)) {
            groupMainPageLinksSecurityService.validateCanSelectNonActive(currentUser);
        }
        return groupMainPageLinksRepository.findByIsActive(isActive).stream()
                .map(GroupMainPageLinksResponseDTO::from).toList();
    }

    @Transactional
    public GroupMainPageLinksResponseDTO create(GroupMainPageLinksRequestDTO dto) {
        if (!StringUtils.hasText(dto.name())) {
            throw new InvalidGroupMainPageLinksNameException("Наименование группы ссылок не может быть пустым!");
        }

        String cleanName = typeNameValidator.getCleanName(dto.name());
        validateName(cleanName);
        GroupMainPageLinks.GroupMainPageLinksBuilder savedBuilder = GroupMainPageLinks.builder().name(cleanName);

        if (StringUtils.hasText(dto.description())) {
            String cleanDescription = typeNameValidator.getCleanName(dto.description());
            validateDescription(cleanDescription);
            savedBuilder.description(cleanDescription);
        }

        GroupMainPageLinks saved = groupMainPageLinksRepository.save(savedBuilder.isActive(true).build());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS,
                            SseSignalTypes.CREATED);
                }
            });
        }

        return GroupMainPageLinksResponseDTO.from(saved);
    }

    @Transactional
    public GroupMainPageLinksResponseDTO update(Integer id, GroupMainPageLinkUpdateRequestDTO dto,
            AppUserDetails currentUser) {
        GroupMainPageLinks groupMainPageLinks = groupMainPageLinksRepository.findById(id)
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена"));

        if (!groupMainPageLinks.isActive()) {
            groupMainPageLinksSecurityService.validateCanSelectNonActive(currentUser);
        }

        boolean hasChange = false;

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(groupMainPageLinks.getName(), cleanName)) {
                validateName(cleanName);
                groupMainPageLinks.setName(cleanName);
                hasChange = true;
            }
        }

        if (dto.description() != null) {
            String cleanDescription = typeNameValidator.getCleanName(dto.description());
            if (!Objects.equals(groupMainPageLinks.getDescription(), cleanDescription)) {
                validateDescription(cleanDescription);
                groupMainPageLinks.setDescription(cleanDescription);
                hasChange = true;
            }
        }

        if (hasChange) {
            groupMainPageLinks.setUpdatedBy(currentUser.getDisplayName());
        }

        GroupMainPageLinksResponseDTO response = GroupMainPageLinksResponseDTO.from(groupMainPageLinks);

        if (hasChange) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS,
                                SseSignalTypes.UPDATED);
                    }
                });
            }
        }

        return response;
    }

    @Transactional
    public void safeDelete(Integer id) {
        GroupMainPageLinks group = groupMainPageLinksRepository.findById(id)
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа не найдена!"));

        group.setActive(false);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS,
                            SseSignalTypes.UPDATED);
                }
            });
        }
    }

    @Transactional
    public void restore(Integer id) {
        GroupMainPageLinks group = groupMainPageLinksRepository.findById(id)
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа не найдена!"));
        group.setActive(true);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS,
                            SseSignalTypes.UPDATED);
                }
            });
        }

    }

    @Transactional
    public void permanentDelete(Integer id) {
        if (!groupMainPageLinksRepository.existsById(id)) {
            throw new GroupMainPageLinksNotFoundException("Группа ссылок не найдена");
        }
        groupMainPageLinksRepository.deleteById(id);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS,
                            SseSignalTypes.DELETED);
                }
            });
        }
    }

    private void validateName(String name) {
        if (name.length() > 50) {
            throw new InvalidGroupMainPageLinksNameException(
                    "Наименование группы ссылок не может быть больше 50 символов!");
        }

        if (!StringUtils.hasText(name)) {
            throw new InvalidGroupMainPageLinksNameException("Наименование группы ссылок не может быть пустым!");
        }

        if (groupMainPageLinksRepository.existsByName(name)) {
            throw new GroupMainPageLinksAlreadyExistsException("Группа " + name + " уже существует!");
        }
    }

    private void validateDescription(String description) {
        if (description.length() > 100) {
            throw new InvalidGroupMainPageLinksDescriptionException(
                    "Описание группы ссылок не может быть больше 100 символов!");
        }
        if (description.isEmpty()) {
            throw new InvalidGroupMainPageLinksDescriptionException(
                    "Описание группы ссылок не может быть пустым, но может быть не указано!");
        }
    }
}
