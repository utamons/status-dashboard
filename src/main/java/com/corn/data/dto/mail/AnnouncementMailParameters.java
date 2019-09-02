package com.corn.data.dto.mail;

@SuppressWarnings("unused")
public class AnnouncementMailParameters implements MailParameters {
    private String header,announcement,url;

    public AnnouncementMailParameters(String header, String announcement, String url) {
        this.header = header;
        this.announcement = announcement;
        this.url = url;
    }

    public String getHeader() {
        return header;
    }

    public String getAnnouncement() {
        return announcement;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
