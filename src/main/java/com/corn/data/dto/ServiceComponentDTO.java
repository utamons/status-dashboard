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
public class ServiceComponentDTO {
    private Long id;
    private String name;
    private String statusString;
    private String statusType;
    private Instant updatedAt;
    private String updatedBy;

    @JsonCreator
    public ServiceComponentDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("name")
            String name,
            @JsonProperty("statusString")
            String statusString,
            @JsonProperty("statusType")
            String statusType,
            @JsonProperty("updatedAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant updatedAt,
            @JsonProperty("updatedBy")
            String updatedBy) {
        this.id = id;
        this.name = name;
        this.statusString = statusString;
        this.statusType = statusType;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatusString() {
        return statusString;
    }

    public String getStatusType() {
        return statusType;
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
        ServiceComponentDTO that = (ServiceComponentDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(statusString, that.statusString) &&
                Objects.equals(statusType, that.statusType) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withStatusString(String statusString) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withId(Long id) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withName(String name) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withName(Instant updatedAt) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withUpdatedBy(String updatedBy) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withUpdatedAt(Instant updatedAt) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }

    public ServiceComponentDTO withStatusType(String statusType) {
        return new ServiceComponentDTO(id, name, statusString, statusType, updatedAt, updatedBy);
    }
}
