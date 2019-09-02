package integration;

import com.corn.data.dto.UserDTO;
import com.corn.service.mock.MockMailService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import util.DbUtils;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.corn.util.Constants.SESSION_ID;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static util.TestUtil.randomUser;

abstract class BaseControllersTest {

    String base;
    String baseApi;

    DbUtils dbUtils;

    void baseSetUp(int port) throws ClassNotFoundException, SQLException {
        base = "http://localhost:" + port + "/";
        baseApi = base + "api/";

        if (dbUtils == null) {
            dbUtils = new DbUtils();
        }

        dbUtils.cleanAll();
        MockMailService.cleanAllEmails();
    }

    HttpEntity<String> getAuthEntity(String token) throws SQLException {
        Long userId = dbUtils.addUser(randomUser(null));
        dbUtils.setAuth(token, userId, LocalDateTime.MAX);
        HttpHeaders headers = new HttpHeaders();
        headers.set(SESSION_ID, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>("", headers);
    }

    <T> HttpEntity<T> getAuthEntity(T body, UserDTO user) throws SQLException {
        String token = randomAlphanumeric(64);
        Long userId = dbUtils.addUser(user);

        dbUtils.setAuth(token, userId, LocalDateTime.MAX);
        HttpHeaders headers = new HttpHeaders();
        headers.set(SESSION_ID, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    Instant currentDate() {
        return Clock.systemDefaultZone().instant();
    }

    Instant daysAgo(int days) {
        return Clock.systemDefaultZone().instant().minus(days, ChronoUnit.DAYS);
    }
}
