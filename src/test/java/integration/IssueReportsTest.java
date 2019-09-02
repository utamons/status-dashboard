package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.corn.Application;
import com.corn.controller.ErrorJson;
import com.corn.data.dto.IssueReportDTO;
import com.corn.data.dto.Model;
import com.corn.data.dto.PageDTO;
import com.corn.data.dto.UserDTO;
import com.corn.data.entity.IssueReport;
import com.corn.data.repository.IssueReportRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;
import static util.TestUtil.assertWithin;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = Application.class)
public class IssueReportsTest extends BaseControllersTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private IssueReportRepo issueReportRepo;

    @Before
    public void setUp() throws ClassNotFoundException, SQLException {
        baseSetUp(port);
    }

    @Test
    public void getAllIssueReportsTest() throws SQLException {
        final int PAGE_SIZE = 10;

        UserDTO testUser = TestUtil.randomUser(null);
        List<IssueReportDTO> testList = new ArrayList<>();

        for (int i = 0; i < PAGE_SIZE; ++i) {
            IssueReportDTO reportDTO = TestUtil.randomIssueReport();
            long id = dbUtils.addIssueReport(reportDTO);
            testList.add(reportDTO.withId(id));
        }

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        ResponseEntity<PageDTO> response = template.exchange(baseApi + "/issueReport?start=0&size=" + PAGE_SIZE + "&processed=" + true, HttpMethod.GET,
                request, PageDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO body = response.getBody();
        assertNotNull(body);

        assertEquals(1, body.getTotalPages());
        assertEquals(PAGE_SIZE, body.getTotalElements());
        assertNotNull(body.getContent());
        assertEquals(PAGE_SIZE, body.getContent().size());

        checkIssueReportPage(0, PAGE_SIZE, body, testList);
    }

    @Test
    public void issueReportsPaginationTest() throws SQLException {
        final int PAGE_SIZE = 10;
        final int PAGES = 10;

        UserDTO testUser = TestUtil.randomUser(null);
        Map<Integer, List<IssueReportDTO>> testPages = new HashMap<>();

        for (int page = 0; page < PAGES; ++page) {
            List<IssueReportDTO> pageContent = new ArrayList<>();
            for (int i = 0; i < PAGE_SIZE; ++i) {
                IssueReportDTO reportDTO = TestUtil.randomIssueReport();
                long id = dbUtils.addIssueReport(reportDTO);
                pageContent.add(reportDTO.withId(id));
            }
            testPages.put(page, pageContent);
        }

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        for (int page = 0; page < PAGES; ++page) {

            ResponseEntity<PageDTO> response = template.exchange(baseApi + "/issueReport?start=" + page + "&size=" + PAGE_SIZE + "&processed=" + true,
                    HttpMethod.GET, request, PageDTO.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PageDTO body = response.getBody();
            assertNotNull(body);

            assertEquals(PAGES, body.getTotalPages());
            assertEquals(PAGE_SIZE * PAGES, body.getTotalElements());
            assertNotNull(body.getContent());
            assertEquals(PAGE_SIZE, body.getContent().size());

            List<IssueReportDTO> pageContent = testPages.get(page);

            checkIssueReportPage(page, PAGE_SIZE, body, pageContent);
        }
    }

    @Test
    public void issueReportsProcessedTest() throws SQLException {
        final int PAGE_SIZE = 10;
        final int PAGES = 10;

        UserDTO testUser = TestUtil.randomUser(null);
        Map<Integer, List<IssueReportDTO>> testPages = new HashMap<>();

        for (int page = 0; page < PAGES; ++page) {
            List<IssueReportDTO> pageContent = new ArrayList<>();
            for (int i = 0; i < PAGE_SIZE; ++i) {
                IssueReportDTO reportDTO = TestUtil.randomIssueReport().withProcessed(page % 2 == 0); // odd pages have processed=false items
                long id = dbUtils.addIssueReport(reportDTO);
                pageContent.add(reportDTO.withId(id));
            }
            testPages.put(page, pageContent);
        }

        HttpEntity<String> request = this.getAuthEntity("", testUser);

        for (int page = 0; page < PAGES / 2; ++page) {

            ResponseEntity<PageDTO> response = template.exchange(baseApi + "/issueReport?start=" + page + "&size=" + PAGE_SIZE + "&processed=" + false,
                    HttpMethod.GET, request, PageDTO.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PageDTO body = response.getBody();
            assertNotNull(body);

            assertEquals(PAGES / 2, body.getTotalPages());
            assertEquals(PAGE_SIZE * PAGES / 2, body.getTotalElements());
            assertNotNull(body.getContent());
            assertEquals(PAGE_SIZE, body.getContent().size());

            List<IssueReportDTO> pageContent = testPages.get(page * 2 + 1); // we take only odd pages with processed=false items

            checkIssueReportPage(page, PAGE_SIZE, body, pageContent);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newIssueReportTest() {
        IssueReportDTO test = TestUtil.randomIssueReport().withProcessed(true);
        ResponseEntity<Model> response = template.postForEntity(baseApi + "issueReport", test, Model.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Model<String> body = response.getBody();
        assertNotNull(body);

        Optional<IssueReport> resultOpt = issueReportRepo.findById(100L);
        assertTrue(resultOpt.isPresent());
        IssueReport result = resultOpt.get();

        Instant currentDate = Clock.systemDefaultZone().instant();

        assertEquals(test.getReportText(), result.getReportText());
        assertWithin(currentDate, result.getCreatedAt(), 500);
        assertFalse(result.isProcessed());
        assertNull(result.getProcessedAt());
        assertNull(result.getProcessedBy());
        assertNull(result.getRemarkText());
    }

    @Test
    public void nullReportTextTest() {
        IssueReportDTO test = TestUtil.randomIssueReport().withReportText(null);
        ResponseEntity<ErrorJson> response = template.postForEntity(baseApi + "issueReport", test, ErrorJson.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorJson errorJson = response.getBody();
        assertNotNull(errorJson);
        assertEquals("Please, enter a text", errorJson.getMessage());
    }

    @Test
    public void updateIssueReportTestProcessed() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        IssueReportDTO initial = TestUtil.randomIssueReport().withProcessed(false);
        long id = dbUtils.addIssueReport(initial);

        IssueReportDTO test = TestUtil.randomIssueReport().withId(id).withProcessed(true);

        HttpEntity<IssueReportDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<IssueReportDTO> response = template.exchange(baseApi + "issueReport", HttpMethod.PUT, request, IssueReportDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        IssueReportDTO body = response.getBody();
        assertNotNull(body);

        Optional<IssueReport> testOpt = issueReportRepo
                .findById(id);

        assertTrue(testOpt.isPresent());
        IssueReport result = testOpt.get();

        Instant currentDate = Clock.systemDefaultZone().instant();

        assertEquals(initial.getReportText(), result.getReportText());
        assertEquals(initial.getCreatedAt(), result.getCreatedAt());
        assertTrue(result.isProcessed());
        assertWithin(currentDate, result.getProcessedAt(), 500);
        assertEquals(testUser.getUsername(), result.getProcessedBy());
        assertEquals(test.getRemarkText(), result.getRemarkText());
    }

    @Test
    public void updateIssueReportTestRemark() throws SQLException {
        UserDTO testUser = TestUtil.randomUser(null);
        IssueReportDTO initial = TestUtil.randomIssueReport().withProcessed(false);
        long id = dbUtils.addIssueReport(initial);

        IssueReportDTO test = TestUtil.randomIssueReport().withId(id).withProcessed(false);

        HttpEntity<IssueReportDTO> request = this.getAuthEntity(test, testUser);

        ResponseEntity<IssueReportDTO> response = template.exchange(baseApi + "issueReport", HttpMethod.PUT, request, IssueReportDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        IssueReportDTO body = response.getBody();
        assertNotNull(body);

        Optional<IssueReport> testOpt = issueReportRepo.findById(id);

        assertTrue(testOpt.isPresent());
        IssueReport result = testOpt.get();

        assertEquals(initial.getReportText(), result.getReportText());
        assertEquals(initial.getCreatedAt(), result.getCreatedAt());
        assertFalse(result.isProcessed());
        assertNull(result.getProcessedAt());
        assertNull(result.getProcessedBy());
        assertEquals(test.getRemarkText(), result.getRemarkText());
    }


    @SuppressWarnings("SameParameterValue")
    private void checkIssueReportPage(int page, int PAGE_SIZE, PageDTO body, List<IssueReportDTO> pageContent) {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < PAGE_SIZE; ++i) {
            IssueReportDTO testDTO = pageContent.get(i);
            LinkedHashMap interim = (LinkedHashMap) body.getContent().get(i);
            IssueReportDTO result = mapper.convertValue(interim, IssueReportDTO.class);

            assertEquals("page: " + page + ", item: " + i, testDTO.getId(), result.getId());
            assertEquals(testDTO.getReportText(), result.getReportText());
            assertEquals(testDTO.getRemarkText(), result.getRemarkText());
            assertEquals(testDTO.getCreatedAt(), result.getCreatedAt());
            assertEquals(testDTO.getProcessedAt(), result.getProcessedAt());
            assertEquals(testDTO.getProcessedBy(), result.getProcessedBy());
        }
    }
}
