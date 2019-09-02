package integration;

import com.corn.Application;
import com.corn.controller.ErrorJson;
import com.corn.data.dto.*;
import com.corn.data.repository.SessionsRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import util.TestUtil;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;

import static com.corn.service.SessionService.MAX_AGE;
import static com.corn.util.Constants.SESSION_ID;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.*;
import static util.TestUtil.assertWithin;
import static util.TestUtil.randomUser;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class AuthenticationTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private SessionsRepo sessionsRepo;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(port);
    }

    /**
     * headers with no authentication token
     *
     * @return HttpEntity for TestRestTemplate
     */
    private HttpEntity<String> wrongTokenEntity() {
        return wrongTokenEntity("");
    }

    /**
     * headers with no authentication token
     *
     * @return HttpEntity for TestRestTemplate
     */
    private HttpEntity<String> noTokenEntity() {
        return noTokenEntity("");
    }

    /**
     * headers with no authentication token
     *
     * @param body JSON outbound body of the request
     * @param <T> type of the outbound object
     *
     * @return HttpEntity for TestRestTemplate
     */
    private <T> HttpEntity<T> noTokenEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    /**
     * headers with not existing authentication token
     *
     * @param body JSON outbound body of the request
     * @param <T> type of the outbound object
     *
     * @return HttpEntity for TestRestTemplate
     */
    private <T> HttpEntity<T> wrongTokenEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(SESSION_ID, randomAlphanumeric(64)); // wrong token
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    /**
     * checks response for HTTP 403 Forbidden.
     *
     * @param response response to check
     */
    private void forbiddenCheck(ResponseEntity<ErrorJson> response) {
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("No message available", errorJson.getMessage());
    }

    /**
     * checks response for HTTP 401 Unauthorized.
     *
     * @param response response to check
     */
    private void unauthorizedCheck(ResponseEntity<ErrorJson> response) {
        /*
           Tests might get 400 BAD_REQUEST instead of 401 UNAUTHORIZED. This means broken authentication.
           Normally they should pass with 401 UNAUTHORIZED. 400 BAD_REQUEST means completely opened for everyone endpoint.
        */
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Unauthorized", errorJson.getMessage());
    }

    /**
     * Successful login, getting SessionDTO.
     *
     * @throws SQLException ex
     */
    @Test
    public void loginTest() throws SQLException {
        UserDTO userDTO = randomUser(null);
        long id = dbUtils.addUser(userDTO);
        UserDTO test = userDTO.withId(id);
        Instant testCreatedAt = Clock.systemDefaultZone().instant();
        Instant testExpiredAt = testCreatedAt.plus(MAX_AGE, MINUTES);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> request = new HttpEntity<>(test, headers);

        ResponseEntity<SessionDTO> response = template.exchange(baseApi + "login", HttpMethod.POST, request, SessionDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        SessionDTO body = response.getBody();
        assertNotNull(body);

        assertNotNull(body.getToken()); // we must pass authentication token to frontend
        assertWithin(testCreatedAt, body.getCreatedAt(), 500);
        assertWithin(testExpiredAt, body.getExpiredAt(), 500);

        UserDTO that = body.getUser();
        assertNotNull(that);

        assertEquals(test.getId(), that.getId());
        assertEquals(test.getUsername(), that.getUsername());
        assertEquals(test.getRole(), that.getRole());
        assertEquals(test.getFirstName(), that.getFirstName());
        assertEquals(test.getLastName(), that.getLastName());
        assertNull(that.getPassword()); // we don't pass neither password, nor password hash
    }

    /**
     * no username and password
     */
    @Test
    public void loginEmptyCredentialsTest() {
        UserDTO test = new UserDTO(null, null, null, null, null, null, true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> request = new HttpEntity<>(test, headers);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "login", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide username. Please, provide password. " +
                "", errorJson.getMessage());
    }

    /**
     * wrong username and password
     */
    @Test
    public void loginWrongCredentialsTest() {
        UserDTO userDTO = randomUser(null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> request = new HttpEntity<>(userDTO, headers);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "login", HttpMethod.POST, request, ErrorJson.class);
        forbiddenCheck(response);
    }

    /**
     * inactive (deleted) user tries to login
     */
    @Test
    public void loginInactiveUserTest() throws SQLException {
        UserDTO test = new UserDTO(
                null,
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                "maintainer",
                false
        );

        dbUtils.addUser(test);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> request = new HttpEntity<>(test, headers);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "login", HttpMethod.POST, request, ErrorJson.class);
        forbiddenCheck(response);
    }

    /**
     *  user with wrong role (not a maintainer) tries to login
     */
    @Test
    public void loginWrongRoleTest() throws SQLException {
        UserDTO test = new UserDTO(
                null,
                randomAlphabetic(7),
                randomAlphabetic(8),
                randomAlphabetic(7),
                randomAlphabetic(7),
                randomAlphabetic(7),
                true
        );

        dbUtils.addUser(test);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> request = new HttpEntity<>(test, headers);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "login", HttpMethod.POST, request, ErrorJson.class);
        forbiddenCheck(response);
    }

    /**
     * Successful logout
     *
     * @throws SQLException ex
     */
    @Test
    public void logoutTest() throws SQLException {
        String token = randomAlphanumeric(64);
        HttpEntity<String> entity = getAuthEntity(token);

        assertNotNull(sessionsRepo.findByToken(token));

        ResponseEntity<Model> response = template.exchange(baseApi + "login", HttpMethod.DELETE, entity, Model.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNull(sessionsRepo.findByToken(token)); // session is removed
    }

    /*
        Group of tests against wrong or absent authentication token =========================================================================
     */

    @Test
    public void logoutWrongTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "login", HttpMethod.DELETE, wrongTokenEntity(), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void logoutNoTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "login", HttpMethod.DELETE, noTokenEntity(), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void newAnnouncementWrongTokenTest() {
        AnnouncementDTO test = TestUtil.randomTestAnnouncement();
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, wrongTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void newAnnouncementNoTokenTest() {
        AnnouncementDTO test = TestUtil.randomTestAnnouncement();
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.POST, noTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void updateAnnouncementWrongTokenTest() {
        AnnouncementDTO test = TestUtil.randomTestAnnouncement();
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, wrongTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void updateAnnouncementNoTokenTest() {
        AnnouncementDTO test = TestUtil.randomTestAnnouncement();
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement", HttpMethod.PUT, noTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void deleteAnnouncementWrongTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement/999", HttpMethod.DELETE, wrongTokenEntity(), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void deleteAnnouncementNoTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "announcement/999", HttpMethod.DELETE, noTokenEntity(), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void newStatusWrongTokenTest() {
        ServiceStatusDTO test = TestUtil.randomServiceStatus(null, null, null);
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, wrongTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void newStatusNoTokenTest() {
        ServiceStatusDTO test = TestUtil.randomServiceStatus(null, null, null);
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, noTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void updateStatusWrongTokenTest() {
        ServiceStatusDTO test = TestUtil.randomServiceStatus(null, null, null);
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, wrongTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void updateStatusNoTokenTest() {
        ServiceStatusDTO test = TestUtil.randomServiceStatus(null, null, null);
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, noTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void resolveWrongTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "resolve/999", HttpMethod.POST, wrongTokenEntity(), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void resolveNoTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "resolve/999", HttpMethod.POST, noTokenEntity(), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void issueReportsWrongTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "/issueReport?start=0&size=10&processed=true", HttpMethod.GET, wrongTokenEntity(),
                ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void issueReportsNoTokenTest() {
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "/issueReport?start=0&size=10&processed=true", HttpMethod.GET, noTokenEntity(),
                ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void updateIssueReportWrongTokenTest() {
        IssueReportDTO test = TestUtil.randomIssueReport();
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "/issueReport", HttpMethod.PUT, wrongTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

    @Test
    public void updateIssueReportNoTokenTest() {
        IssueReportDTO test = TestUtil.randomIssueReport();
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "/issueReport", HttpMethod.PUT, noTokenEntity(test), ErrorJson.class);
        unauthorizedCheck(response);
    }

}
