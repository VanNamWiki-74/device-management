package com.devicemgmt.common.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DashboardDTO implements Serializable {
    private int totalDevices;
    private int availableDevices;
    private int inUseDevices;
    private int maintenanceDevices;
    private int brokenDevices;
    private int disposedDevices;
    private int totalCategories;
    private int totalLocations;
    private int totalUsers;
    private int activeAssignments;
    private List<Map<String, Object>> devicesByCategory;
    private List<Map<String, Object>> recentLogs;

    public DashboardDTO() {}

    public int getTotalDevices()                    { return totalDevices; }
    public void setTotalDevices(int n)              { this.totalDevices = n; }

    public int getAvailableDevices()                { return availableDevices; }
    public void setAvailableDevices(int n)          { this.availableDevices = n; }

    public int getInUseDevices()                    { return inUseDevices; }
    public void setInUseDevices(int n)              { this.inUseDevices = n; }

    public int getMaintenanceDevices()              { return maintenanceDevices; }
    public void setMaintenanceDevices(int n)        { this.maintenanceDevices = n; }

    public int getBrokenDevices()                   { return brokenDevices; }
    public void setBrokenDevices(int n)             { this.brokenDevices = n; }

    public int getDisposedDevices()                 { return disposedDevices; }
    public void setDisposedDevices(int n)           { this.disposedDevices = n; }

    public int getTotalCategories()                 { return totalCategories; }
    public void setTotalCategories(int n)           { this.totalCategories = n; }

    public int getTotalLocations()                  { return totalLocations; }
    public void setTotalLocations(int n)            { this.totalLocations = n; }

    public int getTotalUsers()                      { return totalUsers; }
    public void setTotalUsers(int n)                { this.totalUsers = n; }

    public int getActiveAssignments()               { return activeAssignments; }
    public void setActiveAssignments(int n)         { this.activeAssignments = n; }

    public List<Map<String, Object>> getDevicesByCategory()             { return devicesByCategory; }
    public void setDevicesByCategory(List<Map<String, Object>> list)    { this.devicesByCategory = list; }

    public List<Map<String, Object>> getRecentLogs()                    { return recentLogs; }
    public void setRecentLogs(List<Map<String, Object>> recentLogs)     { this.recentLogs = recentLogs; }
}
