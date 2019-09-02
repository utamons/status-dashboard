package com.corn.service.mock;

import com.corn.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile({"test","test_ports"})
public class MockMailService implements MailService {

    private static final Map<Long, List<MockEmail>> emails = new ConcurrentHashMap<>();
    public final static String STATUS = "status";
    public final static String ANNOUNCEMENT = "announcement";
    public final static String CONFIRMATION = "announcement";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void sendStatus(Map<String, String> emailTuples, String statusType, String statusString, String componentsString, String updateMessage) {
        logger.debug("start");
        long id = Thread.currentThread().getId();
        Map<String,String> props = new HashMap<>();

        props.put("statusType",statusType);
        props.put("statusString",statusString);
        props.put("componentsString",componentsString);
        props.put("updateMessage",updateMessage);


        List<MockEmail> mockEmails = new ArrayList<>();
        for (String email: emailTuples.keySet()) {
            String unsubscribeUrl = emailTuples.get(email);
            MockEmail mockEmail = new MockEmail(email, STATUS,unsubscribeUrl,props);
            mockEmails.add(mockEmail);
        }

        emails.put(id,mockEmails);
    }

    @Override
    public void sendAnnouncement(Map<String, String> emailTuples, String header, String announcement) {
        logger.debug("start");
        long id = Thread.currentThread().getId();
        Map<String,String> props = new HashMap<>();

        props.put("header",header);
        props.put("announcement",announcement);

        List<MockEmail> mockEmails = new ArrayList<>();
        for (String email: emailTuples.keySet()) {
            String unsubscribeUrl = emailTuples.get(email);
            MockEmail mockEmail = new MockEmail(email,ANNOUNCEMENT,unsubscribeUrl,props);
            mockEmails.add(mockEmail);
        }

        emails.put(id,mockEmails);
    }

    @Override
    public void sendConfirmation(String email, String confirmationUrl) {
        logger.debug("start");
        long id = Thread.currentThread().getId();
        Map<String,String> props = new HashMap<>();

        props.put("confirmationUrl",confirmationUrl);

        MockEmail mockEmail = new MockEmail(email,CONFIRMATION,null,props);

        emails.put(id,Collections.singletonList(mockEmail));
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    public static void cleanAllEmails() {
        emails.clear();
    }

    public static Set<Long> allThreadKeys() {
        return emails.keySet();
    }

    public static List<MockEmail> emails(Long threadKey) {
        return emails.get(threadKey);
    }
}
