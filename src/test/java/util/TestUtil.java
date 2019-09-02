package util;

import com.corn.data.dto.*;
import com.corn.data.entity.Subscription;
import org.apache.commons.lang.math.RandomUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

public class TestUtil {

    private TestUtil() {
        // the class isn't meant to be instantiated.
    }

    public static final String INDEX_HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">\n" +
            "    <link href=\"https://fonts.googleapis.com/css?family=Roboto:300,400,500\" rel=\"stylesheet\">\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <title>Status dashboard</title>\n" +
            "    <base href=\"/\">\n" +
            "\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "</head>\n" +
            "<body>\n" +
            "<app-root></app-root>\n" +
            "<script type=\"text/javascript\" src=\"runtime.js\"></script><script type=\"text/javascript\" src=\"polyfills.js\"></script><script type=\"text/javascript\" src=\"styles.js\"></script><script type=\"text/javascript\" src=\"vendor.js\"></script><script type=\"text/javascript\" src=\"main.js\"></script></body>\n" +
            "</html>\n";

    public static UserDTO randomUser(Long uId) {
        return new UserDTO(
                uId,
                randomAlphabetic(7),
                randomAlphabetic(8),
                randomAlphabetic(7),
                randomAlphabetic(7),
                "maintainer",
                true
        );
    }

    public static AnnouncementDTO randomTestAnnouncement() {
        return new AnnouncementDTO(
                null,
                randomInstant(),
                randomAlphabetic(10),
                randomAlphabetic(10),
                true,
                randomInstant(),
                randomAlphabetic(10),
                randomInstant(),
                randomAlphabetic(10)
        );
    }

    public static IssueReportDTO randomIssueReport() {
        return new IssueReportDTO(
                null,
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomInstant(),
                true,
                randomInstant(),
                randomAlphabetic(10)
        );
    }

    public static ServiceComponentDTO randomServiceComponent() {
        return new ServiceComponentDTO(
                null,
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomInstant(),
                randomAlphabetic(10)
        );
    }

    public static Subscription randomSubscription() {
        return new Subscription(
                randomAlphabetic(32),
                randomAlphabetic(64)
        );
    }

    public static ServiceEventDTO randomServiceEvent(Long seId) {
        return new ServiceEventDTO(
                seId,
                randomInstant(),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(100),
                randomAlphabetic(100),
                false,
                randomInstant(),
                randomAlphabetic(100),
                randomInstant(),
                randomAlphabetic(100),
                new ArrayList<>()
        );
    }

    public static ServiceEventDTO resolvedServiceEvent(Instant eventDate) {
        return new ServiceEventDTO(
                null,
                eventDate,
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(100),
                randomAlphabetic(100),
                true,
                randomInstant(),
                randomAlphabetic(100),
                randomInstant(),
                randomAlphabetic(100),
                new ArrayList<>()
        );
    }

    public static ServiceEventDTO accidentServiceEvent(String componentsString, String username) {
        return new ServiceEventDTO(
                null,
                randomInstant(),
                "Service is temporarily unavailable",
                "accident",
                "Active accident! Service is temporarily unavailable",
                componentsString,
                false,
                randomInstant(),
                username,
                randomInstant(),
                username,
                new ArrayList<>()
        );
    }

    public static ServiceEventDTO maintenanceServiceEvent(String componentsString, String username) {
        return new ServiceEventDTO(
                null,
                randomInstant(),
                "Service is temporarily unavailable",
                "maintenance",
                "Maintenance! Service is temporarily unavailable",
                componentsString,
                false,
                randomInstant(),
                username,
                randomInstant(),
                username,
                new ArrayList<>()
        );
    }

    public static ServiceEventDTO randomServiceEvent(Long seId, List<EventUpdateDTO> history) {
        return new ServiceEventDTO(
                seId,
                randomInstant(),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(100),
                randomAlphabetic(100),
                false,
                randomInstant(),
                randomAlphabetic(100),
                randomInstant(),
                randomAlphabetic(100),
                history
        );
    }

    public static EventUpdateDTO randomEventUpdate(Long seId) {
        return new EventUpdateDTO(
                null,
                randomInstant(),
                randomAlphabetic(10),
                seId,
                randomAlphabetic(10),
                randomInstant(),
                randomAlphabetic(10),
                randomInstant(),
                randomAlphabetic(10)
        );
    }

    public static EventUpdateDTO randomEventUpdate(Long seId, Instant date) {
        return new EventUpdateDTO(
                null,
                date,
                randomAlphabetic(10),
                seId,
                randomAlphabetic(10),
                randomInstant(),
                randomAlphabetic(10),
                randomInstant(),
                randomAlphabetic(10)
        );
    }

    public static ServiceStatusDTO randomServiceStatus(long seId) {
        ServiceEventDTO eventDTO = randomServiceEvent(seId);

        return new ServiceStatusDTO(
                null,
                randomAlphabetic(20),
                randomAlphabetic(10),
                randomAlphabetic(100),
                eventDTO,
                true,
                randomInstant(),
                randomAlphabetic(10),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static ServiceStatusDTO randomServiceStatus(ServiceEventDTO eventDTO) {
        return new ServiceStatusDTO(
                null,
                randomAlphabetic(20),
                randomAlphabetic(10),
                randomAlphabetic(100),
                eventDTO,
                true,
                randomInstant(),
                randomAlphabetic(10),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static ServiceStatusDTO accidentServiceStatus(ServiceEventDTO eventDTO, String username, List<ServiceComponentDTO> components) {
        return new ServiceStatusDTO(
                null,
                "Service is temporarily unavailable",
                "accident",
                "Active accident! Service is temporarily unavailable",
                eventDTO,
                true,
                randomInstant(),
                username,
                components,
                new ArrayList<>()
        );
    }

    public static ServiceStatusDTO normalServiceStatus(String username, List<ServiceComponentDTO> components) {
        return new ServiceStatusDTO(
                null,
                "Service is operational",
                "normal",
                "Welcome to the Service Status Page. There you can see current information of the service performance. You can bookmark or subscribe to this page for the latest updates.",
                null,
                true,
                randomInstant(),
                username,
                components,
                new ArrayList<>()
        );
    }


    public static ServiceStatusDTO randomServiceStatus(ServiceEventDTO eventDTO, List<ServiceComponentDTO> components, List<ServiceEventDTO> history) {
        return new ServiceStatusDTO(
                null,
                randomAlphabetic(20),
                randomAlphabetic(10),
                randomAlphabetic(100),
                eventDTO,
                true,
                randomInstant(),
                randomAlphabetic(10),
                components,
                history
        );
    }

    public static SessionDTO randomSession(long uId) {
        UserDTO userDTO = randomUser(uId);
        return new SessionDTO(
                null,
                userDTO,
                randomInstant(),
                randomInstant(),
                randomAlphabetic(64)
        );
    }

    public static void assertWithin(Instant expected, Instant actual, long milliseconds) {
        if (expected == null)
            throw new AssertionError("Expected date should not be null");
        else if (actual == null)
            throw new AssertionError("Actual date is null");
        else if (milliseconds < 0)
            throw new AssertionError("Number of milliseconds should not be negative");
        else {
            long expectedMs = expected.toEpochMilli();
            long actualMs = actual.toEpochMilli();
            long diffMs = expectedMs - actualMs;

            if (Math.abs(diffMs) > milliseconds)
                throw new AssertionError("expected and actual are differ in " + Math.abs(diffMs) + " milliseconds");
        }
    }
    
    public static Instant randomInstant() {
        return Instant.ofEpochMilli(RandomUtils.nextLong());
    }
}
