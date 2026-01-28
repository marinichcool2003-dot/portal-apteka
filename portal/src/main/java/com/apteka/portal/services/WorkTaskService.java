package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.WorkTaskNotFoundException;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.WorkTypeInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WorkTaskService {
    private final WorkTypeInterface workTaskInterface;

    public List<WorkType> getAll() {
        return workTaskInterface.findAll();
    }

    public WorkType getOne(Integer id) {
        return workTaskInterface.findById(id)
            .orElseThrow(() -> new WorkTaskNotFoundException(id));
    }

    public void delete(Integer id) {
        if (!workTaskInterface.existsById(id)) {
            throw new WorkTaskNotFoundException(id);
        }
        workTaskInterface.deleteById(id);
    }
}
