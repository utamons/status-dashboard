package com.corn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.corn.data.dto.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Supplier;

import static com.corn.util.Constants.FINISH;
import static com.corn.util.Constants.START;
import static com.corn.util.Utils.isNotEmpty;
import static java.util.Objects.requireNonNull;

@Service
@Profile({"h2db","main","test_mail"})
public class MailServiceImpl implements MailService {

    private final static boolean FULL_DEBUG = false;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${corn.mail.template.status.id}")
    private int statusTemplateId;

    @Value("${corn.mail.template.confirmation.id}")
    private int confirmationTemplateId;

    @Value("${corn.mail.template.announcement.id}")
    private int announcementTemplateId;

    @Value("${corn.mail.sending}")
    private boolean mailingEnabled;

    @Value("${corn.mail.username}")
    private String username;

    @Value("${corn.mail.password}")
    private String password;

    @Value("${corn.mail.key}")
    private String apiKey;

    @Value("${corn.mail.url}")
    private String apiUrl;

    private String authToken;

    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public void sendStatus(Map<String, String> emailTuples, String statusType, String statusString, String componentsString, String updateMessage) {
        logger.debug(START);
        if (!mailingEnabled) {
            logger.debug("Mailing is disabled");
            return;
        }

        Map<String, MailParameters> parametersMap = new HashMap<>();

        emailTuples.forEach(
                (email, unsubscribeUrl) -> parametersMap.put(
                        email,
                        new StatusMailParameters(
                                statusType,
                                statusString,
                                componentsString,
                                updateMessage,
                                unsubscribeUrl)
                ));

        new Thread(new BatchSender(statusTemplateId, prepareSendingList(parametersMap))).start();
        logger.debug(FINISH);
    }

    @Override
    public void sendAnnouncement(Map<String, String> emailTuples, String header, String announcement) {
        logger.debug(START);
        if (!mailingEnabled) {
            logger.debug("Mailing is disabled");
            return;
        }

        Map<String, MailParameters> parametersMap = new HashMap<>();

        emailTuples.forEach(
                (email, unsubscribeUrl) -> parametersMap.put(
                        email,
                        new AnnouncementMailParameters(
                                header,
                                announcement,
                                unsubscribeUrl)
                ));

        final List<MailSendingDTO> sendingList = prepareSendingList(parametersMap);

        new Thread(new BatchSender(announcementTemplateId, sendingList)).start();
        logger.debug(FINISH);
    }

    @Override
    public void sendConfirmation(String email, String confirmationUrl) {
        logger.debug(START);
        if (!mailingEnabled) {
            logger.debug("Mailing is disabled");
            return;
        }

        MailParameters parameters = new ConfirmationMailParameters(confirmationUrl);
        Map<String, MailParameters> parametersMap = new HashMap<>();
        parametersMap.put(email, parameters);


        final List<MailSendingDTO> sendingList = prepareSendingList(parametersMap);

        new Thread(new BatchSender(confirmationTemplateId, sendingList)).start();
        logger.debug(FINISH);
    }

    @Override
    public boolean isAuthenticated() {
        return authToken != null;
    }

    private List<MailSendingDTO> prepareSendingList(Map<String, MailParameters> parameters) {
        final List<MailSendingDTO> intermediateList = new ArrayList<>();
        parameters.forEach((key, value) -> intermediateList.add(new MailSendingDTO(value, key, "")));
        return Collections.unmodifiableList(intermediateList);
    }

    private void send(int templateId, MailSendingDTO sendingDTO) {
        logger.debug(START);
        Object result = null;
        try {
            result = authCall(post(apiUrl + "mail/" + templateId, sendingDTO, Object.class, true, true));
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException, {}", e.getMessage());
        }

        if (result == null)
            logger.error("finish, failed");
        else
            logger.info("mail has been sent to {}", sendingDTO.getTo_email());
    }


    private boolean authenticate() throws JsonProcessingException {
        logger.debug(START);
        boolean result = false;

        try {
            ResponseEntity<MailAuthResponseDTO> response = post(
                    apiUrl + "authenticate",
                    new MailAuthDTO(username, password, true),
                    MailAuthResponseDTO.class,
                    false,
                    false
            ).get();

            authToken = requireNonNull(response.getBody()).getId_token();
            result = true;
        } catch (HttpClientErrorException e) {
            logger.error("Authentication failed - HTTP {}", e.getRawStatusCode());
            String responseBody = e.getResponseBodyAsString();
            if (isNotEmpty(responseBody))
                logger.error("HTTP body {}", e.getResponseBodyAsString());
        }

        logger.debug("finish, auth={}", result);
        return result;
    }

    private <R> R authCall(Supplier<ResponseEntity<R>> call) {
        logger.debug(START);
        R result = null;
        try {
            if (isAuthenticated() || authenticate()) {
                ResponseEntity<R> response;
                try {
                    response = call.get();
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED && authenticate()) { // if token has been expired
                        response = call.get();
                    } else {
                        throw e;
                    }
                }
                result = requireNonNull(response).getBody();
            }
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException, {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            logger.error("Cannot make mail API call, got HTTP {}", e.getRawStatusCode());
            String responseBody = e.getResponseBodyAsString();
            if (isNotEmpty(responseBody))
                logger.error("HTTP body {}", e.getResponseBodyAsString());
        }
        logger.debug(FINISH);
        return result;
    }

    private <T, R> Supplier<ResponseEntity<R>> post(String url, T value, Class<R> type, boolean auth, boolean needApiKey) throws JsonProcessingException {

        String json = mapper.writeValueAsString(value);

        return () -> {
            HttpHeaders headers = new HttpHeaders();
            if (auth)
                headers.add("Authorization", "Bearer " + authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            String finalUrl = url(url, needApiKey);

            if (FULL_DEBUG) {
                logger.debug("POST:");
                logger.debug("Url: {}", finalUrl);
                logger.debug("Json: {}", json);
                @SuppressWarnings("UnusedAssignment") Map<String, String> hMap = headers.toSingleValueMap();
                logger.debug("Headers: ");
                hMap.forEach((k, v) -> logger.debug("{} : {}", k, v));
            }

            ResponseEntity<R> response = restTemplate.postForEntity(finalUrl, entity, type);

            if (FULL_DEBUG) {
                logger.debug("Response:");
                logger.debug("Status: {}", response.getStatusCodeValue());
                if (response.getBody() != null)
                    logger.debug("Body: {}", response.getBody().toString());
            }

            return response;
        };
    }


    private String url(String url, boolean needApiKey) {
        if (needApiKey && url.contains("?"))
            return url + "&api-key=" + apiKey;
        else if (needApiKey)
            return url + "?api-key=" + apiKey;
        else
            return url;
    }

    class BatchSender implements Runnable {

        private final int templateId;
        private final List<MailSendingDTO> sendingList;

        BatchSender(final int templateId, final List<MailSendingDTO> sendingList) {
            this.templateId = templateId;
            this.sendingList = sendingList;
        }

        @Override
        public void run() {
            sendingList.forEach(dto -> send(templateId, dto));
        }
    }
}
