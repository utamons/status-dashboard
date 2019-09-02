package unit.dto.mail;

import com.corn.data.dto.mail.*;
import org.junit.Test;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MailDTOTest {

    @Test
    public void announcementMailParametersTest() {
        final String header = randomAlphabetic(10);
        final String announcement = randomAlphabetic(10);
        final String url = randomAlphabetic(10);

        AnnouncementMailParameters test =
                new AnnouncementMailParameters(
                        header,
                        announcement,
                        url
                );

        assertEquals(announcement, test.getAnnouncement());
        assertEquals(header, test.getHeader());
        assertEquals(url, test.getUrl());
    }

    @Test
    public void confirmationMailParametersTest() {
        final String url = randomAlphabetic(10);
        ConfirmationMailParameters test = new ConfirmationMailParameters(url);
        assertEquals(url, test.getUrl());
    }

    @Test
    public void mailAuthDTOTest() {
        final String username = randomAlphabetic(10);
        final String password = randomAlphabetic(10);
        final boolean rememberMe = true;

        MailAuthDTO test = new MailAuthDTO(username, password, rememberMe);

        assertEquals(username, test.getUsername());
        assertEquals(password, test.getPassword());
        assertEquals(rememberMe, test.getRememberMe());
    }

    @Test
    public void mailAuthResponseDTOTest() {
        final String token = randomAlphabetic(10);

        MailAuthResponseDTO test = new MailAuthResponseDTO();

        test.setId_token(token);

        assertEquals(token, test.getId_token());
    }

    @Test
    public void mailSendingDTOTest() {
        final String url = randomAlphabetic(10);
        final String toEmail = randomAlphabetic(10);
        final String toName = randomAlphabetic(10);

        ConfirmationMailParameters cmp = new ConfirmationMailParameters(url);

        MailSendingDTO test = new MailSendingDTO(cmp, toEmail, toName);

        assertEquals(cmp, test.getParameters());
        assertEquals(toEmail, test.getTo_email());
        assertEquals(toName, test.getTo_name());
    }

    @Test
    public void statusMailParametersTest() {
        final String type = randomAlphabetic(10),
                header = randomAlphabetic(10),
                components = randomAlphabetic(10),
                update = randomAlphabetic(10),
                url = randomAlphabetic(10);

        StatusMailParameters test = new StatusMailParameters(type, header, components, update, url);

        assertEquals(type, test.getType());
        assertEquals(header, test.getHeader());
        assertEquals(components, test.getComponents());
        assertEquals(update, test.getUpdate());
        assertEquals(url, test.getUrl());
    }
}