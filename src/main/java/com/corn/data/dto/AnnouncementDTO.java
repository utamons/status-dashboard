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
public class AnnouncementDTO {
    private Long id;
    @Deprecated
    private Instant date;
    private String header;
    private String description;
    private boolean active;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    @JsonCreator
    public AnnouncementDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("date")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant date,
            @JsonProperty("header")
            String header,
            @JsonProperty("description")
            String description,
            @JsonProperty("active")
            boolean active,
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
        this.header = header;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    @Deprecated
    @JsonSerialize(using = InstantSerializer.class)
    public Instant getDate() {
        return date;
    }

    public String getHeader() {
        return header;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
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
        AnnouncementDTO that = (AnnouncementDTO) o;
        return active == that.active &&
                Objects.equals(id, that.id) &&
                Objects.equals(date, that.date) &&
                Objects.equals(header, that.header) &&
                Objects.equals(description, that.description) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withId(Long id) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withCreatedBy(String createdBy) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withCreatedAt(Instant createdAt) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withUpdatedAt(Instant updatedAt) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withUpdatedBy(String updatedBy) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withActive(boolean active) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withHeader(String header) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }

    public AnnouncementDTO withDescription(String description) {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }
}
