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
public class SessionDTO {
    private Long id;
    private UserDTO user;
    private Instant createdAt;
    private Instant expiredAt;
    private String token;

    @JsonCreator
    public SessionDTO(
            @JsonProperty("id")
            Long id,
            @JsonProperty("user")
            UserDTO user,
            @JsonProperty("createdAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant createdAt,
            @JsonProperty("expiredAt")
            @JsonDeserialize(using = InstantDeserializer.class)
            Instant expiredAt,
            @JsonProperty("token")
            String token) {
        this.id = id;
        this.user = user;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public UserDTO getUser() {
        return user;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getCreatedAt() {
        return createdAt;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getExpiredAt() {
        return expiredAt;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionDTO that = (SessionDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(expiredAt, that.expiredAt) &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, createdAt, expiredAt, token);
    }

    @Override
    public String toString() {
        return "SessionDTO{" +
                "id=" + id +
                ", user=" + user.toString() +
                ", createdAt=" + createdAt +
                ", expiredAt=" + expiredAt +
                ", token='" + token + '\'' +
                '}';
    }

    public SessionDTO withExpiredAt(Instant expiredAt) {
        return new SessionDTO(id, user, createdAt, expiredAt, token);
    }
}
