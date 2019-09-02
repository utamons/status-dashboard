package integration;

import com.corn.Application;
import com.corn.controller.ErrorJson;
import com.corn.data.dto.*;
import com.corn.data.entity.ServiceComponent;
import com.corn.data.entity.ServiceEvent;
import com.corn.data.entity.Subscription;
import com.corn.data.repository.ServiceComponentsRepo;
import com.corn.data.repository.ServiceEventsRepo;
import com.corn.data.repository.ServiceStatusRepo;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import util.TestUtil;

import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class UpdateStatusTest extends BaseControllersTest {

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

    @Autowired
    private ServiceEventsRepo serviceEventsRepo;


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


    @Test
    public void updateStatusTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        List<ServiceComponentDTO> initialComponents = statusTestHelper.actualComponents();

        // Initial data  - putting it into database

        ServiceStatusDTO initialStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO initialEvent = initialStatus.getCurrentEvent();

        long statusId = initialStatus.getId();
        long eventId = initialEvent.getId();
        List<EventUpdateDTO> history = initialEvent.getHistory();

        // Updated data

        ServiceEventDTO testEvent = statusTestHelper.getAccidentEvent().withId(eventId).withHistory(history);
        ServiceStatusDTO testStatus =
                statusTestHelper.getAccidentStatus()
                        .withId(statusId)
                        .withCurrentEvent(testEvent);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        Optional<ServiceStatusDTO> testOpt = serviceStatusRepo
                .findById(body.getId())
                .map(s -> s.toValue(null, null));

        assertTrue(testOpt.isPresent());
        ServiceStatusDTO actualStatus = testOpt.get();

        ServiceEventDTO actualEvent = actualStatus.getCurrentEvent();
        assertNotNull(actualEvent);

        testStatus = statusTestHelper.setActualData( // update test data with actual ids,  update dates and updater user
                body.getId(),
                body.getCurrentEvent().getId(),
                initialEvent.getCreatedBy(),
                testUser.getUsername(),
                initialEvent.getCreatedAt(),
                currentDate(),
                initialEvent.getEventDate(),
                testStatus,
                initialComponents);

        statusTestHelper.checkStatus(testStatus, actualStatus);
        statusTestHelper.checkEvent(testStatus.getCurrentEvent(), actualStatus.getCurrentEvent());
        statusTestHelper.checkComponents(testStatus.getComponents(), statusTestHelper.actualComponents(), initialComponents);

    }

    /**
     * We're trying to remove,update and update messages in a history.
     *
     * @throws SQLException ex
     */
    @Test
    public void updateMessagesHistoryTest() throws SQLException {
        Random random = new Random();
        final int TO_REMOVE = random.nextInt(100) + 10;
        final int TO_UPDATE = random.nextInt(100) + 10;
        final int TO_ADD = random.nextInt(100) + 10;

        UserDTO testUser = TestUtil.randomUser(null);

        // Initial data  - putting it into database

        ServiceStatusDTO initialStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO initialEvent = initialStatus.getCurrentEvent();

        // Existing messages ( 1 comes from prepareAccidentStatus() )
        List<EventUpdateDTO> initialHistory = statusTestHelper.prepareAdditionalHistory(initialEvent, TO_REMOVE + TO_UPDATE - 1);
        List<EventUpdateDTO> testHistory = new ArrayList<>();
        List<EventUpdateDTO> updatedHistory = new ArrayList<>();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        // Okay, updating the initialHistory.

        // Removing and updating
        for (int i = TO_REMOVE - 1; i < TO_REMOVE + TO_UPDATE; ++i) {
            EventUpdateDTO message = initialHistory.get(i)
                    .withMessage(randomAlphanumeric(1024))
                    .withType(randomAlphanumeric(128));

            testHistory.add(message);
            updatedHistory.add(message);
        }

        // Adding
        for (int i = 0; i < TO_ADD; ++i) {
            EventUpdateDTO message = TestUtil.randomEventUpdate(null);
            testHistory.add(message);
            newHistory.add(message);
        }


        long statusId = initialStatus.getId();
        long eventId = initialEvent.getId();

        ServiceEventDTO testEvent = statusTestHelper.getAccidentEvent().withId(eventId).withHistory(testHistory);
        ServiceStatusDTO testStatus = statusTestHelper.getAccidentStatus()
                .withId(statusId)
                .withCurrentEvent(testEvent);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        Optional<ServiceStatusDTO> testOpt = serviceStatusRepo
                .findById(body.getId())
                .map(s -> s.toValue(null, null));

        assertTrue(testOpt.isPresent());
        ServiceStatusDTO actualStatus = testOpt.get();

        ServiceEventDTO actualEvent = actualStatus.getCurrentEvent();
        assertNotNull(actualEvent);
        List<EventUpdateDTO> actualHistory = actualEvent.getHistory();
        assertNotNull(actualHistory);

        testHistory.clear(); // recreating reference history from scratch, updating it with actual data

        for (EventUpdateDTO u : updatedHistory) {
            testHistory.add(
                    u
                            .withUpdatedAt(actualEvent.getUpdatedAt())
                            .withUpdatedBy(actualEvent.getUpdatedBy())
            );
        }

        for (EventUpdateDTO u : newHistory) {
            testHistory.add(
                    u
                            .withEventId(actualEvent.getId())
                            .withUpdatedAt(null)
                            .withUpdatedBy(null)
                            .withCreatedBy(actualEvent.getUpdatedBy())
                            .withCreatedAt(actualEvent.getUpdatedAt())
            );
        }

        statusTestHelper.checkUpdatesHistory(testHistory, actualHistory);
    }

    /**
     * Null status string
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNullStatusStringStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withStatusString(null);
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyStatusStringStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withStatusString("");
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullStatusTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withStatusType(null);
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyStatusTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withStatusType("");
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullDescriptionTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withDescription(null);
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyDescriptionTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withDescription("");
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withCurrentEvent(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullStatusStringEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withStatusString(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyStatusStringEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withStatusString("");

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullEventTypeEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withEventType(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyEventTypeEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withEventType("");

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullDescriptionEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withDescription(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyDescriptionEventTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withDescription("");

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Sub-Header description. ", errorJson.getMessage());
    }

    /**
     * Null update type
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNullUpdateTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0).withType(null));
        newHistory.add(oldHistory.get(0)
                .withType(null));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message type. ", errorJson.getMessage());
    }

    /**
     * Empty update type
     *
     * @throws SQLException ex
     */
    @Test
    public void updateEmptyUpdateTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0).withType(""));
        newHistory.add(oldHistory.get(0)
                .withType(""));
        newHistory.add(oldHistory.get(0)
                .withType(""));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message type. ", errorJson.getMessage());
    }

    /**
     * Null update message
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNullUpdateMessageTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0).withMessage(null));
        newHistory.add(oldHistory.get(0)
                .withMessage(null));
        newHistory.add(oldHistory.get(0)
                .withMessage(null));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message text. ", errorJson.getMessage());
    }

    /**
     * Empty update message
     *
     * @throws SQLException ex
     */
    @Test
    public void updateEmptyUpdateMessageTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0).withMessage(""));
        newHistory.add(oldHistory.get(0)
                .withMessage(""));
        newHistory.add(oldHistory.get(0)
                .withMessage(""));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Message text. ", errorJson.getMessage());
    }

    /**
     * Empty update history
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNullUpdateHistoryTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(null)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, provide Messages. ", errorJson.getMessage());
    }

    /**
     * Empty update history
     *
     * @throws SQLException ex
     */
    @Test
    public void updateEmptyUpdateHistoryTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> emptyHistory = new ArrayList<>();

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(emptyHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withStatusString(randomAlphanumeric(2048));
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withStatusType(randomAlphanumeric(65));
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withDescription(randomAlphanumeric(1025));
        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withStatusString(randomAlphanumeric(1025));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withEventType(randomAlphanumeric(65));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO testEvent = testStatus.getCurrentEvent()
                .withDescription(randomAlphanumeric(1025));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(testEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Sub-Header description length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long update message
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongUpdateMessageTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0).withMessage(randomAlphanumeric(1024)));
        newHistory.add(oldHistory.get(0)
                .withMessage(randomAlphanumeric(1024)));
        newHistory.add(oldHistory.get(0)
                .withMessage(randomAlphanumeric(1025)));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Message text length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * Too long update type
     *
     * @throws SQLException ex
     */
    @Test
    public void tooLongUpdateTypeTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        List<EventUpdateDTO> oldHistory = testStatus.getCurrentEvent().getHistory();
        List<EventUpdateDTO> newHistory = new ArrayList<>();

        newHistory.add(oldHistory.get(0).withType(randomAlphanumeric(129)));
        newHistory.add(oldHistory.get(0)
                .withType(randomAlphanumeric(128)));
        newHistory.add(oldHistory.get(0)
                .withType(randomAlphanumeric(129)));


        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus
                .withCurrentEvent(testStatus.getCurrentEvent().withHistory(newHistory)), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Message type length must be less than 129 characters. ", errorJson.getMessage());
    }

    /**
     * wrong status id
     *
     * @throws SQLException ex
     */
    @Test
    public void wrongIdUpdateStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withId(9999L);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Status 9999 not found. ", errorJson.getMessage());
    }

    /**
     * wrong status id
     *
     * @throws SQLException ex
     */
    @Test
    public void nullIdUpdateStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false).withId(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Status id is empty! ", errorJson.getMessage());
    }

    /**
     * trying to update archive status, which should be always unchanged
     *
     * @throws SQLException ex
     */
    @Test
    public void updateArchiveStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(true);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("You cannot update archived status. ", errorJson.getMessage());
    }

    /**
     * trying to update normal status, which should be always unchanged
     *
     * @throws SQLException ex
     */
    @Test
    public void updateNormalStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareNormalStatus();

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("You cannot update normal status. ", errorJson.getMessage());
    }

    /**
     * trying to update nonexistent component
     *
     * @throws SQLException ex
     */
    @Test
    public void updateWrongIdComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getWrongIdComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullStatusTypeComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getNullStatusTypeComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyStatusTypeComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getEmptyStatusTypeComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateNullStatusStringComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getNullStatusStringComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateEmptyStatusStringComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getEmptyStatusStringComponent());

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateTooLongStatusTypeComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getLongStatusTypeComponent(65));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
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
    public void updateTooLongStatusStringComponentsTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false)
                .withComponents(statusTestHelper.getLongStatusStringComponent(1025));

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus, testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Status string of component length must be less than 1025 characters. ", errorJson.getMessage());
    }

    /**
     * wrong event id
     *
     * @throws SQLException ex
     */
    @Test
    public void wrongEventIdUpdateStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO currentEvent = testStatus.getCurrentEvent().withId(9999L);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(currentEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Event with id=9999 does not exist. ", errorJson.getMessage());
    }

    /**
     * null event id
     *
     * @throws SQLException ex
     */
    @Test
    public void nullEventIdUpdateStatusTest() throws SQLException {

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);
        ServiceEventDTO currentEvent = testStatus.getCurrentEvent().withId(null);

        HttpEntity<ServiceStatusDTO> request = this.getAuthEntity(testStatus.withCurrentEvent(currentEvent), testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "status", HttpMethod.PUT, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Event id is empty! ", errorJson.getMessage());
    }

    /**
     * wrong resolve Id
     *
     * @throws SQLException ex
     */
    @Test
    public void resolveWrongIdTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);

        HttpEntity<String> request = this.getAuthEntity("", testUser);
        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "resolve/9999", HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Status 9999 not found.", errorJson.getMessage());
    }

    /**
     * trying to resolve already resolved status
     *
     * @throws SQLException ex
     */
    @Test
    public void resolveWrongStatusTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(true);

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "resolve/"+testStatus.getId(), HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("This status is already resolved!", errorJson.getMessage());
    }

    /**
     * trying to resolve current normal status
     *
     * @throws SQLException ex
     */
    @Test
    public void resolveNormalStatusTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareNormalStatus();

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ErrorJson> response = template.exchange(baseApi + "resolve/"+testStatus.getId(), HttpMethod.POST, request, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("This status cannot be resolved!", errorJson.getMessage());
    }


    @Test
    public void resolveTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "resolve/" + testStatus.getId(), HttpMethod.POST, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        Optional<ServiceStatusDTO> testOpt = serviceStatusRepo
                .findById(body.getId())
                .map(s -> s.toValue(null, null));

        assertTrue(testOpt.isPresent());
        ServiceStatusDTO result = testOpt.get();
        assertNull(result.getCurrentEvent());
        assertEquals("Service is operational", result.getStatusString());
        assertEquals("normal", result.getStatusType());
        assertEquals("Welcome to the Service Status Page. " +
                "There you can see current information of the service performance. " +
                "You can bookmark or subscribe to this page for the latest updates.", result.getDescription());
    }

    @Test
    public void resolveAccidentMessageTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "resolve/" + testStatus.getId(), HttpMethod.POST, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        Optional<ServiceEventDTO> testOpt = serviceEventsRepo
                .findById(testStatus.getCurrentEvent().getId())
                .map(ServiceEvent::toValue);

        assertTrue(testOpt.isPresent());
        ServiceEventDTO result = testOpt.get();

        assertTrue(result.isResolved());
        assertEquals(2, result.getHistory().size());
        EventUpdateDTO resultDTO = result.getHistory().get(1);
        assertEquals("Resolved", resultDTO.getType());
        assertEquals("The issue has been resolved.", resultDTO.getMessage());
    }

    @Test
    public void resolveMaintenanceMessageTest() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareMaintenanceStatus(false);

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "resolve/" + testStatus.getId(), HttpMethod.POST, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        Optional<ServiceEventDTO> testOpt = serviceEventsRepo
                .findById(testStatus.getCurrentEvent().getId())
                .map(ServiceEvent::toValue);

        assertTrue(testOpt.isPresent());
        ServiceEventDTO result = testOpt.get();

        assertTrue(result.isResolved());
        assertEquals(2, result.getHistory().size());
        EventUpdateDTO resultDTO = result.getHistory().get(1);
        assertEquals("Update", resultDTO.getType());
        assertEquals("The maintenance has been finished.", resultDTO.getMessage());
    }

    @Test
    public void resolveEmailsTest() throws SQLException {
        Set<String> testEmails = new HashSet<>();
        final int MAX_SUBSCRIBED = 10;

        for (int i = 0; i < MAX_SUBSCRIBED; ++i) {
            String testEmail = randomAlphanumeric(10);
            subscriptionRepo.save(new Subscription(testEmail, null));
            testEmails.add(testEmail);
        }

        UserDTO testUser = TestUtil.randomUser(null);
        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false);

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<ServiceStatusDTO> response = template.exchange(baseApi + "resolve/" + testStatus.getId(), HttpMethod.POST, request, ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        Optional<ServiceStatusDTO> testOpt = serviceStatusRepo
                .findById(body.getId())
                .map(s -> s.toValue(null, null));

        assertTrue(testOpt.isPresent());
        ServiceStatusDTO result = testOpt.get();

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
                assertEquals(testStatus.getCurrentEvent().getComponentsString(), mockEmail.get("componentsString"));
                assertEquals("The issue has been resolved.", mockEmail.get("updateMessage"));
            }
        }
    }
}

