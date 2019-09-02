package com.corn.data.dto.mail;

@SuppressWarnings("unused")
public class StatusMailParameters implements MailParameters {
    private String type,header,components,update,url;

    public StatusMailParameters(String type, String header, String components, String update, String url) {
        this.type = type;
        this.header = header;
        this.components = components;
        this.update = update;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public String getComponents() {
        return components;
    }

    public String getUpdate() {
        return update;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
