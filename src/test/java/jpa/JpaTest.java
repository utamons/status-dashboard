package jpa;

import com.corn.Application;
import com.corn.data.dto.*;
import com.corn.data.entity.*;
import com.corn.data.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import util.DbUtils;
import util.TestUtil;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
// @DataJpaTest works using transactions, and doesn't see records, created with a different connection in DbUtils during the transaction.
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class JpaTest {

    @Autowired
    AnnouncementsRepo announcementsRepo;

    @Autowired
    UsersRepo usersRepo;

    @Autowired
    IssueReportRepo issueReportRepo;

    @Autowired
    ServiceComponentsRepo serviceComponentsRepo;

    @Autowired
    SubscriptionRepo subscriptionRepo;

    @Autowired
    ServiceEventsRepo serviceEventsRepo;

    @Autowired
    EventUpdatesRepo eventUpdatesRepo;

    @Autowired
    ServiceStatusRepo serviceStatusRepo;

    @Autowired
    SessionsRepo sessionsRepo;

    private DbUtils dbUtils;

    // todo test database constraints

    @Before
    public void setUp() throws ClassNotFoundException {

        if (dbUtils == null) {
            dbUtils = new DbUtils();
        }
    }

    @Test
    public void announcementTest() throws SQLException {
        AnnouncementDTO av = TestUtil.randomTestAnnouncement();
        long id = dbUtils.addAnnouncement(av);

        Optional<Announcement> optAnn = announcementsRepo.findById(id);
        assertTrue(optAnn.isPresent());
        Announcement result = optAnn.get();
        assertEquals(av.getDate(), result.getDate());
        assertEquals(av.getHeader(), result.getHeader());
        assertEquals(av.getDescription(), result.getDescription());
        assertEquals(av.isActive(), result.isActive());
        assertEquals(av.getCreatedAt(), result.getCreatedAt());
        assertEquals(av.getCreatedBy(), result.getCreatedBy());
        assertEquals(av.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(av.getUpdatedBy(), result.getUpdatedBy());

        List<Announcement> announcements = announcementsRepo.findAllByActive(true);
        assertTrue(announcements.size() > 0);
        optAnn = announcements.stream().filter(a -> a.getId().equals(id)).findFirst();
        assertTrue(optAnn.isPresent());
        Announcement found = optAnn.get();
        assertEquals(result, found);
    }

    @Test
    public void userTest() throws SQLException {
        UserDTO u = TestUtil.randomUser(null);
        long id = dbUtils.addUser(u);

        Optional<User> optUser = usersRepo.findById(id);
        assertTrue(optUser.isPresent());
        User result = optUser.get();
        assertEquals(u.getUsername(), result.getUsername());
        assertEquals(u.getFirstName(), result.getFirstName());
        assertEquals(Sha512DigestUtils.shaHex(u.getPassword()), result.getPassword());
        assertEquals(u.isActive(), result.isActive());
        assertEquals(u.getRole(), result.getRole());

        User found = usersRepo.findByUsernameAndPassword(u.getUsername(), Sha512DigestUtils.shaHex(u.getPassword()));
        assertEquals(result, found);
    }

    @Test
    public void issueReportTest() throws SQLException {
        IssueReportDTO ir = TestUtil.randomIssueReport();
        long id = dbUtils.addIssueReport(ir);

        Optional<IssueReport> optReport = issueReportRepo.findById(id);
        assertTrue(optReport.isPresent());
        IssueReport result = optReport.get();
        assertEquals(ir.getReportText(), result.getReportText());
        assertEquals(ir.getRemarkText(), result.getRemarkText());
        assertEquals(ir.isProcessed(), result.isProcessed());
        assertEquals(ir.getCreatedAt(), result.getCreatedAt());
        assertEquals(ir.getProcessedAt(), result.getProcessedAt());
        assertEquals(ir.getProcessedBy(), result.getProcessedBy());

        List<IssueReport> issueReports = issueReportRepo.findAllByProcessed(true, PageRequest.of(0, 10)).getContent();
        assertTrue(issueReports.size() > 0);
        optReport = issueReports.stream().filter(a -> a.getId().equals(id)).findFirst();
        assertTrue(optReport.isPresent());
        IssueReport found = optReport.get();
        assertEquals(result, found);
    }

    @Test
    public void componentsTest() throws SQLException {
        ServiceComponentDTO sc = TestUtil.randomServiceComponent();
        long id = dbUtils.addServiceComponent(sc);

        Optional<ServiceComponent> optComp = serviceComponentsRepo.findById(id);
        assertTrue(optComp.isPresent());
        ServiceComponent result = optComp.get();
        assertEquals(sc.getStatusType(), result.getStatusType());
        assertEquals(sc.getName(), result.getName());
        assertEquals(sc.getStatusString(), result.getStatusString());
        assertEquals(sc.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(sc.getUpdatedBy(), result.getUpdatedBy());
    }

    @Test
    public void subscriptionsTest() throws SQLException {
        Subscription ss = TestUtil.randomSubscription();
        dbUtils.addSubscription(ss);

        Optional<Subscription> optReport = subscriptionRepo.getByEmail(ss.getEmail());
        assertTrue(optReport.isPresent());
        Subscription result = optReport.get();
        assertEquals(ss.getHash(), result.getHash());
        assertEquals(ss.getEmail(), result.getEmail());

        Subscription found = subscriptionRepo.findTopByHash(ss.getHash());
        assertEquals(result,found);
    }

    @Test
    public void serviceEventTest() throws SQLException {
        ServiceEventDTO se = TestUtil.randomServiceEvent(null);
        long id = dbUtils.addServiceEvent(se);

        Optional<ServiceEvent> optEvent = serviceEventsRepo.findById(id);
        assertTrue(optEvent.isPresent());
        ServiceEvent result = optEvent.get();
        assertEquals(se.getEventDate(), result.getEventDate());
        assertEquals(se.getStatusString(), result.getStatusString());
        assertEquals(se.getEventType(), result.getEventType());
        assertEquals(se.getCreatedAt(), result.getCreatedAt());
        assertEquals(se.getDescription(), result.getDescription());
        assertEquals(se.getComponentsString(), result.getComponentsString());
        assertEquals(se.isResolved(), result.isResolved());
        assertEquals(se.getCreatedBy(), result.getCreatedBy());
        assertEquals(se.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(se.getUpdatedBy(), result.getUpdatedBy());

        List<ServiceEvent> events = serviceEventsRepo.findTop10ByResolvedOrderByEventDateDesc(false);
        assertTrue(events.size() > 0);
        optEvent = events.stream().filter(a -> a.getId().equals(id)).findFirst();
        assertTrue(optEvent.isPresent());
        ServiceEvent found = optEvent.get();
        assertEquals(result, found);
    }

    @Test
    public void eventUpdateTest() throws SQLException {
        ServiceEventDTO se = TestUtil.randomServiceEvent(null);
        long seId = dbUtils.addServiceEvent(se);
        EventUpdateDTO eu = TestUtil.randomEventUpdate(seId);

        long id = dbUtils.addEventUpdate(eu);

        Optional<EventUpdate> optE = eventUpdatesRepo.findById(id);
        assertTrue(optE.isPresent());
        EventUpdate result = optE.get();
        assertEquals(eu.getDate(), result.getDate());
        assertEquals(eu.getType(), result.getType());
        assertEquals(eu.getEventId(), result.getEvent().getId());
        assertEquals(eu.getMessage(), result.getMessage());
        assertEquals(eu.getCreatedAt(), result.getCreatedAt());
        assertEquals(eu.getCreatedBy(), result.getCreatedBy());

        List<EventUpdate> events = eventUpdatesRepo.findAllByEvent(result.getEvent());
        assertTrue(events.size() > 0);
        optE = events.stream().filter(a -> a.getId().equals(id)).findFirst();
        assertTrue(optE.isPresent());
        EventUpdate found = optE.get();
        assertEquals(result, found);
    }

    @Test
    public void serviceStatusTest() throws SQLException {
        ServiceEventDTO se = TestUtil.randomServiceEvent(null);
        long seId = dbUtils.addServiceEvent(se);
        ServiceStatusDTO st = TestUtil.randomServiceStatus(seId);

        long id = dbUtils.addServiceStatus(st);

        Optional<ServiceStatus> optE = serviceStatusRepo.findById(id);
        assertTrue(optE.isPresent());
        ServiceStatus result = optE.get();
        assertEquals(st.getCurrentEvent().getId(), result.getCurrentEvent().getId());
        assertEquals(st.getStatusString(), result.getStatusString());
        assertEquals(st.getDescription(), result.getDescription());
        assertEquals(st.getStatusType(), result.getStatusType());
        assertEquals(st.getUpdatedBy(), result.getUpdatedBy());
        assertEquals(st.getUpdatedAt(), result.getUpdatedAt());

        List<ServiceStatus> events = serviceStatusRepo.findAllByCurrent(st.isCurrent());
        assertTrue(events.size() > 0);
        optE = events.stream().filter(a -> a.getId().equals(id)).findFirst();
        assertTrue(optE.isPresent());
        ServiceStatus found = optE.get();
        assertEquals(result, found);
    }

    @Test
    public void userSessionTest() throws SQLException {
        UserDTO user = TestUtil.randomUser(null);
        long uId = dbUtils.addUser(user);
        SessionDTO s = TestUtil.randomSession(uId).withExpiredAt(Instant.now());

        long id = dbUtils.addSession(s);

        Optional<Session> optS = sessionsRepo.findById(id);
        assertTrue(optS.isPresent());
        Session result = optS.get();
        assertEquals(s.getUser().getId(), result.getUser().getId());
        assertEquals(s.getCreatedAt(), result.getCreatedAt());
        assertEquals(s.getExpiredAt(), result.getExpiredAt());
        assertEquals(s.getToken(), result.getToken());

        Session found = sessionsRepo.findByToken(s.getToken());
        assertNotNull(found);
        assertEquals(result, found);

        sessionsRepo.deleteExpired(Instant.now());

        optS = sessionsRepo.findById(id);
        assertFalse(optS.isPresent());
    }
}
