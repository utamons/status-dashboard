package com.corn.controller;

import com.corn.data.dto.*;
import com.corn.exception.ValidationException;
import com.corn.service.DashboardService;
import com.corn.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import static com.corn.util.Constants.START;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * REST controller for user related requests
 *
 * @author Oleg Zaidullin
 */

@RestController
@RequestMapping(path = "/api")
@CrossOrigin({"https://localhost:4200", "https://127.0.0.1:4200"})
public class DashboardController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DashboardService dashboardService;
    private final SessionService sessionService;

    public DashboardController(DashboardService dashboardService, SessionService sessionService) {
        this.dashboardService = dashboardService;
        this.sessionService = sessionService;
    }

    @Profile("test")
    @GetMapping(value = "/test", produces = APPLICATION_JSON_VALUE)
    public Model<String> getTest(@RequestParam("param") String param) {
        logger.debug(START);
        if ("error".equals(param)) {
            throw new ValidationException("Test error");
        }
        return new Model<>("ok");
    }

    @GetMapping(value = "/status", produces = APPLICATION_JSON_VALUE)
    public ServiceStatusDTO getStatus() {
        logger.debug(START);
        return dashboardService.getStatus();
    }

    @PostMapping(value = "/status", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ServiceStatusDTO newStatus(@RequestBody ServiceStatusDTO statusValue, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        return dashboardService.newStatus(statusValue, sessionId);
    }

    @PutMapping(value = "/status", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ServiceStatusDTO updateStatus(@RequestBody ServiceStatusDTO statusValue, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        return dashboardService.updateStatus(statusValue, sessionId);
    }

    @GetMapping(value = "/announcement", produces = APPLICATION_JSON_VALUE)
    public AnnouncementDTO getAnnouncement() {
        logger.debug(START);
        return dashboardService.getAnnouncement();
    }

    @PostMapping(value = "/announcement", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public AnnouncementDTO newAnnouncement(@RequestBody AnnouncementDTO announcementDTO, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        return dashboardService.newAnnouncement(announcementDTO, sessionId);
    }

    @PutMapping(value = "/announcement", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public AnnouncementDTO updateAnnouncement(@RequestBody AnnouncementDTO announcementDTO, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        return dashboardService.updateAnnouncement(announcementDTO, sessionId);
    }

    @DeleteMapping(value = "/announcement/{id}", produces = APPLICATION_JSON_VALUE)
    public Model<String> deleteAnnouncement(@PathVariable("id") Long id, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        dashboardService.deleteAnnouncement(id, sessionId);
        return new Model<>("ok");
    }

    // todo blocking login from a suspicious IP for some time after X unsuccessful tries

    @PostMapping(value = "/login", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public SessionDTO login(@RequestBody UserDTO userDTO) {
        logger.debug(START);
        return sessionService.login(userDTO);
    }

    @DeleteMapping(value = "/login", produces = APPLICATION_JSON_VALUE)
    public Model<String> logout(@RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        return new Model<>(dashboardService.logout(sessionId));
    }

    @PostMapping(value = "/resolve/{id}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ServiceStatusDTO resolveStatus(@PathVariable("id") Long id, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        return dashboardService.resolveStatus(id, sessionId);
    }

    @GetMapping(value = "/issueReport", produces = APPLICATION_JSON_VALUE)
    public PageDTO<IssueReportDTO> getIssueReports(@RequestParam("start") int startPage, @RequestParam("size") int pageSize, @RequestParam("processed") boolean showProcessed) {
        logger.debug(START);
        return dashboardService.getIssueReports(startPage, pageSize, showProcessed);
    }

    @PostMapping(value = "/issueReport", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Model<String> newIssueReport(@RequestBody IssueReportDTO issueReportDTO) {
        logger.debug(START);
        dashboardService.newIssueReport(issueReportDTO);
        return new Model<>("ok");
    }

    @PutMapping(value = "/issueReport", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Model<String> updateIssueReport(@RequestBody IssueReportDTO issueReportDTO, @RequestHeader("session-id") String sessionId) {
        logger.debug(START);
        dashboardService.updateIssueReport(issueReportDTO, sessionId);
        return new Model<>("ok");
    }

    @PostMapping(value = "/subscription", produces = APPLICATION_JSON_VALUE)
    public Model<String> newSubscription(@RequestParam("email") String email) {
        logger.debug(START);
        return new Model<>(dashboardService.newSubscription(email));
    }

    @DeleteMapping(value = "/subscription", produces = APPLICATION_JSON_VALUE)
    public Model<String> deleteSubscription(@RequestParam("email") String email, @RequestParam("check") String check) {
        logger.debug(START);
        return new Model<>(dashboardService.deleteSubscription(email, check));
    }

    @PostMapping(value = "/confirm", produces = APPLICATION_JSON_VALUE)
    public Model<String> confirmSubscription(@RequestParam("hash") String hash) {
        logger.debug(START);
        return new Model<>(dashboardService.confirmSubscription(hash));
    }
}