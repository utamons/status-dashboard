package com.corn.service.mock;

import java.util.Map;

public class MockEmail {
    private final String email;
    private final String type;
    private final String unsubscribeUrl;
    private final Map<String,String> properties;

    MockEmail(String email, String type, String unsubscribeUrl, Map<String, String> properties) {
        this.email = email;
        this.type = type;
        this.unsubscribeUrl = unsubscribeUrl;
        this.properties = properties;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public String getUnsubscribeUrl() {
        return unsubscribeUrl;
    }

    public String get(String key) {
        return properties.get(key);
    }
}
