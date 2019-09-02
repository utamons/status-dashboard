package integration;

import com.corn.Application;
import com.corn.controller.ErrorJson;
import com.corn.data.dto.Model;
import com.corn.data.entity.Subscription;
import com.corn.data.repository.SubscriptionRepo;
import com.corn.service.mock.MockEmail;
import com.corn.service.mock.MockMailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class SubscriptionsTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @Value("${corn.frontend.url}")
    private String frontendUrl;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(port);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newSubscriptionTest() {
        String testEmail = "test@test.tst";

        ResponseEntity<Model> response = template.postForEntity(baseApi + "subscription?email=" + testEmail, "", Model.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model<String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Thank you for the subscription. Please check your email for instructions.", body.getValue());

        Optional<Subscription> resultOpt = subscriptionRepo.getByEmail(testEmail);
        assertTrue(resultOpt.isPresent());
        Subscription result = resultOpt.get();

        assertEquals(testEmail, result.getEmail());
        String hash = result.getHash();
        assertNotNull(hash);

        assertFalse(MockMailService.allThreadKeys().isEmpty());
        checkConfirmationEmail(testEmail, hash);
    }

    @Test
    public void existingSubscriptionTest() throws SQLException {
        String testEmail = "test@test.tst";
        Subscription subscription = new Subscription(testEmail, null); // no hash, subscription confirmed

        dbUtils.addSubscription(subscription);

        ResponseEntity<Model> response = template.postForEntity(baseApi + "subscription?email=" + testEmail, "", Model.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model body = response.getBody();
        assertNotNull(body);
        assertEquals("This email already subscribed to the status page.", body.getValue());
    }

    @Test
    public void invalidEmailSubscriptionTest() {
        String testEmail = randomAlphanumeric(10);
        ResponseEntity<ErrorJson> response = template.postForEntity(baseApi + "subscription?email=" + testEmail, "", ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson body = response.getBody();
        assertNotNull(body);
        assertEquals("Subscription email not a valid email address.", body.getMessage());
    }

    @Test
    public void tooLongEmailSubscriptionTest() {
        String testEmail = randomAlphanumeric(257);
        ResponseEntity<ErrorJson> response = template.postForEntity(baseApi + "subscription?email=" + testEmail, "", ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson body = response.getBody();
        assertNotNull(body);
        assertEquals("Subscription email length must be less than 257 characters. ", body.getMessage());
    }

    @Test
    public void nullEmailSubscriptionTest() {
        ResponseEntity<ErrorJson> response = template.postForEntity(baseApi + "subscription?email=", "", ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson body = response.getBody();
        assertNotNull(body);
        assertEquals("Please, provide Subscription email. ", body.getMessage());
    }

    @Test
    public void deleteSubscriptionTest() throws SQLException {
        String testEmail = "test@test.tst";
        Subscription subscription = new Subscription(testEmail, null); // no hash, subscription confirmed

        dbUtils.addSubscription(subscription);
        String check = Sha512DigestUtils.shaHex("salt123456789pepper" + testEmail);

        HttpEntity<String> entity = new HttpEntity<>("");

        ResponseEntity<Model> response = template.exchange(baseApi + "subscription?email=" + testEmail + "&check=" + check, HttpMethod.DELETE, entity,
                Model.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model body = response.getBody();
        assertNotNull(body);
        assertEquals("The email successfully unsubscribed.", body.getValue());
        Optional<Subscription> resultOpt = subscriptionRepo.getByEmail(testEmail);
        assertFalse(resultOpt.isPresent());
    }

    @Test
    public void confirmSubscriptionTest() throws SQLException {
        String testEmail = "test@test.tst";
        Subscription subscription = new Subscription(testEmail, "hash"); // no hash, subscription confirmed

        dbUtils.addSubscription(subscription);


        ResponseEntity<Model> response = template.postForEntity(baseApi + "confirm?hash=hash", "", Model.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model body = response.getBody();
        assertNotNull(body);
        assertEquals("Your email has been successfully confirmed", body.getValue());

        Optional<Subscription> resultOpt = subscriptionRepo.getByEmail(testEmail);
        assertTrue(resultOpt.isPresent());
        Subscription result = resultOpt.get();

        assertEquals(testEmail, result.getEmail());
        assertNull(result.getHash());
    }

    @Test
    public void doubleSubscriptionEmailTest() throws SQLException {
        String testEmail = "test@test.tst";
        String hash = randomAlphanumeric(64);
        Subscription subscription = new Subscription(testEmail, hash); // subscription not confirmed, we send another confirmation email

        dbUtils.addSubscription(subscription);

        ResponseEntity<Model> response = template.postForEntity(baseApi + "subscription?email=" + testEmail, "", Model.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model body = response.getBody();
        assertNotNull(body);
        assertEquals("Thank you for the subscription. Please check your email for instructions.", body.getValue());

        assertFalse(MockMailService.allThreadKeys().isEmpty());
        checkConfirmationEmail(testEmail, hash);
    }

    private void checkConfirmationEmail(String testEmail, String hash) {
        for (long id : MockMailService.allThreadKeys()) {
            List<MockEmail> emails = MockMailService.emails(id);
            assertNotNull(emails);
            assertEquals(1, emails.size());
            MockEmail mockEmail = emails.get(0);
            assertEquals(testEmail, mockEmail.getEmail());
            assertNull(mockEmail.getUnsubscribeUrl());
            assertEquals(MockMailService.CONFIRMATION, mockEmail.getType());
            assertEquals(frontendUrl + "subscribe?hash=" + hash, mockEmail.get("confirmationUrl"));
        }
    }
}
