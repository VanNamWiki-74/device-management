package com.devicemgmt.common.dto;

import java.io.Serializable;

public class ApprovalDTO implements Serializable {
    private int id;
    private int requesterId;
    private String requesterName;
    private int deviceId;
    private String deviceCode;
    private String deviceName;
    private int categoryId;
    private String categoryName;
    private int approvalTypeId;
    private String approvalTypeName;
    private String description;
    private String status;
    private int approverId;
    private String approverName;
    private String comments;
    private boolean imported;
    private String requestDate;
    private String approvalDate;

    public ApprovalDTO() {}

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getRequesterId()                 { return requesterId; }
    public void setRequesterId(int requesterId) { this.requesterId = requesterId; }

    public String getRequesterName()                { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public int getDeviceId()                    { return deviceId; }
    public void setDeviceId(int deviceId)       { this.deviceId = deviceId; }

    public String getDeviceCode()               { return deviceCode; }
    public void setDeviceCode(String deviceCode){ this.deviceCode = deviceCode; }

    public String getDeviceName()               { return deviceName; }
    public void setDeviceName(String deviceName){ this.deviceName = deviceName; }

    public int getCategoryId()                  { return categoryId; }
    public void setCategoryId(int categoryId)   { this.categoryId = categoryId; }

    public String getCategoryName()                 { return categoryName; }
    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }

    public int getApprovalTypeId()                  { return approvalTypeId; }
    public void setApprovalTypeId(int approvalTypeId){ this.approvalTypeId = approvalTypeId; }

    public String getApprovalTypeName()                 { return approvalTypeName; }
    public void setApprovalTypeName(String approvalTypeName) { this.approvalTypeName = approvalTypeName; }

    public String getDescription()              { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus()               { return status; }
    public void setStatus(String status)    { this.status = status; }

    public int getApproverId()                  { return approverId; }
    public void setApproverId(int approverId)   { this.approverId = approverId; }

    public String getApproverName()                 { return approverName; }
    public void setApproverName(String approverName){ this.approverName = approverName; }

    public String getComments()             { return comments; }
    public void setComments(String comments){ this.comments = comments; }

    public boolean isImported()             { return imported; }
    public void setImported(boolean imported) { this.imported = imported; }

    public String getRequestDate()              { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getApprovalDate()             { return approvalDate; }
    public void setApprovalDate(String approvalDate) { this.approvalDate = approvalDate; }

    public String getStatusDisplay() {
        if ("APPROVED".equals(status)) return "Đã duyệt";
        if ("REJECTED".equals(status)) return "Từ chối";
        return "Chờ duyệt";
    }
}
