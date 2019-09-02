package com.corn.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.corn.data.InstantDeserializer;
import com.corn.data.InstantSerializer;

import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("unused")
public class EventUpdateDTO {
    private Long id;
    private Instant date;
    private String type;
    private Long eventId;
    private String message;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    @JsonCreator
    public EventUpdateDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("date")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant date,
            @JsonProperty("type")
            String type,
            @JsonProperty("eventId")
            Long eventId,
            @JsonProperty("message")
            String message,
            @JsonProperty("createdAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant createdAt,
            @JsonProperty("createdBy")
            String createdBy,
            @JsonProperty("updatedAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant updatedAt,
            @JsonProperty("updatedBy")
            String updatedBy) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.eventId = eventId;
        this.message = message;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getMessage() {
        return message;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventUpdateDTO that = (EventUpdateDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(date, that.date) &&
                Objects.equals(type, that.type) &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withId(Long id) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withEventId(Long eventId) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }


    public EventUpdateDTO withCreatedBy(String createdBy) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withUpdatedBy(String updatedBy) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withCreatedAt(Instant createdAt) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withUpdatedAt(Instant updatedAt) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withDate(Instant date) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withType(String type) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }

    public EventUpdateDTO withMessage(String message) {
        return new EventUpdateDTO(id, date, type, eventId, message, createdAt, createdBy, updatedAt, updatedBy);
    }
}
