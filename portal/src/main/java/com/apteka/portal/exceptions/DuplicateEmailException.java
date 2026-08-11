package com.apteka.portal.exceptions;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Email уже используется: " + email);
    }
}
