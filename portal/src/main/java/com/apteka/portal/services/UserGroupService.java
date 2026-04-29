package com.apteka.portal.services;

import com.apteka.portal.components.UserGroupSecurityService;
import com.apteka.portal.dtos.request.UserGroupRequestDTO;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.DublicateGroupClientException;
import com.apteka.portal.exceptions.GroupClientNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupClientException;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupSecurityService userGroupSecurityService;
    private final UserGroupRepository userGroupRepository;

    @Transactional(readOnly = true)
    public List<UserGroup> getAll() {
        return userGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserGroup getOne(Integer id) {
        return userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupClientNotFoundException(id));
    }

    @Transactional
    public UserGroup create(UserGroupRequestDTO dto) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getCurrentUser());
        return userGroupRepository.save(UserGroup.builder()
                .name(validateNameGroup(dto.name(), null))
                .phoneNumber(dto.phoneNumber())
                .build());
    }

    @Transactional
    public UserGroup update(Integer id, String name) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getCurrentUser());
        UserGroup upGroup = getOne(id);
        upGroup.setName(validateNameGroup(name, id));
        return userGroupRepository.save(upGroup);
    }

    @Transactional
    public void delete(Integer id) {
        userGroupSecurityService.checkCanCreateGroup(SecurityUtils.getCurrentUser());
        UserGroup deletedGroup = getOne(id);
        userGroupRepository.delete(deletedGroup);
    }

    private String validateNameGroup(String name, Integer currentId) {
        if (name == null || name.isBlank()) {
            throw new InvalidGroupClientException();
        }

        String cleanName = name.strip();

        userGroupRepository.findByName(cleanName).ifPresent(existingGroup -> {
            if (!Objects.equals(existingGroup.getId(), currentId)) {
                throw new DublicateGroupClientException();
            }
        });

        return cleanName;
    }

}
