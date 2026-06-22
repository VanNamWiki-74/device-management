package com.devicemgmt.common.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private int id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean active;
    private String createdAt;
    private String token;

    public UserDTO() {}

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPhone()                     { return phone; }
    public void setPhone(String phone)           { this.phone = phone; }

    public String getRole()                      { return role; }
    public void setRole(String role)             { this.role = role; }

    public boolean isActive()                    { return active; }
    public void setActive(boolean active)        { this.active = active; }

    public String getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    public String getToken()                     { return token; }
    public void setToken(String token)           { this.token = token; }

    public boolean isAdmin() { return "ADMIN".equals(role); }

    @Override
    public String toString() { return fullName + " (" + username + ")"; }
}
