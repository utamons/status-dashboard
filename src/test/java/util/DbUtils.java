package util;

import com.corn.data.dto.*;
import com.corn.data.entity.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.token.Sha512DigestUtils;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author Oleg Zaidullin
 */
public class DbUtils {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DbUtils() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:testData", "sa", "");
    }

    public void cleanAll() throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            int del = 0;
            del += st.executeUpdate("delete from EVENT_UPDATE");
            del += st.executeUpdate("delete from SERVICE_STATUS");
            del += st.executeUpdate("delete from SUBSCRIPTION");
            del += st.executeUpdate("delete from ANNOUNCEMENT");
            del += st.executeUpdate("delete from SERVICE_EVENT");
            del += st.executeUpdate("delete from USER_SESSION");
            del += st.executeUpdate("delete from APP_USER");
            del += st.executeUpdate("delete from ISSUE_REPORT");
            logger.debug("finish, {} record deleted", del);

            st.executeUpdate("update SERVICE_COMPONENT set STATUS_TYPE='normal', STATUS_STRING='Operational'");
            addNormalStatus();
        }
    }

    public void setAllComponentsAccident() throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();

            st.executeUpdate("update SERVICE_COMPONENT set STATUS_TYPE='accident', STATUS_STRING='Unavailable'");
            addNormalStatus();
        }
    }

    private void addNormalStatus() throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_SERVICE_STATUS.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into SERVICE_STATUS " +
                        "(ID, STATUS_STRING, STATUS_TYPE, DESCRIPTION, EVENT_ID, CURRENT_STATUS, UPDATED_AT, UPDATED_BY) VALUES " +
                        "(?,?,?,?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setString(2, "Service is operational");
                pst.setString(3, "normal");
                pst.setString(4, "Welcome to the Service Status Page. There you can see current information of the service performance. You can bookmark or subscribe to this page for the latest updates.");
                pst.setString(5, null);
                pst.setBoolean(6, true);
                pst.setTimestamp(7, null);
                pst.setTimestamp(8, null);

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
            } else
                throw new SQLException("Cannot get next value from SEQ_APP_USER");
        }
    }

    public long addUser(UserDTO userDTO) throws SQLException {
        try (Connection conn = getConnection()) {
            long id = findUser(userDTO);
            if (id == -1) {
                logger.debug("start");
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("select SEQ_APP_USER.nextval from dual");
                if (rs.next()) {
                    id = rs.getLong(1);
                    PreparedStatement pst = conn.prepareStatement("insert into APP_USER (ID,USERNAME,PASSWORD,FIRST_NAME,LAST_NAME,ROLE,ACTIVE)" +
                            " values (?,?,?,?,?,?,?)");

                    pst.setLong(1, id);
                    pst.setString(2, userDTO.getUsername());
                    pst.setString(3, Sha512DigestUtils.shaHex(userDTO.getPassword()));
                    pst.setString(4, userDTO.getFirstName());
                    pst.setString(5, userDTO.getLastName());
                    pst.setString(6, userDTO.getRole());
                    pst.setBoolean(7, userDTO.isActive());

                    int ins = pst.executeUpdate();
                    logger.debug("finish, {} record inserted", ins);
                    return id;
                } else
                    throw new SQLException("Cannot get next value from SEQ_APP_USER");
            }
            return id;
        }
    }

    private long findUser(UserDTO userDTO) throws SQLException {
        long id = -1;
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select ID from APP_USER where USERNAME='" + userDTO.getUsername() + "'");
            if (rs.next()) {
                id = rs.getLong(1);
            }
        }
        return id;
    }

    public void setAuth(String token, Long userId, LocalDateTime expiredAt) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_USER_SESSION.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into USER_SESSION (ID, USER_ID, CREATED_AT, EXPIRED_AT, TOKEN) " +
                        "VALUES (?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setLong(2, userId);
                pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pst.setTimestamp(4, Timestamp.valueOf(expiredAt));
                pst.setString(5, token);

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
            } else
                throw new SQLException("Cannot get next value from SEQ_USER_SESSION");
        }
    }

    public long addAnnouncement(AnnouncementDTO av) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_ANNOUNCEMENT.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into ANNOUNCEMENT " +
                        "(ID, ANNOUNCEMENT_DATE, HEADER, DESCRIPTION, ACTIVE, CREATED_AT, CREATED_BY, UPDATED_AT, UPDATED_BY) values " +
                        "(?,?,?,?,?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setTimestamp(2, nullableTs(av.getDate()));
                pst.setString(3, av.getHeader());
                pst.setString(4, av.getDescription());
                pst.setBoolean(5, av.isActive());
                pst.setTimestamp(6, nullableTs(av.getCreatedAt()));
                pst.setString(7, av.getCreatedBy());
                pst.setTimestamp(8, nullableTs(av.getUpdatedAt()));
                pst.setString(9, av.getUpdatedBy());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_ANNOUNCEMENT");
        }
    }

    public long addIssueReport(IssueReportDTO issueReport) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_ISSUE_REPORT.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into ISSUE_REPORT (ID, REPORT_TEXT, REMARK_TEXT, CREATED_AT, PROCESSED_AT, PROCESSED_BY, PROCESSED) VALUES " +
                        "(?,?,?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setString(2, issueReport.getReportText());
                pst.setString(3, issueReport.getRemarkText());
                pst.setTimestamp(4, nullableTs(issueReport.getCreatedAt()));

                if (issueReport.getProcessedAt() == null)
                    pst.setTimestamp(5, null);
                else
                    pst.setTimestamp(5, nullableTs(issueReport.getProcessedAt()));

                pst.setString(6, issueReport.getProcessedBy());
                pst.setBoolean(7, issueReport.isProcessed());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_ISSUE_REPORT");
        }
    }

    public long addServiceComponent(ServiceComponentDTO serviceComponent) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_SERVICE_COMPONENT.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into SERVICE_COMPONENT (ID, NAME, STATUS_STRING, STATUS_TYPE, UPDATED_AT, UPDATED_BY) VALUES " +
                        "(?,?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setString(2, serviceComponent.getName());
                pst.setString(3, serviceComponent.getStatusString());
                pst.setString(4, serviceComponent.getStatusType());
                pst.setTimestamp(5, nullableTs(serviceComponent.getUpdatedAt()));
                pst.setString(6, serviceComponent.getUpdatedBy());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_SERVICE_COMPONENT");
        }
    }

    public void addSubscription(Subscription subscription) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            PreparedStatement pst = conn.prepareStatement("insert into SUBSCRIPTION (EMAIL, HASH) VALUES (?,?)");

            pst.setString(1, subscription.getEmail());
            pst.setString(2, subscription.getHash());

            int ins = pst.executeUpdate();
            logger.debug("finish, {} record inserted", ins);
        }
    }

    public long addServiceEvent(ServiceEventDTO se) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_SERVICE_EVENT.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into SERVICE_EVENT " +
                                "(ID, EVENT_DATE, STATUS_STRING, EVENT_TYPE, DESCRIPTION, COMPONENTS_STRING, RESOLVED, CREATED_AT, CREATED_BY, UPDATED_AT, UPDATED_BY) values " +
                                "(?,?,?,?,?,?,?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setTimestamp(2, nullableTs(se.getEventDate()));
                pst.setString(3, se.getStatusString());
                pst.setString(4, se.getEventType());
                pst.setString(5, se.getDescription());
                pst.setString(6, se.getComponentsString());
                pst.setBoolean(7, se.isResolved());
                pst.setTimestamp(8, nullableTs(se.getCreatedAt()));
                pst.setString(9, se.getCreatedBy());
                pst.setTimestamp(10,nullableTs(se.getUpdatedAt()));
                pst.setString(11,se.getUpdatedBy());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_SERVICE_EVENT");
        }
    }

    public long addEventUpdate(EventUpdateDTO eu) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_EVENT_UPDATE.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into EVENT_UPDATE " +
                        "(ID, UPDATE_DATE, TYPE, MESSAGE, EVENT_ID, CREATED_AT, CREATED_BY, UPDATED_AT, UPDATED_BY) VALUES " +
                        " (?,?,?,?,?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setTimestamp(2, nullableTs(eu.getDate()));
                pst.setString(3, eu.getType());
                pst.setString(4, eu.getMessage());
                pst.setLong(5, eu.getEventId());
                pst.setTimestamp(6, nullableTs(eu.getCreatedAt()));
                pst.setString(7, eu.getCreatedBy());
                pst.setTimestamp(8, nullableTs(eu.getUpdatedAt()));
                pst.setString(9, eu.getUpdatedBy());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_EVENT_UPDATE");
        }
    }

    public long addServiceStatus(ServiceStatusDTO stDTO) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            st.executeUpdate("update SERVICE_STATUS set CURRENT_STATUS=0");
            ResultSet rs = st.executeQuery("select SEQ_SERVICE_STATUS.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into SERVICE_STATUS " +
                        "(ID, STATUS_STRING, STATUS_TYPE, DESCRIPTION, EVENT_ID, CURRENT_STATUS, UPDATED_AT, UPDATED_BY) VALUES " +
                        "(?,?,?,?,?,?,?,?)");

                Long eventId = null;
                if (stDTO.getCurrentEvent() != null)
                    eventId = stDTO.getCurrentEvent().getId();

                pst.setLong(1, id);
                pst.setString(2, stDTO.getStatusString());
                pst.setString(3, stDTO.getStatusType());
                pst.setString(4, stDTO.getDescription());
                if (eventId == null)
                    pst.setNull(5, Types.BIGINT);
                else
                    pst.setLong(5, eventId);
                pst.setBoolean(6, stDTO.isCurrent());
                pst.setTimestamp(7, nullableTs(stDTO.getUpdatedAt()));
                pst.setString(8, stDTO.getUpdatedBy());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_SERVICE_STATUS");
        }
    }

    public long addSession(SessionDTO s) throws SQLException {
        try (Connection conn = getConnection()) {
            logger.debug("start");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SEQ_USER_SESSION.nextval from dual");
            if (rs.next()) {
                long id = rs.getLong(1);
                PreparedStatement pst = conn.prepareStatement("insert into USER_SESSION (ID, USER_ID, CREATED_AT, EXPIRED_AT, TOKEN) VALUES " +
                                "(?,?,?,?,?)");

                pst.setLong(1, id);
                pst.setLong(2, s.getUser().getId());
                pst.setTimestamp(3, nullableTs(s.getCreatedAt()));
                pst.setTimestamp(4, nullableTs(s.getExpiredAt()));
                pst.setString(5, s.getToken());

                int ins = pst.executeUpdate();
                logger.debug("finish, {} record inserted", ins);
                return id;
            } else
                throw new SQLException("Cannot get next value from SEQ_USER_SESSION");
        }
    }
    
    private Timestamp nullableTs(Instant date) {
        if (date == null)
            return null;
        else 
            return new Timestamp(date.toEpochMilli());
    }
}

