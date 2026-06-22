package com.devicemgmt.common.dto;

import java.io.Serializable;

public class LocationDTO implements Serializable {
    private int id;
    private String name;
    private String description;
    private String floor;
    private int deviceCount;

    public LocationDTO() {}

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public String getFloor()                    { return floor; }
    public void setFloor(String floor)          { this.floor = floor; }

    public int getDeviceCount()                 { return deviceCount; }
    public void setDeviceCount(int n)           { this.deviceCount = n; }

    @Override
    public String toString() { return name + (floor != null && !floor.isEmpty() ? " (" + floor + ")" : ""); }
}
