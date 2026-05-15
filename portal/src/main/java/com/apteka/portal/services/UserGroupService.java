package com.apteka.portal.services;

import com.apteka.portal.components.SecurityUtils;
import com.apteka.portal.components.UserGroupSecurityService;
import com.apteka.portal.dtos.request.UserGroupRequestDTO;
import com.apteka.portal.dtos.response.UserGroupResponseDTO;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.DublicateGroupUserException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupSecurityService userGroupSecurityService;
    private final UserGroupRepository userGroupRepository;

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
    public UserGroupResponseDTO create(UserGroupRequestDTO dto) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getRequiredCurrentUser());
        try {
            UserGroup group = userGroupRepository.save(UserGroup.builder()
                    .name(validateNameGroup(dto.name(), null))
                    .phoneNumber(dto.phoneNumber())
                    .build());
            return UserGroupResponseDTO.from(group);
        } catch (DataIntegrityViolationException e) {
            throw new DublicateGroupUserException(dto.name());
        }
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true)
    }, put = {
            @CachePut(value = CacheNames.USER_GROUP, key = "#id")
    })
    @Transactional
    public UserGroupResponseDTO update(Integer id, UserGroupRequestDTO dto) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getRequiredCurrentUser());
        UserGroup upGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));
        upGroup.setName(validateNameGroup(dto.name(), id));
        upGroup.setPhoneNumber(dto.phoneNumber());
        return UserGroupResponseDTO.from(upGroup);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUP, key = "#id"),
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#id"),
            @CacheEvict(value = CacheNames.GROUP_TASK, allEntries = true),
            @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, allEntries = true)
    })
    @Transactional
    public void delete(Integer id) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getRequiredCurrentUser());
        UserGroup deletedGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));
        userGroupRepository.delete(deletedGroup);
    }

    private String validateNameGroup(String name, Integer currentId) {

        String cleanName = name.strip();
        userGroupRepository.findByName(cleanName).ifPresent(existingGroup -> {
            if (!Objects.equals(existingGroup.getId(), currentId)) {
                throw new DublicateGroupUserException();
            }
        });
        
        return cleanName;
    }
}
