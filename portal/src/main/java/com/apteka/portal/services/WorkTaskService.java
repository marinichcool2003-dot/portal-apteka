package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.WorkTaskNotFoundException;
import com.apteka.portal.models.WorkTask;
import com.apteka.portal.repository.WorkTaskInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WorkTaskService {
    private final WorkTaskInterface workTaskInterface;

    public List<WorkTask> getAll() {
        return workTaskInterface.findAll();
    }

    public WorkTask getOne(Integer id) {
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
