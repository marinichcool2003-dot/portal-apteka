package com.apteka.portal.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apteka.portal.dtos.mail.EmailContent;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.NotificationChannel;
import com.apteka.portal.models.NotificationEventType;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserGroupType;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.NotificationPreferenceRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyDepartmentReportService {

    private final UserGroupRepository userGroupRepository;
    private final AccountRepository accountRepository;
    private final TaskRepository taskRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.report.daily.zone:Europe/Moscow}")
    private String reportZone;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Transactional
    public Map<String, Object> runForYesterday() {
        ZoneId zone = ZoneId.of(reportZone);
        LocalDate yesterday = LocalDate.now(zone).minusDays(1);
        return runForDate(yesterday);
    }

    @Transactional
    public Map<String, Object> runForDate(LocalDate reportDate) {
        ZoneId zone = ZoneId.of(reportZone);
        Instant start = reportDate.atStartOfDay(zone).toInstant();
        Instant end = reportDate.plusDays(1).atStartOfDay(zone).toInstant();

        List<UserGroup> employeeGroups = userGroupRepository.findByIsActiveAndGroupType(true, UserGroupType.EMPLOYEE_GROUP);

        int groupsProcessed = 0;
        int emailsSent = 0;

        for (UserGroup group : employeeGroups) {
            List<Account> bosses = accountRepository.findActiveByUserGroupIdAndRole(group.getId(), UserRole.BOSS);
            if (bosses.isEmpty()) {
                continue;
            }

            List<Task> tasks = taskRepository.findDepartmentTasksForDailyReport(group.getId(), start, end);
            EmailContent content = buildReportContent(group, reportDate, tasks, start, end);

            for (Account boss : bosses) {
                if (!isDailyReportEmailEnabled(boss.getId())) {
                    continue;
                }
                mailService.send(boss.getEmail(), content, NotificationEventType.DAILY_DEPARTMENT_REPORT);
                emailsSent++;
            }
            groupsProcessed++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", reportDate.toString());
        result.put("zone", reportZone);
        result.put("groupsProcessed", groupsProcessed);
        result.put("emailsSent", emailsSent);
        log.info("Daily department report for {}: {} groups, {} emails", reportDate, groupsProcessed, emailsSent);
        return result;
    }

    private boolean isDailyReportEmailEnabled(java.util.UUID accountId) {
        return notificationPreferenceRepository
                .findByAccountIdAndChannelAndEventType(
                        accountId, NotificationChannel.EMAIL, NotificationEventType.DAILY_DEPARTMENT_REPORT)
                .map(p -> p.isEnabled())
                .orElse(false);
    }

    private EmailContent buildReportContent(UserGroup group, LocalDate reportDate, List<Task> tasks,
            Instant start, Instant end) {
        long created = tasks.stream()
                .filter(t -> t.getCreationDate() != null
                        && !t.getCreationDate().isBefore(start)
                        && t.getCreationDate().isBefore(end))
                .count();
        long closed = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.CLOSED
                        && t.getClosingDate() != null
                        && !t.getClosingDate().isBefore(start)
                        && t.getClosingDate().isBefore(end))
                .count();
        long denied = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DENIED
                        && t.getUpdatedDate() != null
                        && !t.getUpdatedDate().isBefore(start)
                        && t.getUpdatedDate().isBefore(end))
                .count();
        long inProgress = tasks.stream()
                .filter(t -> (t.getStatus() == TaskStatus.OPEN || t.getStatus() == TaskStatus.PROCESSED)
                        && t.getCreationDate() != null
                        && t.getCreationDate().isBefore(end)
                        && (t.getClosingDate() == null || !t.getClosingDate().isBefore(end)))
                .count();

        StringBuilder rows = new StringBuilder();
        for (Task task : new ArrayList<>(tasks)) {
            rows.append(emailTemplateService.buildReportTaskRowHtml(
                    task.getId(),
                    task.getTitle(),
                    task.getStatus().getName(),
                    resolveAssigneeName(task)));
        }

        String tableHtml = emailTemplateService.buildReportTasksTableHtml(rows.toString());
        String innerHtml = emailTemplateService.buildReportInnerHtml(
                group.getName(),
                reportDate.format(DATE_FORMAT),
                created, closed, denied, inProgress,
                tableHtml);

        String subject = String.format("Ежедневный отчёт по отделу «%s» за %s",
                group.getName(), reportDate.format(DATE_FORMAT));
        return emailTemplateService.renderReport(subject, innerHtml);
    }

    private String resolveAssigneeName(Task task) {
        if (task.getAssigner() == null) {
            return "Не назначена";
        }
        Client client = task.getAssigner().getClient();
        if (client != null && StringUtils.hasText(client.getFullName())) {
            return client.getFullName();
        }
        return task.getAssigner().getLogin();
    }
}
