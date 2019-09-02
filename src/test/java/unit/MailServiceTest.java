package unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.corn.Application;
import com.corn.data.dto.mail.*;
import com.corn.service.MailService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test_mail")
@ContextConfiguration(classes = Application.class)
public class MailServiceTest {

    private final static String AUTHENTICATION_URL = "/email-campaign/api/authenticate";
    private final static String CONFIRMATION_URL = "/confirmation";
    private final static String UNSUBSCRIBE_URL = "/unsubscribe";
    private final static String EXT_JSON = "application/json, application/*+json";
    private final static String CONTENT_TYPE = "Content-Type";
    private final static String ACCEPT = "Accept";
    private final static String TEST_EMAIL = "test@mail.com";

    @Value("${corn.mail.template.status.id}")
    private int statusTemplateId;

    @Value("${corn.mail.template.confirmation.id}")
    private int confirmationTemplateId;

    @Value("${corn.mail.template.announcement.id}")
    private int announcementTemplateId;

    @Value("${corn.mail.username}")
    private String username;

    @Value("${corn.mail.password}")
    private String password;

    @Value("${corn.mail.key}")
    private String apiKey;


    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Autowired
    public MailService mailService;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void confirmationEmailTest() throws InterruptedException, JsonProcessingException {

        MailAuthResponseDTO authResponseDTO = new MailAuthResponseDTO();
        authResponseDTO.setId_token("test");
        MailAuthDTO authDTO = new MailAuthDTO(username, password, true);

        MailParameters parameters = new ConfirmationMailParameters(CONFIRMATION_URL);
        MailSendingDTO confirmationDTO = new MailSendingDTO(parameters, TEST_EMAIL, "");

        if (!mailService.isAuthenticated())
            stubFor(post(urlEqualTo(AUTHENTICATION_URL))
                    .withHeader(ACCEPT, equalTo(EXT_JSON))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(mapper.writeValueAsString(authResponseDTO))));

        stubFor(post(urlEqualTo(sendingUrl(confirmationTemplateId)))
                .withHeader(ACCEPT, equalTo(EXT_JSON))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(new MailResponseDTO("success")))));


        mailService.sendConfirmation(TEST_EMAIL, CONFIRMATION_URL);
        Thread.sleep(500); // asynchronous sending call requires some delay


        if (!mailService.isAuthenticated())
            verify(postRequestedFor(urlEqualTo(AUTHENTICATION_URL))
                    .withRequestBody(equalToJson(mapper.writeValueAsString(authDTO)))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));

        verify(postRequestedFor(urlEqualTo(sendingUrl(confirmationTemplateId)))
                .withRequestBody(equalToJson(mapper.writeValueAsString(confirmationDTO)))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void announcementEmailTest() throws InterruptedException, JsonProcessingException {

        MailAuthResponseDTO authResponseDTO = new MailAuthResponseDTO();
        authResponseDTO.setId_token("test");
        MailAuthDTO authDTO = new MailAuthDTO(username, password, true);

        String header = randomAlphabetic(10);
        String announcement = randomAlphabetic(100);
        MailParameters parameters = new AnnouncementMailParameters(header, announcement, UNSUBSCRIBE_URL);
        MailSendingDTO announcementDTO = new MailSendingDTO(parameters, TEST_EMAIL, "");

        Map<String, String> emailTuple = new HashMap<>();
        emailTuple.put(TEST_EMAIL, UNSUBSCRIBE_URL);

        if (!mailService.isAuthenticated())
            stubFor(post(urlEqualTo(AUTHENTICATION_URL))
                    .withHeader(ACCEPT, equalTo(EXT_JSON))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(mapper.writeValueAsString(authResponseDTO))));

        stubFor(post(urlEqualTo(sendingUrl(announcementTemplateId)))
                .withHeader(ACCEPT, equalTo(EXT_JSON))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(new MailResponseDTO("success")))));

        mailService.sendAnnouncement(emailTuple, header, announcement);
        Thread.sleep(500); // asynchronous sending call requires some delay

        if (!mailService.isAuthenticated())
            verify(postRequestedFor(urlEqualTo(AUTHENTICATION_URL))
                    .withRequestBody(equalToJson(mapper.writeValueAsString(authDTO)))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));

        verify(postRequestedFor(urlEqualTo(sendingUrl(announcementTemplateId)))
                .withRequestBody(equalToJson(mapper.writeValueAsString(announcementDTO)))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void statusEmailTest() throws InterruptedException, JsonProcessingException {

        MailAuthResponseDTO authResponseDTO = new MailAuthResponseDTO();
        authResponseDTO.setId_token("test");
        MailAuthDTO authDTO = new MailAuthDTO(username, password, true);

        String type = randomAlphabetic(10);
        String header = randomAlphabetic(10);
        String update = randomAlphabetic(100);
        String components = randomAlphabetic(100);

        MailParameters parameters = new StatusMailParameters(type, header, components, update, UNSUBSCRIBE_URL);
        MailSendingDTO announcementDTO = new MailSendingDTO(parameters, TEST_EMAIL, "");

        Map<String, String> emailTuple = new HashMap<>();
        emailTuple.put(TEST_EMAIL, UNSUBSCRIBE_URL);

        if (!mailService.isAuthenticated())
            stubFor(post(urlEqualTo(AUTHENTICATION_URL))
                    .withHeader(ACCEPT, equalTo(EXT_JSON))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(mapper.writeValueAsString(authResponseDTO))));

        stubFor(post(urlEqualTo(sendingUrl(statusTemplateId)))
                .withHeader(ACCEPT, equalTo(EXT_JSON))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(new MailResponseDTO("success")))));

        mailService.sendStatus(emailTuple, type, header, components, update);
        Thread.sleep(500); // asynchronous sending call requires some delay

        if (!mailService.isAuthenticated())
            verify(postRequestedFor(urlEqualTo(AUTHENTICATION_URL))
                    .withRequestBody(equalToJson(mapper.writeValueAsString(authDTO)))
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));

        verify(postRequestedFor(urlEqualTo(sendingUrl(statusTemplateId)))
                .withRequestBody(equalToJson(mapper.writeValueAsString(announcementDTO)))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    private String sendingUrl(int templateId) {
        return "/email-campaign/api/mail/" + templateId + "?api-key=" + apiKey;
    }

    private class MailResponseDTO {
        private String status;

        MailResponseDTO(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
