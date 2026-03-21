package com.tuniway.connect.model.dto;

public class VerifyTwoFactorRequest {
    private String tempToken;
    private String totpCode;

    public VerifyTwoFactorRequest() {
    }

    public VerifyTwoFactorRequest(String tempToken, String totpCode) {
        this.tempToken = tempToken;
        this.totpCode = totpCode;
    }

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }
}