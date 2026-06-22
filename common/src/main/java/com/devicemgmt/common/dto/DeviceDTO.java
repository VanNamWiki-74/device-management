package com.devicemgmt.common.dto;

import java.io.Serializable;

public class DeviceDTO implements Serializable {
    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String categoryName;
    private int locationId;
    private String locationName;
    private String brand;
    private String model;
    private String serialNumber;
    private String status;
    private String purchaseDate;
    private String warrantyExpiry;
    private double purchasePrice;
    private String notes;
    private String createdAt;
    private String updatedAt;

    public DeviceDTO() {}

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getCode()                     { return code; }
    public void setCode(String code)            { this.code = code; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public int getCategoryId()                      { return categoryId; }
    public void setCategoryId(int categoryId)       { this.categoryId = categoryId; }

    public String getCategoryName()                 { return categoryName; }
    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }

    public int getLocationId()                      { return locationId; }
    public void setLocationId(int locationId)       { this.locationId = locationId; }

    public String getLocationName()                 { return locationName; }
    public void setLocationName(String locationName){ this.locationName = locationName; }

    public String getBrand()                    { return brand; }
    public void setBrand(String brand)          { this.brand = brand; }

    public String getModel()                    { return model; }
    public void setModel(String model)          { this.model = model; }

    public String getSerialNumber()                 { return serialNumber; }
    public void setSerialNumber(String serialNumber){ this.serialNumber = serialNumber; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }

    public String getPurchaseDate()                 { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate){ this.purchaseDate = purchaseDate; }

    public String getWarrantyExpiry()               { return warrantyExpiry; }
    public void setWarrantyExpiry(String d)         { this.warrantyExpiry = d; }

    public double getPurchasePrice()                { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice){ this.purchasePrice = purchasePrice; }

    public String getNotes()                    { return notes; }
    public void setNotes(String notes)          { this.notes = notes; }

    public String getCreatedAt()                { return createdAt; }
    public void setCreatedAt(String createdAt)  { this.createdAt = createdAt; }

    public String getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(String updatedAt)  { this.updatedAt = updatedAt; }

    public String getStatusDisplay() {
        return switch (status) {
            case "AVAILABLE"   -> "Sẵn sàng";
            case "IN_USE"      -> "Đang dùng";
            case "MAINTENANCE" -> "Bảo trì";
            case "BROKEN"      -> "Hỏng";
            case "DISPOSED"    -> "Thanh lý";
            default            -> status;
        };
    }
}
