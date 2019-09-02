package com.corn.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.corn.data.InstantDeserializer;
import com.corn.data.InstantSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class ServiceStatusDTO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Long id;
    private String statusString;
    private String statusType;
    private String description;
    private ServiceEventDTO currentEvent;
    private boolean current;
    private Instant updatedAt;
    private String updatedBy;
    private List<ServiceComponentDTO> components;
    private List<ServiceEventDTO> history;

    @JsonCreator
    public ServiceStatusDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("statusString")
            String statusString,
            @JsonProperty("statusType")
            String statusType,
            @JsonProperty("description")
            String description,
            @JsonProperty("currentEvent")
            ServiceEventDTO currentEvent,
            @JsonProperty("current")
            boolean current,
            @JsonProperty("updatedAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant updatedAt,
            @JsonProperty("updatedBy")
            String updatedBy,
            @JsonProperty("components")
            List<ServiceComponentDTO> components,
            @JsonProperty("history")
            List<ServiceEventDTO> history
    ) {
        this.id = id;
        this.statusString = statusString;
        this.statusType = statusType;
        this.description = description;
        this.currentEvent = currentEvent;
        this.current = current;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.components = components;
        this.history = history;
    }

    public List<ServiceEventDTO> getHistory() {
        return history;
    }

    public List<ServiceComponentDTO> getComponents() {
        return components;
    }

    public Long getId() {
        return id;
    }

    public String getStatusString() {
        return statusString;
    }

    public String getStatusType() {
        return statusType;
    }

    public String getDescription() {
        return description;
    }

    public ServiceEventDTO getCurrentEvent() {
        return currentEvent;
    }

    public boolean isCurrent() {
        return current;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceStatusDTO that = (ServiceStatusDTO) o;
        return current == that.current &&
                Objects.equals(id, that.id) &&
                Objects.equals(statusString, that.statusString) &&
                Objects.equals(statusType, that.statusType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(currentEvent, that.currentEvent) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components);
    }

    @Override
    public String toString() {
        return "ServiceStatusDTO{" +
                "id=" + id +
                ", statusString='" + statusString + '\'' +
                ", statusType='" + statusType + '\'' +
                ", description='" + description + '\'' +
                ", currentEvent=" + currentEvent +
                ", current=" + current +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", components=" + components +
                ", history=" + history +
                '}';
    }

    public ServiceStatusDTO withId(Long id) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withComponents(List<ServiceComponentDTO> components) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withCurrentEvent(ServiceEventDTO currentEvent) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withCurrent(boolean current) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withUpdatedAt(Instant updatedAt) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withUpdatedBy(String updatedBy) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withStatusString(String statusString) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withStatusType(String statusType) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }

    public ServiceStatusDTO withDescription(String description) {
        return new ServiceStatusDTO(id, statusString, statusType, description, currentEvent, current, updatedAt, updatedBy, components, history);
    }
}
