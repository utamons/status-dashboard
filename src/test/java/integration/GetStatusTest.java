package integration;

import com.corn.Application;
import com.corn.data.dto.EventUpdateDTO;
import com.corn.data.dto.ServiceComponentDTO;
import com.corn.data.dto.ServiceEventDTO;
import com.corn.data.dto.ServiceStatusDTO;
import com.corn.data.entity.ServiceComponent;
import com.corn.data.repository.ServiceComponentsRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.Assert.*;
import static util.TestUtil.randomEventUpdate;
import static util.TestUtil.resolvedServiceEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class GetStatusTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ServiceComponentsRepo serviceComponentsRepo;

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
     * Getting accident status (maintenance status has the same structure we test maintenance as well)
     *
     * @throws SQLException ex
     */
    @Test
    public void getAccidentStatusTest() throws SQLException {
        List<ServiceComponentDTO> initialComponents = statusTestHelper.actualComponents();

        for (int i = 0; i < 10; ++i) {
            statusTestHelper.prepareAccidentStatus(true); // 10 archived statuses
        }

        ServiceStatusDTO testStatus = statusTestHelper.prepareAccidentStatus(false); // one active accident

        ResponseEntity<ServiceStatusDTO> response = template.getForEntity(baseApi + "status", ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO actualStatus = response.getBody();
        assertNotNull(actualStatus);

        statusTestHelper.checkStatus(testStatus, actualStatus);
        statusTestHelper.checkEvent(testStatus.getCurrentEvent(), actualStatus.getCurrentEvent());
        statusTestHelper.checkComponentsForGet(testStatus.getComponents(), actualStatus.getComponents(), initialComponents);
    }

    /**
     * Getting normal status
     *
     * @throws SQLException ex
     */
    @Test
    public void getNormalStatusTest() throws SQLException {
        List<ServiceComponentDTO> initialComponents = statusTestHelper.actualComponents();

        for (int i = 0; i < 10; ++i) {
            statusTestHelper.prepareAccidentStatus(true); // 10 archived statuses
        }

        ServiceStatusDTO testStatus = statusTestHelper.prepareNormalStatus(); // current normal status

        ResponseEntity<ServiceStatusDTO> response = template.getForEntity(baseApi + "status", ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO actualStatus = response.getBody();
        assertNotNull(actualStatus);

        statusTestHelper.checkStatus(testStatus, actualStatus);
        assertNull(testStatus.getCurrentEvent());
        statusTestHelper.checkComponents(testStatus.getComponents(), actualStatus.getComponents(), initialComponents);
    }


    /**
     * Test of statuses history and updates histories of every status. Both should be sorted in right order.
     *
     * @throws SQLException ex
     */
    @Test
    public void statusHistoryTest() throws SQLException {
        final int HISTORY_ITEMS = 10;
        final int UPDATES = 20;
        List<ServiceEventDTO> testEvents = new ArrayList<>();

        /* Creating history items in ascending order of dates (reversed order) */
        for (int i = HISTORY_ITEMS; i > 0; --i) {
            Instant eventDate = Clock.systemDefaultZone().instant().minus(i, DAYS);
            ServiceEventDTO testEvent = resolvedServiceEvent(eventDate);
            long id = dbUtils.addServiceEvent(testEvent);
            /* Creating messages history items in descending order of dates (reversed order) */
            List<EventUpdateDTO> messagesHistory = new ArrayList<>();
            for (int j = UPDATES; j > 0; --j) {
                Instant updateDate = eventDate.plus(j, MINUTES);
                EventUpdateDTO testUpdate = randomEventUpdate(id, updateDate);
                long updateId = dbUtils.addEventUpdate(testUpdate);
                messagesHistory.add(testUpdate.withId(updateId));
            }
            // reverse both lists after inserting into database,
            // because we're going to statusTestHelper.check if actual lists are sorted in the right order.
            Collections.reverse(messagesHistory); // we're awaiting messages in ascending order of dates
            testEvents.add(testEvent.withId(id).withHistory(messagesHistory));
        }
        Collections.reverse(testEvents); // we're awaiting events history in descending order of dates

        ResponseEntity<ServiceStatusDTO> response = template.getForEntity(baseApi + "status", ServiceStatusDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ServiceStatusDTO body = response.getBody();
        assertNotNull(body);

        List<ServiceEventDTO> actualEvents = body.getHistory();
        assertEquals(10, actualEvents.size()); // this implementation shows only the first 10 items

        for (int i = 0; i < actualEvents.size(); ++i) {
            ServiceEventDTO actual = actualEvents.get(i);
            ServiceEventDTO test = testEvents.get(i);
            statusTestHelper.checkEvent(test, actual);
        }
    }
}

