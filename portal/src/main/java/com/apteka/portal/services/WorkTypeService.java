package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTaskNotFoundException;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;
    private final GroupTaskService groupTaskService;

    public List<WorkType> getAll() {
        return workTypeRepository.findAll();
    }

    public WorkType getOne(Integer id) {
        return workTypeRepository.findById(id)
            .orElseThrow(() -> new WorkTaskNotFoundException(id));
    }

    public WorkType create(String name, Integer groupTaskId) {
        name = name.strip();
        if (name == null || name.isBlank()) {
            throw new InvalidWorkTypeNameException();
        }
        
        GroupTask groupTask = groupTaskService.getOne(groupTaskId);

        if (workTypeRepository.findByName(name).isPresent()) {
            throw new DublicateWorkTypeNameException(name);
        }

        WorkType newWorkType = WorkType.builder().name(name).groupTask(groupTask).build();
        return workTypeRepository.save(newWorkType);
    }

    public void delete(Integer id) {
        if (!workTypeRepository.existsById(id)) {
            throw new WorkTaskNotFoundException(id);
        }
        workTypeRepository.deleteById(id);
    }
}
