package com.apteka.portal.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.models.Task;

import jakarta.persistence.criteria.Predicate;

public class TaskSpecifications {
    private TaskSpecifications(){}

    public static Specification<Task> getTaskWithFilters(DepartamentTaskWithFiltersDTO filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.groupId() != null && filters.groupId() > 0) {
                predicates.add(cb.equal(
                    root.get("workType").get("groupTask").get("userGroup").get("id"),
                    filters.groupId()
                ));
            }

            if (filters.creatorClientId() != null) {
                predicates.add(cb.equal(root.get("createdByClient").get("id"), filters.creatorClientId()));
            }
            if (filters.creatorAptekaId() != null) {
                predicates.add(cb.equal(root.get("createdByApteka").get("id"), filters.creatorAptekaId()));
            }
            if (filters.specificClientId() != null) {
                predicates.add(cb.equal(root.get("assignedClient").get("id"), filters.specificClientId()));
            }
            if (filters.specificAptekaId() != null) {
                predicates.add(cb.equal(root.get("assignedApteka").get("id"), filters.specificAptekaId()));
            }

            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }
            if (filters.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filters.priority()));
            }

            if (filters.workTypeId() != null) {
                predicates.add(cb.equal(root.get("workType").get("id"), filters.workTypeId()));
            }
            if (filters.groupTaskId() != null) {
                predicates.add(cb.equal(root.get("workType").get("groupTask").get("id"), filters.groupTaskId()));
            }

            query.orderBy(cb.desc(root.get("creationDate")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
