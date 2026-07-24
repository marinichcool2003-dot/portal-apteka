package com.apteka.portal.repository.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.models.Task;

public class TaskSpecifications {
    private TaskSpecifications(){}

    public static Specification<Task> getTaskWithFilters(DepartamentTaskWithFiltersDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<?, ?> workType = root.join("workType");
            Join<?, ?> groupTask = workType.join("groupTask");
            
            if (filters.groupId() != null && filters.groupId() > 0) {
                predicates.add(cb.equal(
                    groupTask.join("creatorGroup").get("id"),
                    filters.groupId()
                ));
            } else if (filters.groupTaskId() != null) {
                predicates.add(cb.equal(groupTask.get("id"), filters.groupTaskId()));
            } else if (filters.workTypeId() != null) {
                predicates.add(cb.equal(workType.get("id"), filters.workTypeId()));
            }

            if (filters.creatorId() != null) {
                predicates.add(cb.equal(root.join("creator", JoinType.LEFT).get("id"), filters.creatorId()));
            }
            if (filters.assignerId() != null) {
                predicates.add(cb.equal(root.join("assigner", JoinType.LEFT).get("id"), filters.assignerId()));
            }
            
            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }
            if (filters.priority() != null) {
                predicates.add(cb.equal(workType.get("priority"), filters.priority()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
