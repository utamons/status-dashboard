package com.corn.data.entity;

import com.corn.data.dto.ServiceComponentDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

@SuppressWarnings("unused")
@Entity
@Table(name = "SERVICE_COMPONENT")
public class ServiceComponent {
    private Long id;
    private String name;
    private String statusString;
    private String statusType;
    private Instant updatedAt;
    private String updatedBy;

    @Id
    @SequenceGenerator(name = "SERVICE_COMPONENT_SEQ", sequenceName = "SEQ_SERVICE_COMPONENT", allocationSize=1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SERVICE_COMPONENT_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "STATUS_STRING", nullable = false)
    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    @Column(name = "STATUS_TYPE", nullable = false)
    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceComponent that = (ServiceComponent) o;
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

    @Transient
    public ServiceComponentDTO toValue() {
        return new ServiceComponentDTO(
                id,
                name,
                statusString,
                statusType,
                updatedAt,
                updatedBy
        );
    }
}
