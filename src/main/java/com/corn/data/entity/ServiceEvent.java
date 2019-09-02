package com.corn.data.entity;

import com.corn.data.dto.EventUpdateDTO;
import com.corn.data.dto.ServiceEventDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * ServiceEvent entity
 *
 * @author Oleg Zaidullin
 */
@SuppressWarnings("unused")
@Entity
@Table(name = "SERVICE_EVENT")
public class ServiceEvent extends BaseCreatedUpdated {

    private Long id;
    private Instant eventDate;
    private String statusString;
    private String eventType;
    private String description;
    private String componentsString;
    private boolean resolved;
    private List<EventUpdate> history;

    public ServiceEvent() {
    }

    public ServiceEvent(Instant eventDate, String statusString, String eventType, String description, String componentsString, boolean resolved, Instant createdAt, String createdBy) {
        super(createdAt,createdBy);
        this.eventDate = eventDate;
        this.statusString = statusString;
        this.eventType = eventType;
        this.description = description;
        this.componentsString = componentsString;
        this.resolved = resolved;
    }

    @Id
    @SequenceGenerator(name = "SERVICE_EVENT_SEQ", sequenceName = "SEQ_SERVICE_EVENT", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SERVICE_EVENT_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "EVENT_DATE", nullable = false)
    public Instant getEventDate() {
        return eventDate;
    }

    public void setEventDate(Instant eventDate) {
        this.eventDate = eventDate;
    }

    @Column(name = "STATUS_STRING", nullable = false)
    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    @Column(name = "EVENT_TYPE", nullable = false)
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Column(name = "DESCRIPTION", nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "RESOLVED", nullable = false)
    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "event")
    public List<EventUpdate> getHistory() {
        return history;
    }

    public void setHistory(List<EventUpdate> history) {
        this.history = history;
    }

    @Column(name = "COMPONENTS_STRING", nullable = false)
    public String getComponentsString() {
        return componentsString;
    }

    public void setComponentsString(String componentsString) {
        this.componentsString = componentsString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEvent that = (ServiceEvent) o;
        return resolved == that.resolved &&
                Objects.equals(id, that.id) &&
                Objects.equals(eventDate, that.eventDate) &&
                Objects.equals(statusString, that.statusString) &&
                Objects.equals(eventType, that.eventType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(componentsString, that.componentsString) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    @Transient
    public ServiceEventDTO toValue() {
        List<EventUpdateDTO> updates = new ArrayList<>();
        if (this.history != null) {
            this.history.forEach(u -> updates.add(u.toValue()));
        }

        return new ServiceEventDTO(
                id,
                eventDate,
                statusString,
                eventType,
                description,
                componentsString,
                resolved,
                createdAt,
                createdBy,
                updatedAt,
                updatedBy,
                updates
        );
    }
}
