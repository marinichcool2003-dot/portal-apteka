package com.apteka.portal.models;

public final class SseEventNames {
    private SseEventNames() {
    }

    public static final String CONNECT = "CONNECT";

    public static final String REFRESH_USER_GROUPS = "REFRESH_USER_GROUPS";

    public static final String REFRESH_WORK_TYPES = "REFRESH_WORK_TYPES";

    public static final String REFRESH_GROUP_TASKS = "REFRESH_GROUP_TASKS";

    public static final String REFRESH_GROUP_MAIN_PAGE_LINKS = "REFRESH_GROUP_MAIN_PAGE_LINKS";

    public static final String REFRESH_MAIN_PAGE_LINKS = "REFRESH_MAIN_PAGE_LINKS";

    public static final String REFRESH_CLIENTS = "REFRESH_CLIENTS";

    public static final String REFRESH_APTEKI = "REFRESH_APTEKI";

    public static final String REFRESH_TASKS = "REFRESH_TASKS";

    public static final String REFRESH_NEWS = "REFRESH_NEWS";

    public static final String REFRESH_GLOBAL = "REFRESH_GLOBAL";

    //СИГНАЛЫ ДЛЯ ВСТАВКИ/УДАЛЕНИЯ
    public record AppUserDetailsSignalDTO(Integer userGroupId, String action) {
    }
    public record GroupTaskSignalDTO(Integer creatorGroupId, Integer executorGroupId, String action) {
    }
    public record WorkTypeSignalDTO(Integer groupTaskId, String action) { 
    }
    public record MainPageLinkSignalDTO(Integer groupMainPageLinkId, String action) {
    }
    public record NewsSignalDTO(Integer userGroupId, String action) {
    }
    public record TaskSignalsDTO(Integer workTypeId, String action) {
    }

    //СИГНАЛЫ ДЛЯ ОБНОВЛЕНИЯ
    public record EntityUpdateSignalDTO(Object id, String action) {
    }
}
