package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.servicesecurity.MainPageLinksSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.MainPageLinkRequestDTO;
import com.apteka.portal.dtos.request.MainPageLinkUpdateRequestDTO;
import com.apteka.portal.dtos.response.MainPageLinkResponseDTO;
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
    private final MainPageLinkRepository mainPageLinkRepository;
    private final MainPageLinksSecurityService mainPageLinksSecurityService;
    private final TypeNameValidator typeNameValidator;
    private final GroupMainPageLinksRepository groupMainPageLinksRepository;
    private final CacheManager cacheManager;

    private final SseController sseController;

    @Cacheable(value = CacheNames.MAIN_PAGE_LINKS_BY_GROUP, key = "#groupId")
    @Transactional(readOnly = true)
    public List<MainPageLinkResponseDTO> getByGroup(Integer groupId) {
        return mainPageLinkRepository.findByGroupMainPageLinksId(groupId)
                .stream().map(MainPageLinkResponseDTO::from).toList();
    }

    @Cacheable(value = CacheNames.MAIN_PAGE_LINKS, key = "#id")
    @Transactional(readOnly = true)
    public MainPageLinkResponseDTO getOne(Integer id) {
        return MainPageLinkResponseDTO.from(mainPageLinkRepository.findById(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка на главной странице не найдена!")));
    }

    @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#result.groupMainPageLinksResponseDTO().id()")
    @Transactional
    public MainPageLinkResponseDTO create(MainPageLinkRequestDTO dto, AppUserDetails currentUser) {
        mainPageLinksSecurityService.validateCanCreateAndUpdate(currentUser);
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
        validateLink(cleanLink);

        MainPageLink savedLink = mainPageLinkRepository
                .save(MainPageLink.builder().name(cleanName).link(cleanLink).groupMainPageLinks(group).build());

        var signal = new SseEventNames.MainPageLinkSignalDTO(savedLink.getGroupMainPageLinks().getId(), SseSignalTypes.CREATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
        return MainPageLinkResponseDTO.from(savedLink);
    }

    @Transactional
    public MainPageLinkResponseDTO update(Integer id, MainPageLinkUpdateRequestDTO dto, AppUserDetails currentUser) {
        mainPageLinksSecurityService.validateCanCreateAndUpdate(currentUser);
        MainPageLink mainPageLink = mainPageLinkRepository.findByid(id)
                .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка на главной странице не найдена!"));

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
                validateLink(cleanLink);
                mainPageLink.setLink(cleanLink);
                hasChange = true;
            }
        }

        if (dto.groupMainPageLinkId() != null && dto.groupMainPageLinkId() > 0) {
            GroupMainPageLinks group = groupMainPageLinksRepository.findById(dto.groupMainPageLinkId())
                    .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена!"));
            
            cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(mainPageLink.getGroupMainPageLinks().getId());
            mainPageLink.setGroupMainPageLinks(group);
            hasChange = true;
        }

        MainPageLinkResponseDTO response = MainPageLinkResponseDTO.from(mainPageLink);

        if (hasChange) {
            var cache = cacheManager.getCache(CacheNames.MAIN_PAGE_LINKS);
            if (cache != null) {
                cache.put(id, response);
            }
            cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(response.groupMainPageLinksResponseDTO().id());
        }

        var signal = new SseEventNames.EntityUpdateSignalDTO(mainPageLink.getId(), SseSignalTypes.UPDATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
        return response;
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        mainPageLinksSecurityService.validateCanCreateAndUpdate(currentUser);
        MainPageLink mainPageLink = mainPageLinkRepository.findById(id)
            .orElseThrow(() -> new MainPageLinkNotFoundException("Ссылка не найдена"));

        mainPageLinkRepository.delete(mainPageLink);

        cacheManager.getCache(CacheNames.WORK_TYPE).evict(id);
        cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(mainPageLink.getGroupMainPageLinks().getId());

        var signal = new SseEventNames.MainPageLinkSignalDTO(mainPageLink.getGroupMainPageLinks().getId(), SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_MAIN_PAGE_LINKS, signal);
    }

    private void validateName(String name) {
        if (name.length() > 50) {
            throw new InvalidMainPageLinkNameException("Наименование ссылки не должно превышать 50 символов!");
        }
        if (mainPageLinkRepository.existsByName(name)) {
            throw new MainPageLinkAlreadyExistsException("Ссылка с таким именем уже существует!");
        }
    }

    private void validateLink(String link) {
        if (link.length() > 1000) {
            throw new InvalidMainPageLinkNameException("Ссылка не может содержать более 1000 символов!");
        }
    }
}
