package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTaskNotFoundException;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.WorkTypeInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTypeService {
    private final WorkTypeInterface workTaskInterface;
    private final GroupTaskService groupTaskService;

    public List<WorkType> getAll() {
        return workTaskInterface.findAll();
    }

    public WorkType getOne(Integer id) {
        return workTaskInterface.findById(id)
            .orElseThrow(() -> new WorkTaskNotFoundException(id));
    }

    public WorkType create(String name, Integer groupTaskId) {
        name = name.strip();
        if (name == null || name.isBlank()) {
            throw new InvalidWorkTypeNameException();
        }
        
        GroupTask groupTask = groupTaskService.getOne(groupTaskId);

        if (workTaskInterface.findByName(name).isPresent()) {
            throw new DublicateWorkTypeNameException(name);
        }

        WorkType newWorkType = WorkType.builder().name(name).groupTask(groupTask).build();
        return workTaskInterface.save(newWorkType);
    }

    public void delete(Integer id) {
        if (!workTaskInterface.existsById(id)) {
            throw new WorkTaskNotFoundException(id);
        }
        workTaskInterface.deleteById(id);
    }
}
