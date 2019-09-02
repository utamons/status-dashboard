package com.corn.data.entity;

import com.corn.data.dto.AnnouncementDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

@SuppressWarnings("unused")
@Entity
@Table(name = "ANNOUNCEMENT")
public class Announcement extends BaseCreatedUpdated {
    private Long id;
    private Instant date;
    private String header;
    private String description;
    private boolean active;

    public Announcement() {}

    public Announcement(Instant date, String header, String description, boolean active, Instant createdAt, String createdBy) {
        super(createdAt,createdBy);
        this.date = date;
        this.header = header;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    @Id
    @SequenceGenerator(name = "ANNOUNCEMENT_SEQ", sequenceName = "SEQ_ANNOUNCEMENT", allocationSize=1)
    @GeneratedValue(strategy = SEQUENCE, generator = "ANNOUNCEMENT_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "ANNOUNCEMENT_DATE", nullable = false)
    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    @Column(name = "HEADER", nullable = false)
    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Column(name = "DESCRIPTION", nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "ACTIVE", nullable = false)
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
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

    @Transient
    public AnnouncementDTO toValue() {
        return new AnnouncementDTO(id, date, header, description, active, createdAt, createdBy, updatedAt, updatedBy);
    }
}
