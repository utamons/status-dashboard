package integration;

import com.corn.data.dto.*;
import com.corn.data.entity.ServiceComponent;
import com.corn.data.repository.ServiceComponentsRepo;
import org.apache.commons.lang.math.RandomUtils;
import util.DbUtils;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.*;
import static util.TestUtil.*;

@SuppressWarnings("SameParameterValue")
class StatusTestHelper {

    private final ServiceComponentsRepo serviceComponentsRepo;
    private final DbUtils dbUtils;

    StatusTestHelper(ServiceComponentsRepo serviceComponentsRepo, DbUtils dbUtils) {
        this.serviceComponentsRepo = serviceComponentsRepo;
        this.dbUtils = dbUtils;
    }

    private List<ServiceComponentDTO> prepareAccidentComponents() {
        return prepareRandomComponents("accident", "Unavailable");
    }

    private List<ServiceComponentDTO> prepareMaintenanceComponents() {
        return prepareRandomComponents("maintenance", "Unavailable");
    }

    private List<ServiceComponentDTO> prepareRandomComponents(String type, String status) {
        List<ServiceComponentDTO> result = new ArrayList<>();
        List<ServiceComponent> components = new ArrayList<>();
        boolean accidentAdded = false;
        // trying to set random accident components.
        while (!accidentAdded) {
            components.clear();
            for (ServiceComponent component : serviceComponentsRepo.findAll()) {
                if (RandomUtils.nextInt() % 2 == 0) { // random affected components
                    component.setStatusType(type);
                    component.setStatusString(status);
                    accidentAdded = true;
                } else {
                    component.setStatusType("normal");
                    component.setStatusString("Operational");
                }
                components.add(component);
            }
        }
        // update database
        for (ServiceComponent component : components) {
            serviceComponentsRepo.save(component);
            result.add(component.toValue()
                    .withName(randomAlphabetic(10)) // name should not be updated by any endpoint. We set random value to check this.
                    .withUpdatedAt(randomInstant()) // updatedAt and updatedBy should not be set using data passed to endpoint. We set random to check this
                    .withUpdatedBy(randomAlphabetic(10)));
        }
        return result;
    }

