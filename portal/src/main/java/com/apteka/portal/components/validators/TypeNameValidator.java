package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;

@Component
public class TypeNameValidator {
    public String getCleanName(String name) {
        return name.strip().replaceAll("\\s+", " ");
    }
}
