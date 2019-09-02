package com.corn.data.entity;

import com.corn.data.dto.EventUpdateDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

@SuppressWarnings("unused")
@Entity
@Table(name = "EVENT_UPDATE")
public class EventUpdate extends BaseCreatedUpdated {
    private Long id;
    private Instant date;
    private String type;
    private ServiceEvent event;
    private String message;

    public EventUpdate() {
    }

    public EventUpdate(Instant date, String type, ServiceEvent event, String message, Instant createdAt, String createdBy) {
        super(createdAt,createdBy);
        this.date = date;
        this.type = type;
        this.event = event;
        this.message = message;
    }

    @Id
    @SequenceGenerator(name = "EVENT_UPDATE_SEQ", sequenceName = "SEQ_EVENT_UPDATE", allocationSize=1)
    @GeneratedValue(strategy = SEQUENCE, generator = "EVENT_UPDATE_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "UPDATE_DATE", nullable = false)
    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    @Column(name = "TYPE", nullable = false)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "EVENT_ID")
    public ServiceEvent getEvent() {
        return event;
    }

    public void setEvent(ServiceEvent event) {
        this.event = event;
    }

    @Column(name = "MESSAGE", nullable = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventUpdate that = (EventUpdate) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(date, that.date) &&
                Objects.equals(type, that.type) &&
                Objects.equals(event.getId(), that.event.getId()) &&
                Objects.equals(message, that.message) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, type, event.getId(), message, createdAt, createdBy, updatedAt, updatedBy);
    }

    @Transient
    EventUpdateDTO toValue() {
        return new EventUpdateDTO(
                id,
                date,
                type,
                event.getId(),
                message,
                createdAt,
                createdBy,
                updatedAt,
                updatedBy
        );
    }
}
