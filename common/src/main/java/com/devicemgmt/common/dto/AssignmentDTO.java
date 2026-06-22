package com.devicemgmt.common.dto;

import java.io.Serializable;

public class AssignmentDTO implements Serializable {
    private int id;
    private int deviceId;
    private String deviceCode;
    private String deviceName;
    private String assignedTo;
    private int userId;
    private String department;
    private int assignedBy;
    private String assignedByName;
    private String assignedDate;
    private String expectedReturn;
    private String returnedDate;
    private String status;
    private String notes;
    private String createdAt;

    public AssignmentDTO() {}

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getDeviceId()                { return deviceId; }
    public void setDeviceId(int deviceId)   { this.deviceId = deviceId; }

    public String getDeviceCode()               { return deviceCode; }
    public void setDeviceCode(String deviceCode){ this.deviceCode = deviceCode; }

    public String getDeviceName()               { return deviceName; }
    public void setDeviceName(String deviceName){ this.deviceName = deviceName; }

    public int getUserId()                  { return userId; }
    public void setUserId(int userId)       { this.userId = userId; }

    public String getAssignedTo()               { return assignedTo; }
    public void setAssignedTo(String assignedTo){ this.assignedTo = assignedTo; }

    public String getDepartment()               { return department; }
    public void setDepartment(String department){ this.department = department; }

    public int getAssignedBy()                  { return assignedBy; }
    public void setAssignedBy(int assignedBy)   { this.assignedBy = assignedBy; }

    public String getAssignedByName()               { return assignedByName; }
    public void setAssignedByName(String name)      { this.assignedByName = name; }

    public String getAssignedDate()                 { return assignedDate; }
    public void setAssignedDate(String assignedDate){ this.assignedDate = assignedDate; }

    public String getExpectedReturn()               { return expectedReturn; }
    public void setExpectedReturn(String d)         { this.expectedReturn = d; }

    public String getReturnedDate()                 { return returnedDate; }
    public void setReturnedDate(String d)           { this.returnedDate = d; }

    public String getStatus()               { return status; }
    public void setStatus(String status)    { this.status = status; }

    public String getNotes()                { return notes; }
    public void setNotes(String notes)      { this.notes = notes; }

    public String getCreatedAt()            { return createdAt; }
    public void setCreatedAt(String c)      { this.createdAt = c; }

    public String getStatusDisplay() {
        return "ACTIVE".equals(status) ? "Đang sử dụng" : "Đã trả";
    }
}
