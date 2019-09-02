package com.corn.data.dto.mail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class MailAuthDTO {

    private String username;
    private String password;
    private Boolean rememberMe;

    @JsonCreator
    public MailAuthDTO(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("rememberMe") Boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
