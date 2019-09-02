package com.corn.data.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.corn.data.InstantDeserializer;
import com.corn.data.InstantSerializer;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class ServiceEventDTO {

    private Long id;
    private Instant eventDate;
    private String statusString;
    private String eventType;
    private String description;
    private String componentsString;
    private boolean resolved;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private List<EventUpdateDTO> history;

    @JsonCreator
    public ServiceEventDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("eventDate")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant eventDate,
            @JsonProperty("statusString")
            String statusString,
            @JsonProperty("eventType")
            String eventType,
            @JsonProperty("description")
            String description,
            @JsonProperty("componentsString")
            String componentsString,
            @JsonProperty("resolved")
            boolean resolved,
            @JsonProperty("createdAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant createdAt,
            @JsonProperty("createdBy")
            String createdBy,
            @JsonProperty("updatedAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant updatedAt,
            @JsonProperty("updatedBy")
            String updatedBy,
            @JsonProperty("history")
            List<EventUpdateDTO> history) {

        this.id = id;
        this.eventDate = eventDate;
        this.statusString = statusString;
        this.eventType = eventType;
        this.description = description;
        this.componentsString = componentsString;
        this.resolved = resolved;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.history = history;

    }


    public Long getId() {
        return id;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getEventDate() {
        return eventDate;
    }

    public String getStatusString() {
        return statusString;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getComponentsString() {
        return componentsString;
    }

    public boolean isResolved() {
        return resolved;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public List<EventUpdateDTO> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEventDTO that = (ServiceEventDTO) o;
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
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(history, that.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withId(Long id) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withHistory(List<EventUpdateDTO> history) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withResolved(boolean resolved) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withCreatedAt(Instant createdAt) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withUpdatedAt(Instant updatedAt) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withCreatedBy(String createdBy) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withUpdatedBy(String updatedBy) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withEventDate(Instant eventDate) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withStatusString(String statusString) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withEventType(String eventType) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withDescription(String description) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }

    public ServiceEventDTO withComponentsString(String componentsString) {
        return new ServiceEventDTO(id, eventDate, statusString, eventType, description, componentsString, resolved, createdAt, createdBy, updatedAt, updatedBy, history);
    }
}
