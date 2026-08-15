package com.apteka.portal.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.apteka.portal.dtos.mail.EmailContent;
import com.apteka.portal.util.HtmlUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final ResourceLoader resourceLoader;

    private static final String DEFAULT_BRAND_NAME = "Социальная Аптека";

    @Value("${app.mail.brand.name:}")
    private String brandNameConfig;

    @Value("${app.mail.brand.accent-color:#007A33}")
    private String accentColor;

    @Value("${app.mail.brand.text-color:#333333}")
    private String textColor;

    @Value("${app.mail.brand.muted-color:#666666}")
    private String mutedColor;

    @Value("${app.mail.brand.background-color:#F5F5F5}")
    private String backgroundColor;

    @Value("${app.mail.brand.logo-path:classpath:logo.png}")
    private String logoPath;

    @Value("${app.mail.brand.portal-url:}")
    private String portalUrl;

    private String brandName;

    private boolean logoAvailable;

    @PostConstruct
    void initLogo() {
        brandName = (brandNameConfig == null || brandNameConfig.isBlank())
                ? DEFAULT_BRAND_NAME
                : brandNameConfig.trim();
        Resource logo = resourceLoader.getResource(logoPath);
        logoAvailable = logo.exists() && logo.isReadable();
    }

    public boolean isLogoAvailable() {
        return logoAvailable;
    }

    public EmailContent renderOtp(String purposeTitle, String code, int ttlMinutes) {
        String htmlContent = """
                <h2 style="margin:0 0 16px;font-size:20px;font-weight:600;color:%s;">%s</h2>
                <table role="presentation" cellspacing="0" cellpadding="0" style="margin:0 auto 20px;">
                <tr><td style="background-color:%s;border:1px solid %s;border-radius:6px;padding:16px 32px;text-align:center;">
                <span style="font-size:32px;font-weight:700;letter-spacing:8px;color:%s;font-family:Arial,Helvetica,sans-serif;">%s</span>
                </td></tr></table>
                <p style="margin:0 0 8px;color:%s;font-size:14px;">Код действителен %d мин.</p>
                <p style="margin:0;color:%s;font-size:14px;">Если вы не запрашивали код — проигнорируйте это письмо.</p>
                """.formatted(
                textColor, HtmlUtils.escapeHtml(purposeTitle),
                backgroundColor, accentColor, textColor, code,
                mutedColor, ttlMinutes, mutedColor);

        String plain = """
                %s

                %s

                Код действителен %d мин.
                Если вы не запрашивали код — проигнорируйте это письмо.
                """.formatted(purposeTitle, code, ttlMinutes).trim();

        return new EmailContent(purposeTitle, wrapLayout(htmlContent), wrapLayoutPlain(plain));
    }

    public EmailContent renderNotification(String subject, String message, String detailsHtml) {
        String htmlContent = """
                <h2 style="margin:0 0 12px;font-size:18px;font-weight:600;color:%s;">%s</h2>
                <p style="margin:0 0 16px;color:%s;font-size:15px;line-height:1.5;">%s</p>
                %s
                """.formatted(
                textColor, HtmlUtils.escapeHtml(subject),
                textColor, HtmlUtils.escapeHtml(message),
                detailsHtml != null ? detailsHtml : "");

        String plainDetails = detailsHtml != null ? "\n\n" + HtmlUtils.plainTextFromHtml(detailsHtml) : "";
        String plain = subject + "\n\n" + message + plainDetails;

        return new EmailContent(subject, wrapLayout(htmlContent), wrapLayoutPlain(plain));
    }

    public EmailContent renderNews(String title, String bodyText) {
        String escapedBody = HtmlUtils.escapeHtml(bodyText).replace("\n", "<br/>");
        String htmlContent = """
                <h2 style="margin:0 0 12px;font-size:18px;font-weight:600;color:%s;">%s</h2>
                <p style="margin:0;color:%s;font-size:15px;line-height:1.6;">%s</p>
                """.formatted(textColor, HtmlUtils.escapeHtml(title), textColor, escapedBody);

        String plain = title + "\n\n" + bodyText;
        return new EmailContent("Новость: " + title, wrapLayout(htmlContent), wrapLayoutPlain(plain));
    }

    public EmailContent renderReport(String subject, String innerHtml) {
        return new EmailContent(subject, wrapLayout(innerHtml),
                wrapLayoutPlain(subject + "\n\n" + HtmlUtils.plainTextFromHtml(innerHtml)));
    }

    /** HTML-блок деталей задачи для renderNotification. */
    public String taskDetailsHtml(Long taskId, String title, String statusName) {
        return """
                <table role="presentation" cellspacing="0" cellpadding="0" width="100%%" style="background-color:%s;border-radius:4px;padding:12px;">
                <tr><td style="color:%s;font-size:14px;line-height:1.6;">
                <strong>Задача #%d:</strong> %s<br/>
                <strong>Статус:</strong> %s
                </td></tr></table>
                """.formatted(backgroundColor, textColor, taskId,
                HtmlUtils.escapeHtml(title), HtmlUtils.escapeHtml(statusName));
    }

    /** Inner HTML для daily report (без layout). */
    public String buildReportInnerHtml(String groupName, String reportDate,
            long created, long closed, long denied, long inProgress, String tasksTableHtml) {
        return """
                <h2 style="margin:0 0 8px;font-size:18px;color:%s;">Ежедневный отчёт: %s</h2>
                <p style="margin:0 0 16px;color:%s;font-size:14px;">Дата: %s</p>
                <ul style="margin:0 0 20px;padding-left:20px;color:%s;font-size:14px;line-height:1.8;">
                <li>Создано: %d</li>
                <li>Закрыто: %d</li>
                <li>Отклонено: %d</li>
                <li>В работе: %d</li>
                </ul>
                <h3 style="margin:0 0 8px;font-size:16px;color:%s;">Задачи</h3>
                %s
                """.formatted(
                textColor, HtmlUtils.escapeHtml(groupName),
                mutedColor, HtmlUtils.escapeHtml(reportDate),
                textColor, created, closed, denied, inProgress,
                textColor, tasksTableHtml);
    }

    public String buildReportTasksTableHtml(String rowsHtml) {
        return """
                <table role="presentation" border="0" cellspacing="0" cellpadding="6" width="100%%"
                style="border-collapse:collapse;font-size:13px;color:%s;">
                <tr style="background-color:%s;">
                <th align="left" style="padding:8px;border-bottom:2px solid %s;">ID</th>
                <th align="left" style="padding:8px;border-bottom:2px solid %s;">Заголовок</th>
                <th align="left" style="padding:8px;border-bottom:2px solid %s;">Статус</th>
                <th align="left" style="padding:8px;border-bottom:2px solid %s;">Исполнитель</th>
                </tr>
                %s
                </table>
                """.formatted(textColor, backgroundColor, accentColor, accentColor, accentColor, accentColor, rowsHtml);
    }

    public String buildReportTaskRowHtml(Long id, String title, String status, String assignee) {
        return """
                <tr>
                <td style="padding:8px;border-bottom:1px solid #EEEEEE;">%d</td>
                <td style="padding:8px;border-bottom:1px solid #EEEEEE;">%s</td>
                <td style="padding:8px;border-bottom:1px solid #EEEEEE;">%s</td>
                <td style="padding:8px;border-bottom:1px solid #EEEEEE;">%s</td>
                </tr>
                """.formatted(id, HtmlUtils.escapeHtml(title), HtmlUtils.escapeHtml(status), HtmlUtils.escapeHtml(assignee));
    }

    private String wrapLayout(String contentHtml) {
        String header = logoAvailable
                ? """
                <img src="cid:brandLogo" alt="%s" width="280" style="max-width:280px;height:auto;display:block;margin:0 auto;border:0;"/>
                """.formatted(HtmlUtils.escapeHtml(brandName))
                : """
                <span style="font-size:20px;font-weight:700;color:%s;">%s</span>
                """.formatted(accentColor, HtmlUtils.escapeHtml(brandName));

        String footerLink = buildFooterLinkHtml();

        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background-color:%s;font-family:Arial,Helvetica,sans-serif;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:%s;">
                <tr><td align="center" style="padding:24px 16px;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background-color:#FFFFFF;max-width:600px;width:100%%;">
                <tr><td style="padding:24px;text-align:center;">%s</td></tr>
                <tr><td style="border-top:2px solid %s;font-size:0;line-height:0;height:0;">&nbsp;</td></tr>
                <tr><td style="padding:24px;">%s</td></tr>
                <tr><td style="padding:16px 24px;border-top:1px solid #EEEEEE;color:%s;font-size:12px;line-height:1.6;text-align:center;">
                %sЕсли вы не запрашивали это письмо — проигнорируйте его.<br/>
                &copy; %s
                </td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """.formatted(
                backgroundColor, backgroundColor, header, accentColor, contentHtml,
                mutedColor, footerLink, HtmlUtils.escapeHtml(brandName));
    }

    private String buildFooterLinkHtml() {
        if (!shouldShowPortalUrl()) {
            return "";
        }
        String safeUrl = HtmlUtils.escapeHtml(portalUrl.trim());
        return """
                <a href="%s" style="color:%s;text-decoration:none;">%s</a><br/>
                """.formatted(safeUrl, accentColor, safeUrl);
    }

    private boolean shouldShowPortalUrl() {
        if (portalUrl == null || portalUrl.isBlank()) {
            return false;
        }
        String lower = portalUrl.toLowerCase();
        return !lower.contains("localhost") && !lower.contains("127.0.0.1");
    }

    private String wrapLayoutPlain(String body) {
        StringBuilder sb = new StringBuilder(body).append("\n\n---\n");
        if (shouldShowPortalUrl()) {
            sb.append(portalUrl.trim()).append('\n');
        }
        sb.append("© ").append(brandName);
        return sb.toString();
    }
}
