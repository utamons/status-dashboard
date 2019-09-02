package integration;

import com.corn.Application;
import com.corn.controller.ErrorJson;
import com.corn.data.dto.*;
import com.corn.data.entity.ServiceComponent;
import com.corn.data.entity.Subscription;
import com.corn.data.repository.ServiceComponentsRepo;
import com.corn.data.repository.ServiceStatusRepo;
import com.corn.data.repository.SubscriptionRepo;
import com.corn.service.mock.MockEmail;
import com.corn.service.mock.MockMailService;
import org.assertj.core.util.Lists;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import util.TestUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class AddStatusTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ServiceComponentsRepo serviceComponentsRepo;

    @Autowired
    private ServiceStatusRepo serviceStatusRepo;

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    private StatusTestHelper statusTestHelper;

    @Value("${corn.frontend.url}")
    private String frontendUrl;

    private final HashMap<Long, ServiceComponentDTO> componentsMap = new HashMap<>();

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(port);
        if (componentsMap.isEmpty()) {
            for (ServiceComponent component : serviceComponentsRepo.findAll()) {
                componentsMap.put(component.getId(), component.toValue());
            }
        }

        if (statusTestHelper == null)
            statusTestHelper = new StatusTestHelper(serviceComponentsRepo, dbUtils);
    }



    /**
     * Adding an accident (maintenance status)
     *
     * @throws SQLException ex
     */
    @Test
    public void addStatusTest() throws SQLException {
        List<ServiceComponentDTO> initialComponents = statusTestHelper.actualComponents();

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());

        Optional<ServiceStatusDTO> actualOpt = serviceStatusRepo
                .findById(body.getId())
                .map(s -> s.toValue(null, null));

        assertTrue(actualOpt.isPresent());
        ServiceStatusDTO actualStatus = actualOpt.get();

        testStatus = statusTestHelper.setActualData( // add test data with actual ids,  current dates and current user
                body.getId(),
                body.getCurrentEvent().getId(),
                testUser.getUsername(),
                null,
                currentDate(),
                null,
                currentDate(),
                testStatus,
                initialComponents
                );

        statusTestHelper.checkStatus(testStatus, actualStatus);
        statusTestHelper.checkEvent(testStatus.getCurrentEvent(), actualStatus.getCurrentEvent());
        statusTestHelper.checkComponents(testStatus.getComponents(), statusTestHelper.actualComponents(), initialComponents);
    }

    /**
     * Null status string
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullStatusStringStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withStatusString(null);
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Header. ", errorJson.getMessage());
    }

    /**
     * Empty status string
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyStatusStringStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withStatusString("");
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Header. ", errorJson.getMessage());
    }

    /**
     * Null status type
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullStatusTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withStatusType(null);
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Issue type. ", errorJson.getMessage());
    }

    /**
     * Empty status type
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyStatusTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withStatusType("");
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Issue type. ", errorJson.getMessage());
    }

    /**
     * Null Description
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullDescriptionTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withDescription(null);
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Description. ", errorJson.getMessage());
    }

    /**
     * Empty Description
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyDescriptionTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withDescription("");
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Description. ", errorJson.getMessage());
    }

    /**
     * Null event
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withCurrentEvent(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Current event data is empty! ", errorJson.getMessage());
    }

    /**
     * Null status string in event
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullStatusStringEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withStatusString(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Sub-Header. ", errorJson.getMessage());
    }

    /**
     * Empty status string in event
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyStatusStringEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withStatusString("");

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Sub-Header. ", errorJson.getMessage());
    }

    /**
     * Null type in event
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullEventTypeEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withEventType(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Event Type. ", errorJson.getMessage());
    }

    /**
     * Empty type in event
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyEventTypeEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withEventType("");

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Event Type. ", errorJson.getMessage());
    }

    /**
     * Null description in event
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullDescriptionEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withDescription(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Sub-Header description. ", errorJson.getMessage());
    }

    /**
     * Empty description in event
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyDescriptionEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withDescription("");

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Sub-Header description. ", errorJson.getMessage());
    }

    /**
     * Null add type
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullUpdateTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0));
        newHistory.add(oldHistory.get(0)
                .withType(null));
        newHistory.add(oldHistory.get(0)
                .withType(null));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message type. ", errorJson.getMessage());
    }

    /**
     * Empty add type
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyUpdateTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0));
        newHistory.add(oldHistory.get(0)
                .withType(""));
        newHistory.add(oldHistory.get(0)
                .withType(""));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message type. ", errorJson.getMessage());
    }

    /**
     * Null add message
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullUpdateMessageTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0));
        newHistory.add(oldHistory.get(0)
                .withMessage(null));
        newHistory.add(oldHistory.get(0)
                .withMessage(null));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message text. ", errorJson.getMessage());
    }

    /**
     * Empty add message
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyUpdateMessageTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0));
        newHistory.add(oldHistory.get(0)
                .withMessage(""));
        newHistory.add(oldHistory.get(0)
                .withMessage(""));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message text. ", errorJson.getMessage());
    }

    /**
     * Empty add history
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullUpdateHistoryTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(null)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Messages. ", errorJson.getMessage());
    }

    /**
     * Empty add history
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyUpdateHistoryTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> emptyHistory = new ArrayList<>();

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(emptyHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Messages. ", errorJson.getMessage());
    }

    /**
     * Too long status string
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongStatusStringStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withStatusString(randomAlphanumeric(2048));
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Header length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long status type
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongStatusTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withStatusType(randomAlphanumeric(65));
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Issue type length must be less than 65 characters. ", errorJson.getMessage());
    }


    /**
     * Too long description
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongDescriptionTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus().withDescription(randomAlphanumeric(1025));
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Description length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long status string in event
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongStatusStringEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withStatusString(randomAlphanumeric(1025));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Sub-Header length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long type in event
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongEventTypeEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withEventType(randomAlphanumeric(65));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Event Type length must be less than 65 characters. ", errorJson.getMessage());
    }

    /**
     * Too long description in event
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongDescriptionEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withDescription(randomAlphanumeric(1025));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Sub-Header description length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long add message
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongUpdateMessageTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0));
        newHistory.add(oldHistory.get(0)
                .withMessage(randomAlphanumeric(1024)));
        newHistory.add(oldHistory.get(0)
                .withMessage(randomAlphanumeric(1025)));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Message text length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long add type
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongUpdateTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0));
        newHistory.add(oldHistory.get(0)
                .withType(randomAlphanumeric(128)));
        newHistory.add(oldHistory.get(0)
                .withType(randomAlphanumeric(129)));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Message type length must be less than 129 characters. ", errorJson.getMessage());
    }

    /**
     * Adding an accident (maintenance status) when we already have an active accident(maintenance)
     *
     * @throws SQLException ex
     */
    @Test
    public void addToExistingStatusTest() throws SQLException {

        statusTestHelper.prepareAccidentStatus(false);

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus();
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Active accident or maintenance already exists and isn't resolved or finished. ", errorJson.getMessage());
    }

    /**
     * trying to add nonexistent component
     *
     * @throws SQLException ex
     */
    @Test
    public void addWrongIdComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getWrongIdComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Component with id=999 does not exist. ", errorJson.getMessage());
    }

    /**
     * trying to set null statusType for a component
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullStatusTypeComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getNullStatusTypeComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Status type of component. ", errorJson.getMessage());
    }

    /**
     * trying to set empty statusType for a component
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyStatusTypeComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getEmptyStatusTypeComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Status type of component. ", errorJson.getMessage());
    }

    /**
     * trying to set null statusString for a component
     *
     * @throws SQLException ex
     */
    @Test
    public void addNullStatusStringComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getNullStatusStringComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Status string of component. ", errorJson.getMessage());
    }

    /**
     * trying to set empty statusString for a component
     *
     * @throws SQLException ex
     */
    @Test
    public void addEmptyStatusStringComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getEmptyStatusStringComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Status string of component. ", errorJson.getMessage());
    }

    /**
     * trying to set too long statusType for a component
     *
     * @throws SQLException ex
     */
    @Test
    public void addTooLongStatusTypeComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getLongStatusTypeComponent(65));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Status type of component length must be less than 65 characters. ", errorJson.getMessage());
    }

    /**
     * trying to set too long statusString for a component
     *
     * @throws SQLException ex
     */
    @Test
    public void addTooLongStatusStringComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withComponents(statusTestHelper.getLongStatusStringComponent(1025));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Status string of component length must be less than 1025 characters. ", errorJson.getMessage());
    }

    @Test
    public void newStatusEmailsTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        Set<String> testEmails = new HashSet<>();
        final int MAX_SUBSCRIBED = 10;

        for (int i = 0; i < MAX_SUBSCRIBED; ++i) {
            String testEmail = randomAlphanumeric(10);
            subscriptionRepo.save(new Subscription(testEmail, null));
            testEmails.add(testEmail);
        }

        // let's add some unconfirmed subscriptions to be sure they won't get emails
        for (int i = 0; i < MAX_SUBSCRIBED; ++i) {
            String testEmail = randomAlphanumeric(10);
            subscriptionRepo.save(new Subscription(testEmail, randomAlphanumeric(64)));
        }

        List<ServiceComponentDTO> initialComponents = new ArrayList<>();
        serviceComponentsRepo.findAll().forEach(c -> initialComponents.add(c.toValue()));

        List<ServiceComponentDTO> testComponents = initialComponents.stream()
                .map(c -> c.withStatusString("Unavailable"))
                .map(c -> c.withStatusType("partial"))
                .collect(Collectors.toList());

        List<EventUpdateDTO> testUpdates = Lists.newArrayList(TestUtil.randomEventUpdate(null));

        ServiceEventDTO testEvent = TestUtil.randomServiceEvent(null, testUpdates);

        ServiceStatusDTO testStatus = TestUtil.randomServiceStatus(testEvent, testComponents, null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "status", HttpMethod.POST, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());

        Optional<ServiceStatusDTO> testOpt = serviceStatusRepo
                .findById(body.getId())
                .map(s -> s.toValue(null, null));

        assertTrue(testOpt.isPresent());
        ServiceStatusDTO result = testOpt.get();

        ServiceEventDTO resultEvent = result.getCurrentEvent();


        assertFalse(MockMailService.allThreadKeys().isEmpty());
        for (long id : MockMailService.allThreadKeys()) {
            List<MockEmail> emails = MockMailService.emails(id);
            assertNotNull(emails);
            assertEquals(testEmails.size(), emails.size());
            for (int i = 0; i < MAX_SUBSCRIBED; ++i) {
                MockEmail mockEmail = emails.get(i);
                assertTrue(testEmails.contains(mockEmail.getEmail()));
                String testEmail = mockEmail.getEmail();

                assertEquals(testEmail, mockEmail.getEmail());
                assertEquals(MockMailService.STATUS, mockEmail.getType());
                assertEquals(result.getStatusType(), mockEmail.get("statusType"));
                assertEquals(result.getStatusString(), mockEmail.get("statusString"));
                assertEquals(resultEvent.getComponentsString(), mockEmail.get("componentsString"));
                assertEquals(result.getDescription(), mockEmail.get("updateMessage"));
            }
        }
    }
}

