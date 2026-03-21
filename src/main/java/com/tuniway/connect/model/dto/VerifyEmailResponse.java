package com.tuniway.connect.model.dto;

public class VerifyEmailResponse {
    private String email;
    private boolean verified;
    private String message;

    public VerifyEmailResponse() {
    }

    public VerifyEmailResponse(String email, boolean verified, String message) {
        this.email = email;
        this.verified = verified;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
