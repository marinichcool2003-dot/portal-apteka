package com.apteka.portal.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyDepartmentReportScheduler {

    private final DailyDepartmentReportService service;

    @Scheduled(cron = "${app.report.daily.cron:0 0 6 * * *}", zone = "${app.report.daily.zone:Europe/Moscow}")
    public void runDaily() {
        log.debug("Starting scheduled daily department report");
        service.runForYesterday();
    }
}
