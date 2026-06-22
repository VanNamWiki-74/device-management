package com.devicemgmt.common.dto;

import java.io.Serializable;

public class CategoryDTO implements Serializable {
    private int id;
    private String name;
    private String description;
    private int deviceCount;

    public CategoryDTO() {}

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public int getDeviceCount()                 { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }

    @Override
    public String toString() { return name; }
}
