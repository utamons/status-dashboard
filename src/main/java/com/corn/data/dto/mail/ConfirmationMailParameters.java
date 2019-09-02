package com.corn.data.dto.mail;

import java.util.Objects;

@SuppressWarnings("unused")
public class ConfirmationMailParameters implements MailParameters {
    private String url;

    public ConfirmationMailParameters(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmationMailParameters that = (ConfirmationMailParameters) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
