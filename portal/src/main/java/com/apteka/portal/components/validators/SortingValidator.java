package com.apteka.portal.components.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SortingValidator {
    public Pageable validateAndFixSorting(Pageable pageable, Set<String> ALLOWED_SORT_FIELDS, Sort DEFAULT_SORT) {
        int pageSize = Math.min(pageable.getPageSize(), 100);
        if (!pageable.getSort().isSorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageSize,
                    DEFAULT_SORT);
        }

        List<Sort.Order> validOrders = new ArrayList<>();
        boolean hasInvalidField = false;

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();

            if (ALLOWED_SORT_FIELDS.contains(property)) {
                validOrders.add(order);
            } else {
                hasInvalidField = true;
            }
        }

        if (hasInvalidField || validOrders.isEmpty()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageSize,
                    DEFAULT_SORT);
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageSize,
                Sort.by(validOrders));

    }
}
