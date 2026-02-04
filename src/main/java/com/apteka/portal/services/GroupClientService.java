package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.DublicateGroupClientException;
import com.apteka.portal.exceptions.GroupClientNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupClientException;
import com.apteka.portal.models.GroupClient;
import com.apteka.portal.repository.GroupClientInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupClientService {
    private final GroupClientInterface groupClientInterface;

    @Transactional(readOnly = true)
    public List<GroupClient> getAll() {
        return groupClientInterface.findAll();
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public GroupClient getOne(Integer id) {
        return groupClientInterface.findById(id)
            .orElseThrow(() -> new GroupClientNotFoundException(id));
    }

    @SuppressWarnings("null")
    @Transactional
    public GroupClient create(String name){
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupClientException();
        }
        if (groupClientInterface.findByName(name).isPresent()) {
            throw new DublicateGroupClientException(name);
        }

        return groupClientInterface.save(GroupClient.builder()
                .name(name)
                .build());
    }

    @Transactional
    public GroupClient update(Integer id, String name){
        GroupClient upGroup = getOne(id);
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

    @SuppressWarnings("null")
    @Transactional
    public void delete(Integer id){
        if (!groupClientInterface.existsById(id)) {
            throw new GroupClientNotFoundException(id);
        }
        groupClientInterface.deleteById(id);
    }
}
