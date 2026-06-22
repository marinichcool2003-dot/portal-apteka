package com.apteka.portal.services;

import com.apteka.portal.components.servicesecurity.UserGroupSecurityService;
import com.apteka.portal.components.validators.PhoneNumberValidator;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.UserGroupRequestDTO;
import com.apteka.portal.dtos.response.UserGroupResponseDTO;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.DublicateGroupUserException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupUserException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupSecurityService userGroupSecurityService;
    private final UserGroupRepository userGroupRepository;
    private final TypeNameValidator typeNameValidator;
    private final PhoneNumberValidator phoneNumberValidator;
    private final CacheManager cacheManager;
    private final SseController sseController;

    @Cacheable(value = CacheNames.USER_GROUPS_LIST, sync = true)
    @Transactional(readOnly = true)
    public List<UserGroupResponseDTO> getAll() {
        return userGroupRepository.findAll().stream()
                .map(UserGroupResponseDTO::from)
                .toList();
    }

    @Cacheable(value = CacheNames.USER_GROUP, key = "#id", sync = true)
    @Transactional(readOnly = true)
    public UserGroupResponseDTO getOne(Integer id) {
        UserGroup group = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));
        return UserGroupResponseDTO.from(group);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true)
    })
    @Transactional
    public UserGroupResponseDTO create(UserGroupRequestDTO dto, AppUserDetails currentUser) {
        userGroupSecurityService.checkCanCreateGroup(currentUser);
        if (!StringUtils.hasText(dto.name()))
            throw new InvalidGroupUserException("Группа пользователя не может быть пустой!");
        String cleanName = typeNameValidator.getCleanName(dto.name());
        validateNameGroup(cleanName, null);
        UserGroup.UserGroupBuilder savedGroupBuilder = UserGroup.builder().name(cleanName);

        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            savedGroupBuilder.phoneNumber(cleanPhoneNumber);
        }

        if (StringUtils.hasText(dto.internalNumber())) {
            String cleanInternalnumber = phoneNumberValidator.getCleanInternalNumber(dto.internalNumber());
            savedGroupBuilder.internalNumber(cleanInternalnumber);
        }

        if (StringUtils.hasText(dto.extensionNumber())) {
            String cleanExtensionNumber = phoneNumberValidator.getCleanExtensionNumber(dto.extensionNumber());
            savedGroupBuilder.extensionNumber(cleanExtensionNumber);
        }

        UserGroup saved = savedGroupBuilder.build();
        userGroupRepository.save(saved);

        sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, SseSignalTypes.CREATED);

        return UserGroupResponseDTO.from(saved);
    }

    @Transactional
    public UserGroupResponseDTO update(Integer id, UserGroupRequestDTO dto, AppUserDetails currentUser) {
        userGroupSecurityService.checkCanCreateGroup(currentUser);

        UserGroup upGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));

        boolean hasChange = false;

        if (StringUtils.hasText(dto.name())) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upGroup.getName())) {
                validateNameGroup(cleanName, id);
                upGroup.setName(cleanName);
                hasChange = true;
            }
        }        
        
        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            upGroup.setPhoneNumber(cleanPhoneNumber);
            hasChange = true;
        }

        if (StringUtils.hasText(dto.internalNumber())) {
           String cleanInternalnumber = phoneNumberValidator.getCleanInternalNumber(dto.internalNumber()); 
           upGroup.setInternalNumber(cleanInternalnumber);
           hasChange = true; 
        }

        if (StringUtils.hasText(dto.extensionNumber())) {
            String cleanExtensionNumber = phoneNumberValidator.getCleanInternalNumber(dto.internalNumber());
            upGroup.setExtensionNumber(cleanExtensionNumber);
            hasChange = true;
        }

        UserGroupResponseDTO response = UserGroupResponseDTO.from(upGroup);

        if (hasChange) {
            var cache = cacheManager.getCache(CacheNames.USER_GROUP);
            if (cache != null) {
                cache.put(id, response);
            }
            var signal = new SseEventNames.EntityUpdateSignalDTO(id, SseSignalTypes.UPDATED);
            sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, signal);
        }

        return response;
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUP, key = "#id"),
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#id"),
            @CacheEvict(value = CacheNames.GROUP_TASK, allEntries = true),
            @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, allEntries = true)
    })
    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        userGroupSecurityService.checkCanCreateGroup(currentUser);
        UserGroup deletedGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));
        userGroupRepository.delete(deletedGroup);
        sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, SseSignalTypes.DELETED);
    }

    private void validateNameGroup(String name, Integer currentId) {

        userGroupRepository.findByName(name).ifPresent(existingGroup -> {
            if (!Objects.equals(existingGroup.getId(), currentId)) {
                throw new DublicateGroupUserException();
            }
        });
    }
}
