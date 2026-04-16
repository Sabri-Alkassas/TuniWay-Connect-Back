package com.tuniway.connect.model.dto;

public class AdminActivityResponse {
    private String id;
    private String action;
    private String detail;
    private String time;
    private String dot;

    public AdminActivityResponse() {}

    public AdminActivityResponse(String id, String action, String detail, String time, String dot) {
        this.id = id;
        this.action = action;
        this.detail = detail;
        this.time = time;
        this.dot = dot;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDot() { return dot; }
    public void setDot(String dot) { this.dot = dot; }
}
