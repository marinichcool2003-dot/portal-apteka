package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.repository.GroupTaskInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupTaskService {

    private final GroupTaskInterface groupTaskInterface;

    @Transactional(readOnly = true)
    public List<GroupTask> getAll() {
        return groupTaskInterface.findAll();
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public GroupTask getOne(@PathVariable Integer id) {
        return groupTaskInterface.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
    }

    @SuppressWarnings("null")
    @Transactional
    public GroupTask create(String name) {
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupTaskException(name);
        }
        if (groupTaskInterface.findByName(name).isPresent()) {
            throw new DublicateGroupTaskException(name);
        }
        return groupTaskInterface.save(GroupTask.builder()
                .name(name)
                .build());
    }

    @Transactional
    public GroupTask update(Integer id, String name){
        GroupTask upGroup = getOne(id);
        name = name.strip();
        if (name == null || name.isEmpty()) {
            throw new InvalidGroupTaskException(name);
        }
        if (groupTaskInterface.findByName(name).isPresent()) {
            throw new DublicateGroupTaskException(name);
        }

        upGroup.setName(name);
        return groupTaskInterface.save(upGroup);
    }

    @SuppressWarnings("null")
    @Transactional
    public void delete(Integer id) {
        if (!groupTaskInterface.existsById(id)) {
            throw new GroupTaskNotFoundException(id);
        }
        groupTaskInterface.deleteById(id);
    }
}
