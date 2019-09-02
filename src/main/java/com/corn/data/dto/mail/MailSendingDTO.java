package com.corn.data.dto.mail;

@SuppressWarnings("unused")
public class MailSendingDTO {
    private MailParameters parameters;
    private String to_email;
    private String to_name;

    public MailSendingDTO(MailParameters parameters, String to_email, String to_name) {
        this.parameters = parameters;
        this.to_email = to_email;
        this.to_name = to_name;
    }

    public MailParameters getParameters() {
        return parameters;
    }

    public String getTo_email() {
        return to_email;
    }

    public String getTo_name() {
        return to_name;
    }

}
