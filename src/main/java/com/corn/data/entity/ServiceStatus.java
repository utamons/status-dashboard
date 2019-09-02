package com.corn.data.entity;

import com.corn.data.dto.ServiceComponentDTO;
import com.corn.data.dto.ServiceEventDTO;
import com.corn.data.dto.ServiceStatusDTO;

import javax.persistence.*;
import java.time.Instant;
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
@Table(name = "SERVICE_STATUS")
public class ServiceStatus {

    private Long id;
    private String statusString;
    private String statusType;
    private String description;
    private ServiceEvent currentEvent;
    private boolean current;
    private Instant updatedAt;
    private String updatedBy;

    public ServiceStatus() {}

    public ServiceStatus(String statusString,
                         String statusType,
                         String description,
                         ServiceEvent currentEvent,
                         boolean current,
                         Instant updatedAt,
                         String updatedBy) {
        this.statusString = statusString;
        this.statusType = statusType;
        this.description = description;
        this.currentEvent = currentEvent;
        this.current = current;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static ServiceStatus getNormal(Instant updatedAt, String updatedBy) {
        return new ServiceStatus(
                "Service is operational",
                "normal",
                "Welcome to the Service Status Page. There you can see current information of the service performance. You can bookmark or subscribe to this page for the latest updates.",
                null,
                true,
                updatedAt,
                updatedBy
        );
    }

    @Id
    @SequenceGenerator(name = "SERVICE_STATUS_SEQ", sequenceName = "SEQ_SERVICE_STATUS", allocationSize=1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SERVICE_STATUS_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="STATUS_STRING", nullable = false)
    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    @Column(name="STATUS_TYPE", nullable = false)
    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    @Column(name="DESCRIPTION", nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "UPDATED_AT")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Column(name = "UPDATED_BY")
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }


    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "EVENT_ID")
    public ServiceEvent getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(ServiceEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    @Column(name="CURRENT_STATUS", nullable = false)
    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceStatus that = (ServiceStatus) o;
        return current == that.current &&
                Objects.equals(id, that.id) &&
                Objects.equals(statusString, that.statusString) &&
                Objects.equals(statusType, that.statusType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(currentEvent, that.currentEvent) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy);
    }

    @Transient
    public ServiceStatusDTO toValue(List<ServiceComponentDTO> components, List<ServiceEventDTO> history) {
        return new ServiceStatusDTO(
                id,
                statusString,
                statusType,
                description,
                currentEvent == null?null:currentEvent.toValue(),
                current,
                updatedAt,
                updatedBy,
                components,
                history
        );
    }
}
