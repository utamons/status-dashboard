package com.corn.service;

import java.util.Map;

public interface MailService {
    void sendStatus(Map<String, String> emailTuples, String statusType, String statusString, String componentsString, String updateMessage);

    void sendAnnouncement(Map<String, String> emailTuples, String header, String announcement);

    void sendConfirmation(String email, String confirmationUrl);

    boolean isAuthenticated();
}
