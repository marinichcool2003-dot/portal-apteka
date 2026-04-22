package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.DublicateGroupClientException;
import com.apteka.portal.exceptions.GroupClientNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupClientException;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupInterface groupClientInterface;

    @Transactional(readOnly = true)
    public List<UserGroup> getAll() {
        return groupClientInterface.findAll();
    }

    @Transactional(readOnly = true)
    public UserGroup getOne(Integer id) {
        return groupClientInterface.findById(id)
            .orElseThrow(() -> new GroupClientNotFoundException(id));
    }

    @Transactional
    public UserGroup create(String name){
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupClientException();
        }
        if (groupClientInterface.findByName(name).isPresent()) {
            throw new DublicateGroupClientException(name);
        }

        return groupClientInterface.save(UserGroup.builder()
                .name(name)
                .build());
    }

    @Transactional
    public UserGroup update(Integer id, String name){
        UserGroup upGroup = getOne(id);
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupClientException();
        }
        if (groupClientInterface.findByName(name).isPresent()) {
            throw new DublicateGroupClientException(name);
        }
        upGroup.setName(name);
        return groupClientInterface.save(upGroup);
    }

    @Transactional
    public void delete(Integer id){
        if (!groupClientInterface.existsById(id)) {
            throw new GroupClientNotFoundException(id);
        }
        groupClientInterface.deleteById(id);
    }
}
