package com.apteka.portal.dtos.mail;

public record EmailContent(String subject, String htmlBody, String plainBody) {
}
