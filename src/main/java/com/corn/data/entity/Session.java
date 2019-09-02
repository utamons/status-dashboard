package com.corn.data.entity;

import com.corn.data.dto.SessionDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

@SuppressWarnings("unused")
@Entity
@Table(name = "USER_SESSION")
public class Session {
    private Long id;
    private User user;
    private Instant createdAt;
    private Instant expiredAt;
    private String token;

    public Session() {
    }

    public Session(User user, Instant createdAt, Instant expiredAt, String token) {
        this.user = user;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.token = token;
    }

    @Id
    @SequenceGenerator(name = "SESSION_SEQ", sequenceName = "SEQ_USER_SESSION", allocationSize=1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SESSION_SEQ")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne()
    @JoinColumn(name = "USER_ID")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "CREATED_AT", nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "EXPIRED_AT", nullable = false)
    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    @Column(name = "TOKEN", nullable = false)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(id, session.id) &&
                Objects.equals(createdAt, session.createdAt) &&
                Objects.equals(expiredAt, session.expiredAt) &&
                Objects.equals(token, session.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, expiredAt, token);
    }

    @Transient
    public SessionDTO toValue() {
        return new SessionDTO(
                id,
                user.toValue(),
                createdAt,
                expiredAt,
                token
        );
    }
}
