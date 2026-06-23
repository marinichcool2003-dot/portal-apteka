package com.apteka.portal.repository.specification;

import jakarta.persistence.criteria.JoinType;
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
            
            if (filters.groupId() != null && filters.groupId() > 0) {
                predicates.add(cb.equal(
                    root.join("workType").join("groupTask").join("userGroup").get("id"),
                    filters.groupId()
                ));
            } else if (filters.groupTaskId() != null) {
                predicates.add(cb.equal(root.join("workType").join("groupTask").get("id"), filters.groupTaskId()));
            } else if (filters.workTypeId() != null) {
                predicates.add(cb.equal(root.join("workType").get("id"), filters.workTypeId()));
            }

            if (filters.creatorClientId() != null) {
                predicates.add(cb.equal(root.join("createdByClient", JoinType.LEFT).get("id"), filters.creatorClientId()));
            }
            if (filters.creatorAptekaId() != null) {
                predicates.add(cb.equal(root.join("createdByApteka", JoinType.LEFT).get("id"), filters.creatorAptekaId()));
            }
            if (filters.specificClientId() != null) {
                predicates.add(cb.equal(root.join("assignedClient", JoinType.LEFT).get("id"), filters.specificClientId()));
            }
            if (filters.specificAptekaId() != null) {
                predicates.add(cb.equal(root.join("assignedApteka", JoinType.LEFT).get("id"), filters.specificAptekaId()));
            }

            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }
            if (filters.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filters.priority()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