    private List<ServiceComponentDTO> getAccidentComponents() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        boolean accidentAdded = false;
        // trying to set random accident components.
        while (!accidentAdded) {
            result.clear();
            for (ServiceComponent component : serviceComponentsRepo.findAll()) {
                if (RandomUtils.nextInt() % 2 == 0) { // random affected components
                    component.setStatusType("accident");
                    component.setStatusString("Unavailable");
                    accidentAdded = true;
                } else {
                    component.setStatusType("normal");
                    component.setStatusString("Operational");
                }
                result.add(component.toValue()
                        .withName(randomAlphabetic(10)) // name should not be updated by any endpoint. We set random value to check this.
                        .withUpdatedAt(randomInstant()) // updatedAt and updatedBy should not be set using data passed to endpoint. We set random to check this
                        .withUpdatedBy(randomAlphabetic(10))
                );
            }
        }
        return result;
    }

    private List<ServiceComponentDTO> prepareNormalComponents() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            component.setStatusType("normal");
            component.setStatusString("Operational");
            serviceComponentsRepo.save(component);
            result.add(component.toValue());
        }
        return result;
    }

    List<ServiceComponentDTO> getWrongIdComponent() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withId(999L));
        return result;
    }

    List<ServiceComponentDTO> getNullStatusTypeComponent() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withStatusType(null));
        return result;
    }

    List<ServiceComponentDTO> getEmptyStatusTypeComponent() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withStatusType(""));
        return result;
    }

    List<ServiceComponentDTO> getNullStatusStringComponent() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withStatusString(null));
        return result;
    }

    List<ServiceComponentDTO> getEmptyStatusStringComponent() {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withStatusString(""));
        return result;
    }

    List<ServiceComponentDTO> getLongStatusTypeComponent(int size) {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withStatusType(randomAlphabetic(size)));
        return result;
    }

    List<ServiceComponentDTO> getLongStatusStringComponent(int size) {
        List<ServiceComponentDTO> result = new ArrayList<>();
        for (ServiceComponent component : serviceComponentsRepo.findAll()) {
            result.add(component.toValue());
        }
        result.add(0, result.get(0).withStatusString(randomAlphabetic(size)));
        return result;
    }

    String componentsString(List<ServiceComponentDTO> sourceList) {
        List<String> components = new ArrayList<>();
        for (ServiceComponentDTO component : sourceList) {
            if (!component.getStatusType().equals("normal"))
                components.add(component.getName());
        }
        return String.join(",", components);
    }


    /*
     * getAccidentStatus - status with any accident state, including current event with at least one update message and with an actual components string
     */
    ServiceStatusDTO prepareAccidentStatus(boolean resolved) throws SQLException {
        UserDTO testUser = randomUser(null);

        List<ServiceComponentDTO> components = prepareAccidentComponents();

        ServiceEventDTO initialEvent = accidentServiceEvent(componentsString(components), testUser.getUsername()).withResolved(resolved);
        long eventId = dbUtils.addServiceEvent(initialEvent);

        EventUpdateDTO update = randomEventUpdate(eventId, initialEvent.getEventDate())
                .withCreatedAt(initialEvent.getCreatedAt())
                .withCreatedBy(testUser.getUsername());

        long updateId = dbUtils.addEventUpdate(update);

        initialEvent = initialEvent.withId(eventId).withHistory(Collections.singletonList(update.withId(updateId)));

        ServiceStatusDTO initialStatus = randomServiceStatus(initialEvent).withCurrent(!resolved).withUpdatedAt(initialEvent.getCreatedAt());
        long statusId = dbUtils.addServiceStatus(initialStatus);

        return initialStatus.withId(statusId).withComponents(components);
    }

    /*
     * getAccidentStatus - status with any accident state, including current event with at least one update message and with an actual components string
     */
    ServiceStatusDTO prepareMaintenanceStatus(boolean resolved) throws SQLException {
        UserDTO testUser = randomUser(null);

        List<ServiceComponentDTO> components = prepareMaintenanceComponents();

        ServiceEventDTO initialEvent = maintenanceServiceEvent(componentsString(components), testUser.getUsername()).withResolved(resolved);
        long eventId = dbUtils.addServiceEvent(initialEvent);

        EventUpdateDTO update = randomEventUpdate(eventId, initialEvent.getEventDate())
                .withCreatedAt(initialEvent.getCreatedAt())
                .withCreatedBy(testUser.getUsername());

        long updateId = dbUtils.addEventUpdate(update);

        initialEvent = initialEvent.withId(eventId).withHistory(Collections.singletonList(update.withId(updateId)));

        ServiceStatusDTO initialStatus = randomServiceStatus(initialEvent).withCurrent(!resolved).withUpdatedAt(initialEvent.getCreatedAt());
        long statusId = dbUtils.addServiceStatus(initialStatus);

        return initialStatus.withId(statusId).withComponents(components);
    }


    List<EventUpdateDTO> prepareAdditionalHistory(ServiceEventDTO initialEvent, int size) throws SQLException {
        long eventId = initialEvent.getId();
        List<EventUpdateDTO> history = new ArrayList<>(initialEvent.getHistory());

        for (int i = 0; i < size; ++i) {
            EventUpdateDTO update = randomEventUpdate(eventId, initialEvent.getEventDate())
                    .withCreatedAt(initialEvent.getCreatedAt())
                    .withCreatedBy(initialEvent.getCreatedBy());

            long updateId = dbUtils.addEventUpdate(update);
            history.add(update.withId(updateId));
        }
        return history;
    }

    ServiceStatusDTO prepareNormalStatus() throws SQLException {
        UserDTO testUser = randomUser(null);

        List<ServiceComponentDTO> components = prepareNormalComponents();

        ServiceStatusDTO initialStatus = normalServiceStatus(testUser.getUsername(), components);
        long statusId = dbUtils.addServiceStatus(initialStatus);

        return initialStatus.withId(statusId).withComponents(components);
    }

    ServiceStatusDTO getAccidentStatus() {
        List<ServiceComponentDTO> components = getAccidentComponents();
        ServiceEventDTO initialEvent = getAccidentEvent();
        return accidentServiceStatus(initialEvent, null, components).withCurrent(false);
    }

    ServiceEventDTO getAccidentEvent() {
        ServiceEventDTO initialEvent = accidentServiceEvent(null, null).withResolved(false);
        EventUpdateDTO update = randomEventUpdate(null, initialEvent.getEventDate());
        initialEvent = initialEvent.withHistory(Collections.singletonList(update));
        return initialEvent;
    }

    ServiceStatusDTO setActualData(Long statusId, Long eventId, String creator, String updater, Instant createdAt, Instant updatedAt,
                                   Instant eventDate, ServiceStatusDTO testStatus, List<ServiceComponentDTO> initialComponents) {

        List<EventUpdateDTO> updates = new ArrayList<>();

        for (EventUpdateDTO updateDTO : testStatus.getCurrentEvent().getHistory()) {
            updates.add(
                    updateDTO
                            .withEventId(eventId)
                            .withCreatedBy(creator)
                            .withCreatedAt(createdAt)
                            .withUpdatedAt(updatedAt)
                            .withUpdatedBy(updater)
                            .withDate(eventDate)
            );
        }

        List<ServiceComponentDTO> components = new ArrayList<>();

        for (int i=0; i<testStatus.getComponents().size(); ++i) {
            ServiceComponentDTO componentDTO = testStatus.getComponents().get(i);
            ServiceComponentDTO initialComponentDTO =initialComponents.get(i);
            Instant updateDate = updatedAt == null ? createdAt : updatedAt;
            String updaterBy = updater == null ? creator : updater;
            components.add(
                    componentDTO
                            .withName(initialComponentDTO.getName())
                            .withUpdatedBy(updaterBy)
                            .withUpdatedAt(updateDate)
            );
        }

        ServiceEventDTO eventDTO = testStatus.getCurrentEvent()
                .withComponentsString(componentsString(components))
                .withEventDate(eventDate)
                .withCreatedBy(creator)
                .withCreatedAt(createdAt)
                .withUpdatedAt(updatedAt)
                .withUpdatedBy(updater)
                .withHistory(updates);

        return testStatus
                .withCurrent(true)
                .withCurrentEvent(eventDTO)
                .withUpdatedAt(updatedAt == null ? createdAt : updatedAt)
                .withUpdatedBy(updater == null ? creator : updater)
                .withComponents(components)
                .withId(statusId);
    }

    List<ServiceComponentDTO> actualComponents() {
        List<ServiceComponentDTO> components = new ArrayList<>();
        serviceComponentsRepo.findAll().forEach(c -> components.add(c.toValue()));
        return components;
    }

    void checkEvent(ServiceEventDTO test, ServiceEventDTO actual) {
        if (test.getId() != null)
            assertEquals(test.getId(), actual.getId());
        assertWithin(test.getEventDate(), actual.getEventDate(), 500);
        assertEquals(test.getStatusString(), actual.getStatusString());
        assertEquals(test.getEventType(), actual.getEventType());
        assertEquals(test.getDescription(), actual.getDescription());
        assertEquals(test.getComponentsString(), actual.getComponentsString());
        assertEquals(test.isResolved(), actual.isResolved());
        assertWithin(test.getCreatedAt(), actual.getCreatedAt(), 500);
        assertEquals(test.getCreatedBy(), actual.getCreatedBy());
        if (test.getUpdatedAt() == null)
            assertEquals(test.getUpdatedAt(), actual.getUpdatedAt());
        else
            assertWithin(test.getUpdatedAt(), actual.getUpdatedAt(), 500);

        final List<EventUpdateDTO> actualHistory = actual.getHistory();
        final List<EventUpdateDTO> testHistory = test.getHistory();

        checkUpdatesHistory(testHistory, actualHistory);
    }

    void checkUpdatesHistory(List<EventUpdateDTO> testHistory, List<EventUpdateDTO> actualHistory) {
        assertEquals(testHistory.size(), actualHistory.size());
        for (int j = 0; j < testHistory.size(); ++j) {
            EventUpdateDTO testEventUpdate = testHistory.get(j);
            EventUpdateDTO actualEventUpdate = actualHistory.get(j);

            assertEquals(testEventUpdate.getMessage(), actualEventUpdate.getMessage());
            assertEquals(testEventUpdate.getType(), actualEventUpdate.getType());

            assertEquals(testEventUpdate.getEventId(), actualEventUpdate.getEventId());
            assertWithin(testEventUpdate.getCreatedAt(), actualEventUpdate.getCreatedAt(), 500);
            assertEquals(testEventUpdate.getCreatedBy(), actualEventUpdate.getCreatedBy());
            assertWithin(testEventUpdate.getDate(), actualEventUpdate.getDate(), 500);
            if (testEventUpdate.getUpdatedAt() != null)
                assertWithin(testEventUpdate.getUpdatedAt(), actualEventUpdate.getUpdatedAt(), 500);
        }
    }

    void checkComponents(List<ServiceComponentDTO> testComponents, List<ServiceComponentDTO> actualComponents, List<ServiceComponentDTO> initialComponents) {
        assertNotNull(testComponents);
        assertNotNull(actualComponents);
        assertEquals(initialComponents.size(), actualComponents.size());
        assertEquals(initialComponents.size(), testComponents.size());
        for (int i = 0; i < testComponents.size(); ++i) {
            ServiceComponentDTO testComponent = testComponents.get(i);
            ServiceComponentDTO actualComponent = actualComponents.get(i);
            ServiceComponentDTO initialComponent = initialComponents.get(i);
            assertEquals(initialComponent.getId(), actualComponent.getId());
            assertEquals(testComponent.getStatusType(), actualComponent.getStatusType());
            assertEquals(testComponent.getStatusString(), actualComponent.getStatusString());
            assertEquals(initialComponent.getName(), actualComponent.getName());
            if (actualComponent.getUpdatedAt() == null)
                assertNull(testComponent.getUpdatedAt());
            else
                assertWithin(testComponent.getUpdatedAt(), actualComponent.getUpdatedAt(), 500);
            assertEquals(testComponent.getUpdatedBy(), actualComponent.getUpdatedBy());
        }
    }

    // for Get tests, skipping updatedAt,updatedBy checks
    void checkComponentsForGet(List<ServiceComponentDTO> testComponents, List<ServiceComponentDTO> actualComponents,
                             List<ServiceComponentDTO> initialComponents) {
        assertNotNull(testComponents);
        assertNotNull(actualComponents);
        assertEquals(initialComponents.size(), actualComponents.size());
        assertEquals(initialComponents.size(), testComponents.size());
        for (int i = 0; i < testComponents.size(); ++i) {
            ServiceComponentDTO testComponent = testComponents.get(i);
            ServiceComponentDTO actualComponent = actualComponents.get(i);
            ServiceComponentDTO initialComponent = initialComponents.get(i);
            assertEquals(initialComponent.getId(), actualComponent.getId());
            assertEquals(testComponent.getStatusType(), actualComponent.getStatusType());
            assertEquals(testComponent.getStatusString(), actualComponent.getStatusString());
            assertEquals(initialComponent.getName(), actualComponent.getName());
        }
    }

    void checkStatus(ServiceStatusDTO testStatus, ServiceStatusDTO actualStatus) {
        assertEquals(testStatus.getId(), actualStatus.getId());
        assertEquals(testStatus.getStatusString(), actualStatus.getStatusString());
        assertEquals(testStatus.getStatusType(), actualStatus.getStatusType());
        assertEquals(testStatus.getDescription(), actualStatus.getDescription());
        assertEquals(testStatus.isCurrent(), actualStatus.isCurrent());
        assertWithin(testStatus.getUpdatedAt(), actualStatus.getUpdatedAt(), 500);
        assertEquals(testStatus.getUpdatedBy(), actualStatus.getUpdatedBy());
    }
}
