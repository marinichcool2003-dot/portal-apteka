package com.apteka.portal.exceptions;

public class AptekaTaskRatingEditLimitExceededException extends RuntimeException {
    public AptekaTaskRatingEditLimitExceededException() {
        super("Сотрудник может редактировать оценку только один раз!");
    }
}
