package com.apteka.portal.dtos.request;

import java.util.UUID;

public interface TaskRequestDTO {
    public String title();
    public String description();
    public Integer workTypeId();
    public UUID assignedAptekaId();
    public UUID assignedClientId();
}
