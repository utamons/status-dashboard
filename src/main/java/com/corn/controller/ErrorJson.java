package com.corn.controller;

import java.util.Map;

/**
 * @author Oleg Zaidullin
 */
@SuppressWarnings("unused")
public class ErrorJson {

    private Integer status;
    private String  error;
    private String  message;
    private String  timeStamp;
    private String  trace;

    public ErrorJson() {
    }

    ErrorJson(int status, Map<String, Object> errorAttributes) {
        this.status = status;
        this.error = (String) errorAttributes.get("error");
        this.message = (String) errorAttributes.get("message");
        this.timeStamp = errorAttributes.get("timestamp").toString();
        this.trace = (String) errorAttributes.get("trace");
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    @Override
    public String toString() {
        return "<html><body><ul>" +
                "<li>error: '" + error + '\'' +
                "<li>message: '" + message + '\'' +
                "</ul></body></html>";
    }
}
