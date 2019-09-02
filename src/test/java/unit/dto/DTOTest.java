package unit.dto;

import com.corn.data.dto.*;
import com.corn.util.Constants;
import org.junit.Test;
import util.TestUtil;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.corn.util.Utils.randomBoolean;
import static com.corn.util.Utils.randomInstant;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang.math.RandomUtils.nextLong;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DTOTest {

    @Test(expected=IllegalAccessException.class)
    public void testConstructorPrivate() throws Exception {
        Constants.class.newInstance();
        final Constructor constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
        fail("Utility class constructor should be private");
    }

    @Test
    public void announcementDTOTest() {
        final Long id = nextLong();
        final Instant date = randomInstant();
        final String header = randomAlphabetic(10);
        final String description = randomAlphabetic(10);
        final boolean active = randomBoolean();
        final Instant createdAt = randomInstant();
        final String createdBy = randomAlphabetic(10);
        final Instant updatedAt = randomInstant();
        final String updatedBy = randomAlphabetic(10);

        AnnouncementDTO test = new AnnouncementDTO(
                id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy
        );

        assertEquals(id, test.getId());
        assertEquals(date, test.getDate());
        assertEquals(header, test.getHeader());
        assertEquals(description, test.getDescription());
        assertEquals(active, test.isActive());
        assertEquals(createdAt, test.getCreatedAt());
        assertEquals(createdBy, test.getCreatedBy());
        assertEquals(updatedBy, test.getUpdatedBy());
        assertEquals(updatedAt, test.getUpdatedAt());

        AnnouncementDTO test1 = new AnnouncementDTO(
                id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy
        );

        assertEquals(test,test1);
    }

    @Test
    public void eventUpdateDTOTest() {
        final Long id = nextLong();
        final Instant date = randomInstant();
        final String type = randomAlphabetic(10);
        final Long eventId = nextLong();
        final String message = randomAlphabetic(10);
        final Instant createdAt = randomInstant();
        final String createdBy = randomAlphabetic(10);
        final Instant updatedAt = randomInstant();
        final String updatedBy = randomAlphabetic(10);

        EventUpdateDTO test = new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);

        assertEquals(id, test.getId());
        assertEquals(date, test.getDate());
        assertEquals(type, test.getType());
        assertEquals(eventId, test.getEventId());
        assertEquals(message, test.getMessage());
        assertEquals(createdAt, test.getCreatedAt());
        assertEquals(createdBy, test.getCreatedBy());
        assertEquals(updatedBy, test.getUpdatedBy());
        assertEquals(updatedAt, test.getUpdatedAt());

        EventUpdateDTO test1 = new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);

        assertEquals(test,test1);
    }

    @Test
    public void issueReportDTOTest() {
        final Long id = nextLong();
        final String reportText = randomAlphabetic(10);
        final String remarkText = randomAlphabetic(10);
        final Instant createdAt = randomInstant();
        final boolean processed = randomBoolean();
        final Instant processedAt = randomInstant();
        final String processedBy = randomAlphabetic(10);

        IssueReportDTO test = new IssueReportDTO(id,reportText,remarkText,createdAt,processed,processedAt,processedBy);

        assertEquals(id, test.getId());
        assertEquals(reportText, test.getReportText());
        assertEquals(remarkText, test.getRemarkText());
        assertEquals(createdAt, test.getCreatedAt());
        assertEquals(processed, test.isProcessed());
        assertEquals(processedAt, test.getProcessedAt());
        assertEquals(processedBy, test.getProcessedBy());

        IssueReportDTO test1 = new IssueReportDTO(id,reportText,remarkText,createdAt,processed,processedAt,processedBy);

        assertEquals(test,test1);
    }

    @Test
    public void pageDTOTest() {
        final List<String> content = new ArrayList<>();
        content.add(randomAlphabetic(10));

        final int totalPages = nextInt();
        final long totalElements = nextLong();

        PageDTO<String> test = new PageDTO<>(content,totalPages,totalElements);

        assertEquals(content, test.getContent());
        assertEquals(totalElements,test.getTotalElements());
        assertEquals(totalPages,test.getTotalPages());
    }

    @Test
    public void serviceComponentDTOTest() {
        final Long id = nextLong();
        final String name = randomAlphabetic(10);
        final String statusString = randomAlphabetic(10);
        final String statusType = randomAlphabetic(10);
        final Instant updatedAt = randomInstant();
        final String updatedBy = randomAlphabetic(10);

        ServiceComponentDTO test = new ServiceComponentDTO(id,name,statusString,statusType,updatedAt,updatedBy);

        assertEquals(id, test.getId());
        assertEquals(name, test.getName());
        assertEquals(statusString, test.getStatusString());
        assertEquals(statusType, test.getStatusType());
        assertEquals(updatedAt, test.getUpdatedAt());
        assertEquals(updatedBy, test.getUpdatedBy());

        ServiceComponentDTO test1 = new ServiceComponentDTO(id,name,statusString,statusType,updatedAt,updatedBy);

        assertEquals(test,test1);
    }

    @Test
    public void serviceEventDTOTest() {
        final Long id = nextLong();
        final Instant eventDate = randomInstant();
        final String statusString = randomAlphabetic(10);
        final String eventType = randomAlphabetic(10);
        final String description =  randomAlphabetic(10);
        final String componentsString = randomAlphabetic(10);
        boolean resolved = randomBoolean();
        final Instant createdAt = randomInstant();
        final String createdBy = randomAlphabetic(10);
        final Instant updatedAt = randomInstant();
        final String updatedBy = randomAlphabetic(10);
        final List<EventUpdateDTO> history = newArrayList(TestUtil.randomEventUpdate(null));

        ServiceEventDTO test = new ServiceEventDTO(
                id,eventDate,statusString,eventType,description,componentsString,
                resolved,createdAt,createdBy,updatedAt,updatedBy,history);

        assertEquals(id, test.getId());
        assertEquals(eventDate, test.getEventDate());
        assertEquals(statusString, test.getStatusString());
        assertEquals(eventType, test.getEventType());
        assertEquals(description, test.getDescription());
        assertEquals(componentsString, test.getComponentsString());
        assertEquals(resolved, test.isResolved());
        assertEquals(createdAt, test.getCreatedAt());
        assertEquals(createdBy, test.getCreatedBy());
        assertEquals(updatedAt, test.getUpdatedAt());
        assertEquals(updatedBy, test.getUpdatedBy());
        assertEquals(history, test.getHistory());

        ServiceEventDTO test1 = new ServiceEventDTO(
                id,eventDate,statusString,eventType,description,componentsString,
                resolved,createdAt,createdBy,updatedAt,updatedBy,history);

        assertEquals(test,test1);

    }

    @Test
    public void serviceStatusDTOTest() {
        final Long id = nextLong();
        final String statusString = randomAlphabetic(10);
        final String statusType = randomAlphabetic(10);
        final String description = randomAlphabetic(10);
        final ServiceEventDTO currentEvent = TestUtil.randomServiceEvent(null);
        final boolean current = randomBoolean();
        final Instant updatedAt = randomInstant();
        final String updatedBy = randomAlphabetic(10);
        final List<ServiceComponentDTO> components = newArrayList(TestUtil.randomServiceComponent());
        final List<ServiceEventDTO> history = newArrayList(TestUtil.randomServiceEvent(null));

        ServiceStatusDTO test = new ServiceStatusDTO(
          id,statusString,statusType,description,currentEvent,current,updatedAt,updatedBy,components,history
        );

        assertEquals(id, test.getId());
        assertEquals(statusString, test.getStatusString());
        assertEquals(statusType, test.getStatusType());
        assertEquals(description, test.getDescription());
        assertEquals(currentEvent, test.getCurrentEvent());
        assertEquals(current, test.isCurrent());
        assertEquals(updatedAt, test.getUpdatedAt());
        assertEquals(updatedBy, test.getUpdatedBy());
        assertEquals(components, test.getComponents());
        assertEquals(history, test.getHistory());

        ServiceStatusDTO test1 = new ServiceStatusDTO(
                id,statusString,statusType,description,currentEvent,current,updatedAt,updatedBy,components,history
        );

        assertEquals(test,test1);
    }

    @Test
    public void sessionDTOTest() {
        final Long id = nextLong();
        final UserDTO user = TestUtil.randomUser(null);
        final Instant createdAt = randomInstant();
        final Instant expiredAt = randomInstant();
        final String token = randomAlphabetic(10);

        SessionDTO test = new SessionDTO(id,user,createdAt,expiredAt,token);

        assertEquals(id, test.getId());
        assertEquals(user, test.getUser());
        assertEquals(createdAt, test.getCreatedAt());
        assertEquals(expiredAt, test.getExpiredAt());
        assertEquals(token, test.getToken());

        SessionDTO test1 = new SessionDTO(id,user,createdAt,expiredAt,token);

        assertEquals(test,test1);
    }

    @Test
    public void userDTOTest() {
        final Long id = nextLong();
        final String username = randomAlphabetic(10);
        final String password = randomAlphabetic(10);
        final String firstName = randomAlphabetic(10);
        final String lastName = randomAlphabetic(10);
        final String role = randomAlphabetic(10);
        final boolean active = randomBoolean();

        UserDTO test = new UserDTO(id,username,password,firstName,lastName,role,active);

        assertEquals(id, test.getId());
        assertEquals(username, test.getUsername());
        assertEquals(password, test.getPassword());
        assertEquals(firstName, test.getFirstName());
        assertEquals(lastName, test.getLastName());
        assertEquals(role, test.getRole());
        assertEquals(active, test.isActive());

        UserDTO test1 = new UserDTO(id,username,password,firstName,lastName,role,active);

        assertEquals(test,test1);
    }

    @Test
    public void modelTest() {
        Object x = new Object();
        Model<Object> test = new Model<>(x);
        assertEquals(x,test.getValue());
    }
}
