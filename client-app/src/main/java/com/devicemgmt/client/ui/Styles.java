package com.devicemgmt.client.ui;

public final class Styles {
    private Styles() {}

    // Colors
    public static final String PRIMARY      = "#2563EB";
    public static final String PRIMARY_DARK = "#1D4ED8";
    public static final String SUCCESS      = "#16A34A";
    public static final String DANGER       = "#DC2626";
    public static final String WARNING      = "#D97706";
    public static final String SIDEBAR_BG   = "#1E293B";
    public static final String SIDEBAR_HOVER= "#334155";
    public static final String SIDEBAR_ACTIVE="#2563EB";
    public static final String CONTENT_BG   = "#F1F5F9";
    public static final String CARD_BG      = "#FFFFFF";
    public static final String TEXT_PRIMARY = "#0F172A";
    public static final String TEXT_SECONDARY="#64748B";
    public static final String BORDER_COLOR = "#E2E8F0";
    public static final String HEADER_BG    = "#FFFFFF";

    public static String btn(String bg, String hover) {
        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 8 18 8 18;
            -fx-background-radius: 6;
            -fx-font-size: 13px;
            -fx-cursor: hand;
            """, bg);
    }

    public static final String BTN_PRIMARY = btn(PRIMARY, PRIMARY_DARK);
    public static final String BTN_SUCCESS = btn(SUCCESS, "#15803D");
    public static final String BTN_DANGER  = btn(DANGER, "#B91C1C");
    public static final String BTN_SECONDARY = """
        -fx-background-color: #F1F5F9;
        -fx-text-fill: #374151;
        -fx-padding: 8 18 8 18;
        -fx-background-radius: 6;
        -fx-font-size: 13px;
        -fx-cursor: hand;
        -fx-border-color: #D1D5DB;
        -fx-border-radius: 6;
        -fx-border-width: 1;
        """;

    public static final String TEXT_FIELD = """
        -fx-background-color: white;
        -fx-border-color: #D1D5DB;
        -fx-border-radius: 6;
        -fx-background-radius: 6;
        -fx-padding: 8 12 8 12;
        -fx-font-size: 13px;
        """;

    public static final String COMBO_BOX = """
        -fx-background-color: white;
        -fx-border-color: #D1D5DB;
        -fx-border-radius: 6;
        -fx-background-radius: 6;
        -fx-font-size: 13px;
        """;

    public static final String TABLE_VIEW = """
        -fx-background-color: white;
        -fx-border-color: #E2E8F0;
        -fx-border-width: 1;
        """;

    public static final String LABEL_FIELD = """
        -fx-font-size: 13px;
        -fx-text-fill: #374151;
        -fx-font-weight: bold;
        """;

    public static final String CARD = """
        -fx-background-color: white;
        -fx-background-radius: 10;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        -fx-padding: 20;
        """;

    public static String statusBadge(String status) {
        String color = switch (status) {
            case "AVAILABLE"   -> "#16A34A";
            case "IN_USE"      -> "#2563EB";
            case "MAINTENANCE" -> "#D97706";
            case "BROKEN"      -> "#DC2626";
            case "DISPOSED"    -> "#6B7280";
            case "ACTIVE"      -> "#2563EB";
            case "RETURNED"    -> "#16A34A";
            case "PENDING"     -> "#D97706";
            case "APPROVED"    -> "#16A34A";
            case "REJECTED"    -> "#DC2626";
            default            -> "#6B7280";
        };
        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 2 10 2 10;
            -fx-background-radius: 20;
            -fx-font-size: 11px;
            """, color);
    }
}
