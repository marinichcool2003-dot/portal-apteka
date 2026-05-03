package com.apteka.portal.services;

import com.apteka.portal.components.UserGroupSecurityService;
import com.apteka.portal.dtos.request.UserGroupRequestDTO;

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
import com.apteka.portal.exceptions.GroupClientNotFoundException;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupSecurityService userGroupSecurityService;
    private final UserGroupRepository userGroupRepository;

    @Cacheable(value = CacheNames.USER_GROUPS_LIST)
    @Transactional(readOnly = true)
    public List<UserGroup> getAll() {
        return userGroupRepository.findAll();
    }

    @Cacheable(value = CacheNames.USER_GROUP, key = "#id")
    @Transactional(readOnly = true)
    public UserGroup getOne(Integer id) {
        return userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupClientNotFoundException(id));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true)
    })
    @Transactional
    public UserGroup create(UserGroupRequestDTO dto) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getCurrentUser());
        try {
            return userGroupRepository.save(UserGroup.builder()
                    .name(validateNameGroup(dto.name(), null))
                    .phoneNumber(dto.phoneNumber())
                    .build());
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
    public UserGroup update(Integer id, UserGroupRequestDTO dto) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getCurrentUser());
        UserGroup upGroup = getOne(id);
        upGroup.setName(validateNameGroup(dto.name(), id));
        upGroup.setPhoneNumber(dto.phoneNumber());
        return upGroup;
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
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getCurrentUser());
        UserGroup deletedGroup = getOne(id);
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
