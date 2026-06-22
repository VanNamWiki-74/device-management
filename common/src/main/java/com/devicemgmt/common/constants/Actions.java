package com.devicemgmt.common.constants;

public final class Actions {
    private Actions() {}

    // Auth
    public static final String LOGIN          = "LOGIN";
    public static final String LOGOUT         = "LOGOUT";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    // Device
    public static final String GET_DEVICES    = "GET_DEVICES";
    public static final String GET_DEVICE     = "GET_DEVICE";
    public static final String CREATE_DEVICE  = "CREATE_DEVICE";
    public static final String UPDATE_DEVICE  = "UPDATE_DEVICE";
    public static final String DELETE_DEVICE  = "DELETE_DEVICE";
    public static final String EXPORT_DEVICES = "EXPORT_DEVICES";
    public static final String IMPORT_DEVICES = "IMPORT_DEVICES";

    // Category
    public static final String GET_CATEGORIES   = "GET_CATEGORIES";
    public static final String CREATE_CATEGORY  = "CREATE_CATEGORY";
    public static final String UPDATE_CATEGORY  = "UPDATE_CATEGORY";
    public static final String DELETE_CATEGORY  = "DELETE_CATEGORY";

    // Location
    public static final String GET_LOCATIONS   = "GET_LOCATIONS";
    public static final String CREATE_LOCATION = "CREATE_LOCATION";
    public static final String UPDATE_LOCATION = "UPDATE_LOCATION";
    public static final String DELETE_LOCATION = "DELETE_LOCATION";

    // Assignment
    public static final String GET_ASSIGNMENTS   = "GET_ASSIGNMENTS";
    public static final String CREATE_ASSIGNMENT = "CREATE_ASSIGNMENT";
    public static final String UPDATE_ASSIGNMENT = "UPDATE_ASSIGNMENT";
    public static final String DELETE_ASSIGNMENT = "DELETE_ASSIGNMENT";
    public static final String RETURN_DEVICE     = "RETURN_DEVICE";

    // Approval
    public static final String GET_APPROVALS          = "GET_APPROVALS";
    public static final String GET_APPROVAL_TYPES      = "GET_APPROVAL_TYPES";
    public static final String CREATE_APPROVAL         = "CREATE_APPROVAL";
    public static final String PROCESS_APPROVAL        = "PROCESS_APPROVAL";
    public static final String MARK_APPROVAL_IMPORTED  = "MARK_APPROVAL_IMPORTED";

    // User management
    public static final String GET_USERS   = "GET_USERS";
    public static final String CREATE_USER = "CREATE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String TOGGLE_USER = "TOGGLE_USER";

    // Dashboard & Logs
    public static final String GET_DASHBOARD = "GET_DASHBOARD";
    public static final String GET_LOGS      = "GET_LOGS";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER  = "USER";

    // Device statuses
    public static final String STATUS_AVAILABLE   = "AVAILABLE";
    public static final String STATUS_IN_USE      = "IN_USE";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String STATUS_BROKEN      = "BROKEN";
    public static final String STATUS_DISPOSED    = "DISPOSED";
}
