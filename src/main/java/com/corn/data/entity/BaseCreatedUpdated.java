package com.corn.data.entity;

import javax.persistence.*;
import java.time.Instant;

@MappedSuperclass
public class BaseCreatedUpdated {

    Instant createdAt;
    String createdBy;
    Instant updatedAt;
    String updatedBy;

    BaseCreatedUpdated() {
    }

    BaseCreatedUpdated(Instant createdAt, String createdBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    @Column(name = "CREATED_AT", nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "CREATED_BY", nullable = false)
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
}
