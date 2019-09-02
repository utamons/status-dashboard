package integration;

import com.corn.Application;
import com.corn.controller.ErrorJson;
import com.corn.data.dto.AnnouncementDTO;
import com.corn.data.dto.Model;
import com.corn.data.dto.UserDTO;
import com.corn.data.entity.Announcement;
import com.corn.data.entity.Subscription;
import com.corn.data.repository.AnnouncementsRepo;
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
import util.TestUtil;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.*;
import static util.TestUtil.assertWithin;
import static util.TestUtil.randomTestAnnouncement;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class AnnouncementTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AnnouncementsRepo announcementsRepo;

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @Value("${corn.frontend.url}")
    private String frontendUrl;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(port);
    }

    /**
     * Getting announcement. Only one and active announcement should be retrieved, if exists.
     *
     * @throws SQLException ex
     */
    @Test
    public void getAnnouncementTest() throws SQLException {
        // 10 inactive, deleted announcements
        for (int i = 0; i < 10; ++i) {
            AnnouncementDTO initial = randomTestAnnouncement().withActive(false);
            dbUtils.addAnnouncement(initial);
        }

        // one active announcement to show
        AnnouncementDTO initial = randomTestAnnouncement().withActive(true);
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = initial.withId(id);

        ResponseEntity<AnnouncementDTO> response = template.getForEntity(baseApi + "announcement", AnnouncementDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnnouncementDTO body = response.getBody();
        assertNotNull(body);

        assertEquals(test.getId(), body.getId());
        assertEquals(test.getHeader(), body.getHeader());
        assertEquals(test.getDescription(), body.getDescription());
        assertTrue(body.isActive());
        assertEquals(test.getCreatedAt(), body.getCreatedAt());
        assertEquals(test.getCreatedBy(), body.getCreatedBy());
        assertEquals(test.getUpdatedAt(), body.getUpdatedAt());
        assertEquals(test.getUpdatedBy(), body.getUpdatedBy());
    }

    /**
     * If there are no active announcements in the database, we get empty body.
     *
     * @throws SQLException ex
     */
    @Test
    public void getNoActiveAnnouncementTest() throws SQLException {
        // 10 inactive, deleted announcements
        for (int i = 0; i < 10; ++i) {
            AnnouncementDTO initial = randomTestAnnouncement().withActive(false);
            dbUtils.addAnnouncement(initial);
        }

        ResponseEntity<AnnouncementDTO> response = template.getForEntity(baseApi + "announcement", AnnouncementDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnnouncementDTO body = response.getBody();
        assertNull(body);
    }

    /**
     * Normal creation of an announcement. No active announcements in the database.
     *
     * @throws SQLException ex
     */
    @Test
    public void newAnnouncementTest() throws SQLException {
        // 10 inactive, deleted announcements
        for (int i = 0; i < 10; ++i) {
            AnnouncementDTO initial = randomTestAnnouncement().withActive(false);
            dbUtils.addAnnouncement(initial);
        }

        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement()
                .withId(999999L) // must be ignored by endpoint
                .withActive(false) // must be ignored by endpoint
                .withCreatedBy("Lorem ipsum") // must be ignored by endpoint
                .withCreatedAt(daysAgo(10)); // must be ignored by endpoint

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<AnnouncementDTO> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, AnnouncementDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnnouncementDTO body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());

        Optional<AnnouncementDTO> testOpt = announcementsRepo
                .findById(body.getId())
                .map(Announcement::toValue);

        assertTrue(testOpt.isPresent());
        AnnouncementDTO result = testOpt.get();

        assertEquals(body.getId(), result.getId());
        assertEquals(test.getHeader(), result.getHeader());
        assertEquals(test.getDescription(), result.getDescription());
        assertTrue(result.isActive());
        assertWithin(currentDate(), result.getCreatedAt(), 500);
        assertEquals(testUser.getUsername(), result.getCreatedBy());
        assertNull(result.getUpdatedAt());
        assertNull(result.getUpdatedBy());
    }

    /**
     * absent header
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullHeaderAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        // empty header
        AnnouncementDTO test = new AnnouncementDTO(
                null, null, null, randomAlphanumeric(10), true, null, null, null, null);


        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Header. ", errorJson.getMessage());
    }

    /**
     * absent header
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyHeaderAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        // empty header
        AnnouncementDTO test = new AnnouncementDTO(
                null, null, "", randomAlphanumeric(10), true, null, null, null, null);


        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Header. ", errorJson.getMessage());
    }

    /**
     * absent description
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullDescriptionAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        // empty description
        AnnouncementDTO test = new AnnouncementDTO(
                null, null, randomAlphanumeric(10), null, true, null, null, null, null);


        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Description. ", errorJson.getMessage());
    }

    /**
     * absent description
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyDescriptionAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        // empty description
        AnnouncementDTO test = new AnnouncementDTO(
                null, null, randomAlphanumeric(10), "", true, null, null, null, null);


        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Description. ", errorJson.getMessage());
    }

    /**
     * header size exceeds database limits
     *
     * @throws SQLException ex
     */
    @Test
    public void addTooLongHeaderAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        // too long header
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withHeader(randomAlphanumeric(2048));

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Header length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * description size exceeds database limits
     *
     * @throws SQLException ex
     */
    @Test
    public void addTooLongDescriptionAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        // too long description
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withDescription(randomAlphanumeric(4096));

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Description length must be less than 2049 characters. ", errorJson.getMessage());
    }

    /**
     * Trying to add second active announcement
     *
     * @throws SQLException ex
     */
    @Test
    public void twoAnnouncementsTest() throws SQLException {

        AnnouncementDTO initial = randomTestAnnouncement().withActive(true); // active announcement
        dbUtils.addAnnouncement(initial);

        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement();

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Active announcement already exists. ", errorJson.getMessage());
    }

    /**
     * Sending emails to subscribers
     *
     * @throws SQLException ex
     */
    @Test
    public void newAnnouncementEmailsTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement();
        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);
        Set<String> subscribers = new HashSet<>();
        final int MAX_SUBSCRIBED = 10;

        for (int i = 0; i < MAX_SUBSCRIBED; ++i) { // subscribed and confirmed
            String testEmail = randomAlphanumeric(10);
            subscriptionRepo.save(new Subscription(testEmail, null));
            subscribers.add(testEmail);
        }

        for (int i = 0; i < 5; ++i) { // subscribed, but not confirmed
            String testEmail = randomAlphanumeric(10);
            subscriptionRepo.save(new Subscription(testEmail, randomAlphanumeric(10)));
        }

        ResponseEntity<AnnouncementDTO> response = template.exchange(baseApi + "announcement", HttpMethod.POST, request, AnnouncementDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertFalse(MockMailService.allThreadKeys().isEmpty());
        for (long id : MockMailService.allThreadKeys()) {
            List<MockEmail> emails = MockMailService.emails(id);
            assertNotNull(emails);
            assertEquals(subscribers.size(), emails.size()); // emails should be sent only to confirmed emails
            for (int i = 0; i < MAX_SUBSCRIBED; ++i) {
                MockEmail mockEmail = emails.get(i);
                assertTrue(subscribers.contains(mockEmail.getEmail()));
                String testEmail = mockEmail.getEmail();

                assertEquals(testEmail, mockEmail.getEmail());
                String check = Sha512DigestUtils.shaHex("salt123456789pepper" + testEmail);
                String unsubscribeUrl = frontendUrl + "subscribe?email=" + testEmail + "&check=" + check;
                assertEquals(unsubscribeUrl, mockEmail.getUnsubscribeUrl());
                assertEquals(MockMailService.ANNOUNCEMENT, mockEmail.getType());
                assertEquals(test.getHeader(), mockEmail.get("header"));
                assertEquals(test.getDescription(), mockEmail.get("announcement"));
            }
        }
    }

    /**
     * Normal updating
     *
     * @throws SQLException ex
     */
    @Test
    public void updateAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement()
                .withUpdatedBy(null)
                .withUpdatedAt(null)
                .withCreatedAt(daysAgo(1));

        long id = dbUtils.addAnnouncement(initial);

        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id)
                .withActive(false) // must be ignored by endpoint
                .withUpdatedAt(daysAgo(10)) // must be ignored by endpoint
                .withUpdatedBy("Lorem ipsum") // must be ignored by endpoint
                .withCreatedBy("Lorem ipsum") // must be ignored by endpoint
                .withCreatedAt(daysAgo(10)); // must be ignored by endpoint;

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<AnnouncementDTO> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, AnnouncementDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnnouncementDTO body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());

        Optional<AnnouncementDTO> testOpt = announcementsRepo
                .findById(body.getId())
                .map(Announcement::toValue);

        assertTrue(testOpt.isPresent());
        AnnouncementDTO result = testOpt.get();

        assertEquals(body.getId(), result.getId());
        assertEquals(test.getHeader(), result.getHeader());
        assertEquals(test.getDescription(), result.getDescription());
        assertTrue(result.isActive());
        assertEquals(initial.getCreatedAt(), result.getCreatedAt());
        assertEquals(initial.getCreatedBy(), result.getCreatedBy());
        assertWithin(currentDate(), result.getUpdatedAt(), 500);
        assertEquals(testUser.getUsername(), result.getUpdatedBy());
    }

    /**
     * Updating absent header
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNoHeaderAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement();
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id).withHeader(null);

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Header. ", errorJson.getMessage());
    }

    /**
     * Updating absent description
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNoDescriptionAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement();
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id).withDescription(null);

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Description. ", errorJson.getMessage());
    }

    /**
     * Too long header
     *
     * @throws SQLException ex
     */
    @Test
    public void updateTooLongHeaderAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement();
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id).withHeader(randomAlphanumeric(2048));

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Header length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long description
     *
     * @throws SQLException ex
     */
    @Test
    public void updateTooLongDescriptionAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement();
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id).withDescription(randomAlphanumeric(4096));

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Description length must be less than 2049 characters. ", errorJson.getMessage());
    }

    /**
     * Trying to update inactive(deleted) announcement
     *
     * @throws SQLException ex
     */
    @Test
    public void updateInactiveAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement().withActive(false);
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id);

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Trying to update deleted announcement. ", errorJson.getMessage());
    }

    /**
     * Trying to update non existent announcement
     *
     * @throws SQLException ex
     */
    @Test
    public void updateAbsentAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(9999L);

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Announcement {9999} not found. ", errorJson.getMessage());
    }

    @Test
    public void deleteAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement()
                .withActive(true)
                .withCreatedBy(testUser.getUsername());
        long id = dbUtils.addAnnouncement(test);

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<Model> response = template.exchange(baseApi + "announcement/" + id, HttpMethod.DELETE, request, Model.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model body = response.getBody();
        assertNotNull(body);
        assertTrue(announcementsRepo.findAllByActive(true).isEmpty());
    }

    @Test
    public void deleteAbsentAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement/9999", HttpMethod.DELETE, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Announcement {9999} not found. ", errorJson.getMessage());
    }

    @Test
    public void deleteInactiveAnnouncementTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        AnnouncementDTO initial = TestUtil.randomTestAnnouncement().withActive(false);
        long id = dbUtils.addAnnouncement(initial);
        AnnouncementDTO test = TestUtil.randomTestAnnouncement().withId(id);

        HttpEntity<AnnouncementDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement/"+id, HttpMethod.DELETE, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Trying to delete already deleted announcement. ", errorJson.getMessage());
    }

}
