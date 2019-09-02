package integration;

import com.corn.Application;
import com.corn.controller.ErrorJson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class CommonControllersTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(port);
    }

    /**
     * Frontend is served as a static resource of backend, and frontend urls actually consider as backend urls
     * by Spring Boot, it's trying to find these urls in its controllers, and returns HTTP 404 to user.
     *
     * For handling frontend urls properly, we must catch and redirect all frontend urls to /index.html, which
     * is actually frontend code. Frontend code in turn, should perform internal navigation.
     *
     * There we're checking this redirection.
     */
    @Test
    public void angularControllerTest() {
        final String[] frontendUrls = {"", "/home", "/history", "/report", "/subscribe", "/login", "/maintain"};
        for (String url : frontendUrls) {
            ResponseEntity<String> response = template.getForEntity(base + url, String.class);
            assertEquals(302,response.getStatusCodeValue());
            HttpHeaders headers = response.getHeaders();
            List<String> loc = headers.get("Location");
            assertNotNull(loc);
            assertEquals(1,loc.size());
            assertTrue(loc.get(0).contains("index.html"));
        }
    }

    /**
     * This is a test for custom errors controller via special /test endpoint.
     */
    @Test
    public void errorControllerTest() throws SQLException {
        HttpEntity<String> entity = getAuthEntity(randomAlphanumeric(64));
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "test?param=error", HttpMethod.GET, entity, ErrorJson.class);
        ErrorJson body = response.getBody();
        assertNotNull(body);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals(new Integer(400), body.getStatus());
        assertEquals("Test error", body.getMessage());
        assertEquals("Bad Request", body.getError());
        assertNotNull(body.getTimeStamp());
    }
}
