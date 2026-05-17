package com.apteka.portal.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String errorMessage;
    private long currentTimesMillis;
}
