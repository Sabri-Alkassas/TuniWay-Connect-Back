package com.tuniway.connect.model.dto;

import java.util.List;

public class AdminDashboardResponse {
    private boolean success;
    private String message;
    private long totalStaffAccounts;
    private long activeStaffAccounts;
    private long totalEmployeeAccounts;
    private long totalAdminAccounts;
    private long totalTransports;
    private long activeTransports;
    private long totalShifts;
    private long scheduledShifts;
    private long inProgressShifts;
    private long completedShifts;
    private List<AdminActivityResponse> recentActivity;

    public List<AdminActivityResponse> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<AdminActivityResponse> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTotalStaffAccounts() {
        return totalStaffAccounts;
    }

    public void setTotalStaffAccounts(long totalStaffAccounts) {
        this.totalStaffAccounts = totalStaffAccounts;
    }

    public long getActiveStaffAccounts() {
        return activeStaffAccounts;
    }

    public void setActiveStaffAccounts(long activeStaffAccounts) {
        this.activeStaffAccounts = activeStaffAccounts;
    }

    public long getTotalEmployeeAccounts() {
        return totalEmployeeAccounts;
    }

    public void setTotalEmployeeAccounts(long totalEmployeeAccounts) {
        this.totalEmployeeAccounts = totalEmployeeAccounts;
    }

    public long getTotalAdminAccounts() {
        return totalAdminAccounts;
    }

    public void setTotalAdminAccounts(long totalAdminAccounts) {
        this.totalAdminAccounts = totalAdminAccounts;
    }

    public long getTotalTransports() {
        return totalTransports;
    }

    public void setTotalTransports(long totalTransports) {
        this.totalTransports = totalTransports;
    }

    public long getActiveTransports() {
        return activeTransports;
    }

    public void setActiveTransports(long activeTransports) {
        this.activeTransports = activeTransports;
    }

    public long getTotalShifts() {
        return totalShifts;
    }

    public void setTotalShifts(long totalShifts) {
        this.totalShifts = totalShifts;
    }

    public long getScheduledShifts() {
        return scheduledShifts;
    }

    public void setScheduledShifts(long scheduledShifts) {
        this.scheduledShifts = scheduledShifts;
    }

    public long getInProgressShifts() {
        return inProgressShifts;
    }

    public void setInProgressShifts(long inProgressShifts) {
        this.inProgressShifts = inProgressShifts;
    }

    public long getCompletedShifts() {
        return completedShifts;
    }

    public void setCompletedShifts(long completedShifts) {
        this.completedShifts = completedShifts;
    }
}
