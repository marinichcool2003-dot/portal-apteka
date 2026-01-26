package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.DublicateGroupAptekiException;
import com.apteka.portal.exceptions.GroupAptekiNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupAptekiException;
import com.apteka.portal.models.GroupApteki;
import com.apteka.portal.repository.GroupAptekiInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupAptekiService {
    private final GroupAptekiInterface groupAptekiInterface;

    @Transactional(readOnly = true)
    public List<GroupApteki> getAll() {
        return groupAptekiInterface.findAll();
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public GroupApteki getOne(Integer id) {
        return groupAptekiInterface.findById(id)
                .orElseThrow(() -> new GroupAptekiNotFoundException(id));
    }

    @SuppressWarnings("null")
    @Transactional
    public GroupApteki create(String name) {
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupAptekiException();
        }
        if (groupAptekiInterface.findByName(name).isPresent()) {
            throw new DublicateGroupAptekiException(name);
        }
        return groupAptekiInterface.save(GroupApteki.builder()
                .name(name)
                .build());
    }

    @Transactional
    public GroupApteki update(Integer id, String name){
        GroupApteki upGroup = getOne(id);
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupAptekiException();
        }
        if (groupAptekiInterface.findByName(name).isPresent()) {
            throw new DublicateGroupAptekiException(name);
        } 
        upGroup.setName(name);
        return groupAptekiInterface.save(upGroup);
    }

    @SuppressWarnings("null")
    @Transactional
    public void delete(Integer id) {
        if (!groupAptekiInterface.existsById(id)) {
            throw new GroupAptekiNotFoundException(id);
        }
        groupAptekiInterface.deleteById(id);
    }
}
