package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.servicesecurity.MainPageLinksSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.GroupMainPageLinksRequestDTO;
import com.apteka.portal.dtos.response.GroupMainPageLinksResponseDTO;
import com.apteka.portal.exceptions.GroupMainPageLinksAlreadyExistsException;
import com.apteka.portal.exceptions.GroupMainPageLinksNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupMainPageLinksDescriptionException;
import com.apteka.portal.exceptions.InvalidGroupMainPageLinksNameException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
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
    private final CacheManager cacheManager;

    private final SseController sseController;

    @Cacheable(value = CacheNames.GROUPS_MAIN_PAGE_LINKS)
    @Transactional(readOnly = true)
    public List<GroupMainPageLinksResponseDTO> getAll() {
        return groupMainPageLinksRepository.findAll()
                .stream().map(GroupMainPageLinksResponseDTO::from).toList();
    }

    @Cacheable(value = CacheNames.GROUPS_MAIN_PAGE_LINKS, key = "#id")
    @Transactional(readOnly = true)
    public GroupMainPageLinksResponseDTO getOne(Integer id) {
        return GroupMainPageLinksResponseDTO.from(groupMainPageLinksRepository.findById(id)
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена")));
    }

    @Caching(put = @CachePut(value = CacheNames.GROUPS_MAIN_PAGE_LINKS, key = "#result.id"), evict = @CacheEvict(value = CacheNames.GROUPS_MAIN_PAGE_LINKS, allEntries = true))
    @Transactional
    public GroupMainPageLinksResponseDTO create(GroupMainPageLinksRequestDTO dto, AppUserDetails currentUser) {
        groupMainPageLinksSecurityService.validateCanCreateAndUpdate(currentUser);
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

        GroupMainPageLinks saved = groupMainPageLinksRepository.save(savedBuilder.build());
        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_MAIN_PAGE_LINKS, SseSignalTypes.CREATED);

        return GroupMainPageLinksResponseDTO.from(saved);
    }

    @Transactional
    public GroupMainPageLinksResponseDTO update(Integer id, GroupMainPageLinksRequestDTO dto,
            AppUserDetails currentUser) {
        groupMainPageLinksSecurityService.validateCanCreateAndUpdate(currentUser);
        GroupMainPageLinks groupMainPageLinks = groupMainPageLinksRepository.findById(id)
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена"));

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

        GroupMainPageLinksResponseDTO response = GroupMainPageLinksResponseDTO.from(groupMainPageLinks);

        if (hasChange) {
            var cache = cacheManager.getCache(CacheNames.GROUPS_MAIN_PAGE_LINKS);
            if (cache != null) {
                cache.put(id, response);
            }
            var signal = new SseEventNames.EntityUpdateSignalDTO(id, SseSignalTypes.UPDATED);
            sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_MAIN_PAGE_LINKS, signal);
        }

        return response;
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.GROUPS_MAIN_PAGE_LINKS, key = "#id"),
            @CacheEvict(value = CacheNames.GROUPS_MAIN_PAGE_LINKS, allEntries = true)
    })
    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        groupMainPageLinksSecurityService.validateCanDelete(currentUser);
        if (!groupMainPageLinksRepository.existsById(id)) {
            throw new GroupMainPageLinksNotFoundException("Группа ссылок не найдена");
        }
        groupMainPageLinksRepository.deleteById(id);
        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_MAIN_PAGE_LINKS, SseSignalTypes.DELETED);
    }

    private void validateName(String name) {
        if (name.length() > 50) {
            throw new InvalidGroupMainPageLinksNameException(
                    "Наименование группы ссылок не может быть больше 50 символов!");
        }

        if (name.isBlank()) {
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
