package com.apteka.portal.exceptions;

public class ClientBelongsToAnotherGroupException extends RuntimeException {
    public ClientBelongsToAnotherGroupException(String name) {
        super("Нельзя распределить задачу на сотрудника: " + name + " так как он принадлежит к другой группе");
    }
}
