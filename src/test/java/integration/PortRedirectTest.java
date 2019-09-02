package integration;

import com.corn.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_ports")
@ContextConfiguration(classes = Application.class)
public class PortRedirectTest extends BaseControllersTest {

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(8080);
    }


    /**
     *    Any request to 8080 port is gonna be redirected to https and 8443 ports.
     */
    @Test
    public void portsRedirectTest() {
            ResponseEntity<String> response = template.getForEntity(base + "/index.html", String.class);
            assertEquals(302,response.getStatusCodeValue());
            HttpHeaders headers = response.getHeaders();
            List<String> loc = headers.get("Location");
            assertNotNull(loc);
            assertEquals(1,loc.size());
            assertEquals("https://localhost:8443/index.html",loc.get(0));
    }
}
