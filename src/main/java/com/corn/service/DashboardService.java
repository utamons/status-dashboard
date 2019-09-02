package com.corn.service;

import com.corn.data.dto.*;
import com.corn.data.entity.*;
import com.corn.data.repository.*;
import com.corn.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.corn.util.Constants.FINISH;
import static com.corn.util.Constants.START;
import static com.corn.util.Utils.isEmpty;

/**
 * @author Oleg Zaidullin
 */
@Service
public class DashboardService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceStatusRepo serviceStatusRepo;
    private final ServiceComponentsRepo serviceComponentsRepo;
    private final EventUpdatesRepo eventUpdatesRepo;
    private final SessionService sessionService;
    private final ServiceEventsRepo serviceEventsRepo;
    private final AnnouncementsRepo announcementsRepo;
    private final IssueReportRepo issueReportRepo;
    private final SubscriptionRepo subscriptionRepo;
    private final MailService mailService;

    @Value("${corn.frontend.url}")
    private String frontendUrl;

    private final HashMap<Long, ServiceComponentDTO> componentsMap = new HashMap<>();

    public DashboardService(ServiceStatusRepo serviceStatusRepo,
                            ServiceComponentsRepo serviceComponentsRepo,
                            EventUpdatesRepo eventUpdatesRepo,
                            SessionService sessionService,
                            ServiceEventsRepo serviceEventsRepo,
                            AnnouncementsRepo announcementsRepo,
                            IssueReportRepo issueReportRepo,
                            SubscriptionRepo subscriptionRepo, MailService mailService) {
        this.serviceStatusRepo = serviceStatusRepo;
        this.serviceComponentsRepo = serviceComponentsRepo;
        this.eventUpdatesRepo = eventUpdatesRepo;
        this.sessionService = sessionService;
        this.serviceEventsRepo = serviceEventsRepo;
        this.announcementsRepo = announcementsRepo;
        this.issueReportRepo = issueReportRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.mailService = mailService;
        initComponentsMap();
    }


    private void initComponentsMap() {
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            componentsMap.put(component.getId(), component.toValue());
        }
    }

    private List<ServiceComponentDTO> componentValues() {
        List<ServiceComponentDTO> components = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            components.add(component.toValue());
        }
        return components;
    }


    public ServiceStatusDTO getStatus() {
        logger.debug(START);

        List<ServiceStatus> statuses = serviceStatusRepo.findAllByCurrent(true);
        if (statuses.size() == 0) {
            logger.error("No current status found in the database!");
            throw new ValidationException("Internal server error, the error has been logged");
        } else if (statuses.size() > 1) {
            logger.error("More than one current status in the database!");
            throw new ValidationException("Internal server error, the error has been logged");
        }

        List<ServiceComponentDTO> components = new ArrayList<>();
        serviceComponentsRepo.findAll().forEach(c -> components.add(c.toValue()));

        return statuses.get(0).toValue(components, history());
    }

    @Transactional
    public ServiceStatusDTO newStatus(ServiceStatusDTO statusValue, String sessionId) {
        validateStatusValue(statusValue, false);

        String createdBy = sessionService.currentUser(sessionId);
        Instant createdAt = Clock.systemDefaultZone().instant();

        statusValue.getComponents().forEach(c->this.updateComponent(c,createdBy));
        ServiceStatus exStatus = serviceStatusRepo.findAllByCurrent(true).get(0);
        exStatus.setCurrent(false);
        serviceStatusRepo.save(exStatus);

        ServiceEventDTO currentEventValue = statusValue.getCurrentEvent();


        ServiceEvent currentEvent = new ServiceEvent(
                createdAt,
                currentEventValue.getStatusString(),
                currentEventValue.getEventType(),
                currentEventValue.getDescription(),
                componentString(statusValue.getComponents()),
                false,
                createdAt,
                createdBy
        );

        currentEvent = serviceEventsRepo.save(currentEvent);

        List<EventUpdate> updates = new ArrayList<>();
        for (EventUpdateDTO u : statusValue.getCurrentEvent().getHistory()) {
            EventUpdate eventUpdate = new EventUpdate(
                    createdAt,
                    u.getType(),
                    currentEvent,
                    u.getMessage(),
                    createdAt,
                    createdBy
            );
            eventUpdate = eventUpdatesRepo.save(eventUpdate);
            updates.add(eventUpdate);
        }

        currentEvent.setHistory(updates);

        ServiceStatus serviceStatus = new ServiceStatus(
                statusValue.getStatusString(),
                statusValue.getStatusType(),
                statusValue.getDescription(),
                currentEvent,
                true,
                createdAt,
                createdBy
        );

        Map<String, String> emailTuples = new HashMap<>();

        subscriptionRepo.findAll().forEach(
                s -> {
                    if (isEmpty(s.getHash())) {
                        emailTuples.put(s.getEmail(), getUnsubscribeUrl(s.getEmail()));
                    }
                }
        );

        mailService.sendStatus(
                emailTuples,
                serviceStatus.getStatusType(),
                serviceStatus.getStatusString(),
                serviceStatus.getCurrentEvent().getComponentsString(),
                serviceStatus.getDescription()
        );

        return serviceStatusRepo.save(serviceStatus).toValue(componentValues(), history());
    }

    private String componentString(List<ServiceComponentDTO> components) {
        List<String> affectedComponents = new ArrayList<>();
        components.forEach(c -> {
            if (!c.getStatusType().equals("normal"))
                affectedComponents.add(componentsMap.get(c.getId()).getName());
        });
        return String.join(",", affectedComponents);
    }

    private void updateComponent(ServiceComponentDTO c, String updater) {
        ServiceComponent component = serviceComponentsRepo.findById(c.getId())
                .orElseThrow(() -> new ValidationException("Component with id=" + c.getId() + " does not exist"));
        component.setStatusType(c.getStatusType());
        component.setStatusString(componentStatus(c.getStatusType()));
        component.setUpdatedAt(Instant.now());
        component.setUpdatedBy(updater);
        serviceComponentsRepo.save(component);
    }

    private String componentStatus(String statusType) {
        if (statusType.equals("normal"))
            return "Operational";
        else
            return "Unavailable";
    }

    @SuppressWarnings("SameParameterValue")
    private void validateStatusValue(ServiceStatusDTO statusValue, boolean existing) {
        Validator validator = new Validator();
        if (existing) {
            final Long id = statusValue.getId();
            validator.notNull(id, "Status id").validate();
            validator.assertTrue(serviceStatusRepo.existsById(id),"Status "+ id + " not found").validate();
            validator.assertTrue(serviceStatusRepo.isStatusCurrent(id),"You cannot update archived status");
            validator.assertFalse(serviceStatusRepo.getStatusType(id).equals("normal"),"You cannot update normal status")
                    .validate();
        } else {
            final List<ServiceStatus> allCurrent = serviceStatusRepo.findAllByCurrent(true);
            validator.assertTrue(
                    allCurrent.size() > 0 && allCurrent.get(0).getStatusType().equals("normal")
                    , "Active accident or maintenance already exists and isn't " +
                            "resolved or finished");
        }

        validator.notEmpty(statusValue.getStatusString(), "Header")
                .notEmpty(statusValue.getStatusType(), "Issue type")
                .notEmpty(statusValue.getDescription(), "Description")
                .notNull(statusValue.getCurrentEvent(), "Current event data")
                .notEmpty(statusValue.getComponents(), "Components")
                .maxLength(statusValue.getStatusString(), 1024, "Header")
                .maxLength(statusValue.getStatusType(), 64, "Issue type")
                .maxLength(statusValue.getDescription(), 1024, "Description")
                .validate();

        validateCurrentEvent(statusValue.getCurrentEvent(), existing);
        validateComponents(statusValue.getComponents());
    }

    private void validateComponents(List<ServiceComponentDTO> components) {
        Validator validator = new Validator();
        components.forEach(c -> validator
                .notNull(c.getId(), "Id of component")
                .notEmpty(c.getStatusType(), "Status type of component")
                .notEmpty(c.getStatusString(), "Status string of component")
                .maxLength(c.getStatusType(), 64, "Status type of component")
                .maxLength(c.getStatusString(), 1024, "Status string of component")
                .assertTrue(serviceComponentsRepo.existsById(c.getId()), "Component with id=" + c.getId() + " does not exist")
        );
        validator.validate();
    }

    private void validateCurrentEvent(ServiceEventDTO currentEvent, boolean existing) {
        Validator validator = new Validator();
        if (existing) {
            validator.notNull(currentEvent.getId(), "Event id").validate();
            validator.assertTrue(serviceEventsRepo.existsById(currentEvent.getId()), "Event with id=" + currentEvent.getId() + " does not exist");
        }

        validator.notEmpty(currentEvent.getStatusString(), "Sub-Header")
                .notEmpty(currentEvent.getEventType(), "Event Type")
                .notEmpty(currentEvent.getDescription(), "Sub-Header description")
                .notEmpty(currentEvent.getHistory(), "Messages")
                .maxLength(currentEvent.getStatusString(), 1024, "Sub-Header")
                .maxLength(currentEvent.getEventType(), 64, "Event Type")
                .maxLength(currentEvent.getDescription(), 1024, "Sub-Header description")
                .validate();

        validateUpdates(currentEvent.getHistory());
    }

    private void validateUpdates(List<EventUpdateDTO> updates) {
        Validator validator = new Validator();
        updates.forEach(u -> validator
                .notEmpty(u.getType(), "Message type")
                .notEmpty(u.getMessage(), "Message text")
                .maxLength(u.getType(), 128, "Message type")
                .maxLength(u.getMessage(), 1024, "Message text")
                .validate()
        );
    }

    public ServiceStatusDTO updateStatus(ServiceStatusDTO statusValue, String sessionId) {
        validateStatusValue(statusValue, true);
        String updatedBy = sessionService.currentUser(sessionId);
        Instant updatedAt = Clock.systemDefaultZone().instant();

        statusValue.getComponents().forEach((c)->this.updateComponent(c,updatedBy));

        ServiceStatus serviceStatus = serviceStatusRepo.findById(statusValue.getId())
                .orElseThrow(() -> new ValidationException("Status " + statusValue.getId() + " not found"));

        serviceStatus.setStatusString(statusValue.getStatusString());
        serviceStatus.setStatusType(statusValue.getStatusType());
        serviceStatus.setDescription(statusValue.getDescription());
        serviceStatus.setUpdatedAt(updatedAt);
        serviceStatus.setUpdatedBy(updatedBy);

        ServiceEventDTO currentEventValue = statusValue.getCurrentEvent();
        ServiceEvent currentEvent = serviceStatus.getCurrentEvent();

        currentEvent.setStatusString(currentEventValue.getStatusString());
        currentEvent.setEventType(currentEventValue.getEventType());
        currentEvent.setDescription(currentEventValue.getDescription());
        currentEvent.setComponentsString(componentString(statusValue.getComponents()));
        currentEvent.setUpdatedAt(updatedAt);
        currentEvent.setUpdatedBy(updatedBy);
        currentEvent.setHistory(updateEventHistory(currentEvent, currentEventValue.getHistory(), updatedAt, updatedBy));

        serviceEventsRepo.save(currentEvent);

        return serviceStatusRepo.save(serviceStatus).toValue(componentValues(), history());
    }

    @Transactional
    public List<EventUpdate> updateEventHistory(ServiceEvent event, List<EventUpdateDTO> history, Instant updatedAt, String updatedBy) {
        List<EventUpdate> currentHistory = eventUpdatesRepo.findAllByEvent(event);

        // delete - must be first, otherwise just added will be deleted
        List<EventUpdate> toDelete = currentHistory.stream()
                .filter(h ->
                        history.stream()
                                .noneMatch(hs ->
                                        h.getId().equals(hs.getId())
                                )).collect(Collectors.toList());

        currentHistory.removeAll(toDelete);
        for (EventUpdate update: toDelete)
            eventUpdatesRepo.deleteElem(update.getId());

        // update
        history.stream().filter(hs -> hs.getId() != null && hs.getEventId().equals(event.getId())).forEach(hs -> {
            EventUpdate update = eventUpdatesRepo.findById(hs.getId())
                    .orElseThrow(() -> new ValidationException("Message " + hs.getId() + " not found"));
            update.setMessage(hs.getMessage());
            update.setType(hs.getType());
            update.setUpdatedAt(updatedAt);
            update.setUpdatedBy(updatedBy);
            eventUpdatesRepo.save(update);
        });

        // add
        history.stream().filter(hs -> hs.getId() == null).forEach(hs -> {
            EventUpdate update = new EventUpdate(
                    hs.getDate(),
                    hs.getType(),
                    event,
                    hs.getMessage(),
                    updatedAt,
                    updatedBy
            );
            eventUpdatesRepo.save(update);
            currentHistory.add(update);
        });

        return currentHistory;
    }

    public ServiceStatusDTO resolveStatus(Long id, String sessionId) {
        ServiceStatus serviceStatus = serviceStatusRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Status " + id + " not found."));
        if (!serviceStatus.isCurrent())
            throw new ValidationException("This status is already resolved!");
        else if (serviceStatus.getStatusType().equals("normal"))
            throw new ValidationException("This status cannot be resolved!");

        String updatedBy = sessionService.currentUser(sessionId);
        Instant updatedAt = Clock.systemDefaultZone().instant();

        String components = serviceStatus.getCurrentEvent().getComponentsString();

        serviceStatus.setUpdatedBy(updatedBy);
        serviceStatus.setUpdatedAt(updatedAt);
        serviceStatus.setCurrent(false);
        serviceStatus.getCurrentEvent().setResolved(true);
        EventUpdate finalUpdate = addFinalMessage(serviceStatus.getCurrentEvent(), updatedBy, updatedAt);
        serviceStatusRepo.save(serviceStatus);

        serviceStatus = ServiceStatus.getNormal(updatedAt, updatedBy);
        serviceStatus = serviceStatusRepo.save(serviceStatus);

        makeComponentsNormal(updatedAt, updatedBy);

        Map<String, String> emailTuples = new HashMap<>();

        subscriptionRepo.findAll().forEach(
                s -> {
                    if (isEmpty(s.getHash())) {
                        emailTuples.put(s.getEmail(), getUnsubscribeUrl(s.getEmail()));
                    }
                }
        );

        mailService.sendStatus(
                emailTuples,
                serviceStatus.getStatusType(),
                serviceStatus.getStatusString(),
                components,
                finalUpdate.getMessage()

        );

        return serviceStatus.toValue(componentValues(), history());
    }

    private EventUpdate addFinalMessage(ServiceEvent currentEvent, String updatedBy, Instant updatedAt) {
        List<EventUpdate> history = currentEvent.getHistory();
        String messageType = currentEvent.getEventType().equals("maintenance") ? "Update" : "Resolved";
        String message = currentEvent.getEventType().equals("maintenance") ? "The maintenance has been finished." : "The issue has been resolved.";

        EventUpdate eventUpdate = new EventUpdate(
                Clock.systemDefaultZone().instant(), messageType, currentEvent, message, updatedAt, updatedBy
        );
        eventUpdate = eventUpdatesRepo.save(eventUpdate);
        history.add(eventUpdate);
        return eventUpdate;
    }

    private void makeComponentsNormal(Instant updatedAt, String updatedBy) {
        Iterable<ServiceComponent> components = serviceComponentsRepo.findAll();

        for (ServiceComponent component : components) {
            component.setStatusType("normal");
            component.setStatusString("Operational");
            component.setUpdatedAt(updatedAt);
            component.setUpdatedBy(updatedBy);
        }

        serviceComponentsRepo.saveAll(components);
    }

    private List<ServiceEventDTO> history() {
        List<ServiceEvent> events = serviceEventsRepo.findTop10ByResolvedOrderByEventDateDesc(true);
        List<ServiceEventDTO> result = events.stream().map(ServiceEvent::toValue).collect(Collectors.toList());
        for (ServiceEventDTO eventDTO : result) {
            eventDTO.getHistory().sort(Comparator.comparing(EventUpdateDTO::getDate));
        }
        return result;
    }

    public AnnouncementDTO newAnnouncement(AnnouncementDTO announcementDTO, String sessionId) {
        validateAnnouncement(announcementDTO, false);
        Instant currentDate = Clock.systemDefaultZone().instant();
        String createdBy = sessionService.currentUser(sessionId);
        List<Announcement> active = announcementsRepo.findAllByActive(true);
        active.forEach(a -> a.setActive(false));
        announcementsRepo.saveAll(active);

        Announcement announcement = new Announcement(
                currentDate,
                announcementDTO.getHeader(),
                announcementDTO.getDescription(),
                true,
                currentDate,
                createdBy
        );

        Map<String, String> emailTuples = new HashMap<>();

        subscriptionRepo.findAll().forEach(
                s -> {
                    if (isEmpty(s.getHash())) {
                        emailTuples.put(s.getEmail(), getUnsubscribeUrl(s.getEmail()));
                    }
                }
        );


        mailService.sendAnnouncement(emailTuples, announcement.getHeader(), announcement.getDescription());


        return announcementsRepo.save(announcement).toValue();
    }

    private void validateAnnouncement(AnnouncementDTO announcementDTO, boolean existing) {
        Validator validator = new Validator();
        validator
                .notEmpty(announcementDTO.getHeader(), "Header")
                .notEmpty(announcementDTO.getDescription(), "Description")
                .maxLength(announcementDTO.getHeader(), 1024, "Header")
                .maxLength(announcementDTO.getDescription(), 2048, "Description");

        if (existing) {
            Long id = announcementDTO.getId();
            validator.notNull(id, "Id").validate();
            Optional<Announcement> aOpt = announcementsRepo.findById(id);
            validator.assertId(aOpt.isPresent(), id, "Announcement").validate();
            if (aOpt.isPresent())
                validator.assertTrue(aOpt.get().isActive(), "Trying to update deleted announcement");
        } else {
            validator.assertTrue(getAnnouncement() == null, "Active announcement already exists");
        }

        validator.validate();
    }

    public AnnouncementDTO getAnnouncement() {
        List<Announcement> active = announcementsRepo.findAllByActive(true);
        if (active.size() > 0)
            return active.get(0).toValue();
        else
            return null;
    }

    public AnnouncementDTO updateAnnouncement(AnnouncementDTO announcementDTO, String sessionId) {
        validateAnnouncement(announcementDTO, true);
        Instant updateDate = Clock.systemDefaultZone().instant();
        String updatedBy = sessionService.currentUser(sessionId);
        Announcement announcement = announcementsRepo.findById(announcementDTO.getId())
                .orElseThrow(() -> new ValidationException("Announcement " + announcementDTO.getId() + " not found"));

        announcement.setHeader(announcementDTO.getHeader());
        announcement.setDescription(announcementDTO.getDescription());
        announcement.setUpdatedAt(updateDate);
        announcement.setUpdatedBy(updatedBy);

        return announcementsRepo.save(announcement).toValue();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void deleteAnnouncement(Long id, String sessionId) {
        Validator validator = new Validator();
        Optional<Announcement> aOpt = announcementsRepo.findById(id);

        Announcement announcement = aOpt.orElseThrow(() -> new ValidationException("Announcement {" + id + "} not found. "));

        validator.assertTrue(announcement.isActive(), "Trying to delete already deleted announcement");
        validator.validate();

        Instant updateDate = Clock.systemDefaultZone().instant();
        String updatedBy = sessionService.currentUser(sessionId);

        announcement.setActive(false);
        announcement.setUpdatedAt(updateDate);
        announcement.setUpdatedBy(updatedBy);
        announcementsRepo.save(announcement);
    }

    public void newIssueReport(IssueReportDTO issueReportDTO) {
        if (issueReportDTO.getReportText() == null || issueReportDTO.getReportText().length() == 0)
            throw new ValidationException("Please, enter a text");
        IssueReport issueReport = new IssueReport(
                issueReportDTO.getReportText(),
                Clock.systemDefaultZone().instant()
        );
        issueReportRepo.save(issueReport);
    }

    public PageDTO<IssueReportDTO> getIssueReports(int startPage, int pageSize, boolean showProcessed) {
        Page<IssueReportDTO> page = issueReportRepo.findAllByProcessed(showProcessed, PageRequest.of(startPage, pageSize)).map(IssueReport::toValue);
        return new PageDTO<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public void updateIssueReport(IssueReportDTO issueReportDTO, String sessionId) {
        Instant updateDate = Clock.systemDefaultZone().instant();
        String updatedBy = sessionService.currentUser(sessionId);
        IssueReport issueReport = issueReportRepo.findById(issueReportDTO.getId())
                .orElseThrow(() -> new ValidationException("Report " + issueReportDTO.getId() + " not found"));

        if (issueReportDTO.getReportText() == null || issueReportDTO.getReportText().length() == 0)
            throw new ValidationException("Please, enter a text");

        issueReport.setProcessed(issueReportDTO.isProcessed());
        if (issueReportDTO.isProcessed()) {
            issueReport.setProcessedAt(updateDate);
            issueReport.setProcessedBy(updatedBy);
        }
        issueReport.setRemarkText(issueReportDTO.getRemarkText());

        issueReportRepo.save(issueReport);
    }

    public String newSubscription(String email) {
        logger.debug(START);
        String result = "Thank you for the subscription. Please check your email for instructions.";
        Validator validator = new Validator();
        validator.notEmpty(email, "Subscription email").validate();
        validator.maxLength(email, 256, "Subscription email").validate();
        validator.email(email, "Subscription email").validate();

        Subscription subscription = subscriptionRepo.getByEmail(email).orElse(null);

        if (subscription != null && subscription.getHash() == null) {
            result = "This email already subscribed to the status page.";
        } else if (subscription != null && subscription.getHash() != null) {
            mailService.sendConfirmation(email, frontendUrl + "subscribe?hash=" + subscription.getHash());
        } else {
            String hash = Sha512DigestUtils.shaHex(email + System.currentTimeMillis());
            subscription = new Subscription(email, hash);
            subscriptionRepo.save(subscription);
            mailService.sendConfirmation(email, frontendUrl + "subscribe?hash=" + hash);
        }
        logger.debug(FINISH);
        return result;
    }

    public String confirmSubscription(String hash) {
        Subscription subscription = subscriptionRepo.findTopByHash(hash);
        String result;

        if (subscription == null)
            result = "You already confirmed this email.";
        else {
            subscription.setHash(null);
            result = "Your email has been successfully confirmed";
            subscriptionRepo.save(subscription);
        }

        return result;
    }

    public String deleteSubscription(String email, String hash) {
        String check = getCheckHash(email);
        if (hash.equals(check)) {
            Subscription subscription = subscriptionRepo.getByEmail(email)
                    .orElseThrow(() -> new ValidationException("Email not found."));
            subscriptionRepo.delete(subscription);
            return "The email successfully unsubscribed.";
        } else
            return "Hmm.. something went wrong, please ask support for help.";
    }

    private static String getCheckHash(String email) {
        return Sha512DigestUtils.shaHex("salt123456789pepper" + email);
    }

    private String getUnsubscribeUrl(String email) {
        return frontendUrl + "subscribe?email=" + email + "&check=" + getCheckHash(email);
    }

    public static void main(String[] args) {
        System.out.println(getCheckHash("utamons@yandex.com"));
    }

    @SuppressWarnings("SameReturnValue")
    public String logout(String sessionId) {
        sessionService.logout(sessionId);
        return "ok";
    }
}
