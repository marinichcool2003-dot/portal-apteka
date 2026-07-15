package com.apteka.portal.dtos.request.task;

import java.util.UUID;

public interface TaskRequestDTO {
    public String title();
    public String description();
    public Integer workTypeId();
    public UUID assignerId();
}
