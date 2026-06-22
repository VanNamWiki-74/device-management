package com.devicemgmt.server.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (is != null) props.load(is);
        } catch (Exception e) {
            System.err.println("Cannot load server.properties: " + e.getMessage());
        }
    }

    public static int getServerPort() {
        String env = System.getenv("SERVER_PORT");
        if (env != null) return Integer.parseInt(env);
        return Integer.parseInt(props.getProperty("server.port", "9000"));
    }

    public static int getMaxClients() {
        String env = System.getenv("SERVER_MAX_CLIENTS");
        if (env != null) return Integer.parseInt(env);
        return Integer.parseInt(props.getProperty("server.max.clients", "50"));
    }

    public static int getThreadPoolSize() {
        return Integer.parseInt(props.getProperty("server.thread.pool.size", "20"));
    }

    public static String getDbHost() {
        String env = System.getenv("DB_HOST");
        return env != null ? env : props.getProperty("db.host", "localhost");
    }

    public static int getDbPort() {
        String env = System.getenv("DB_PORT");
        if (env != null) return Integer.parseInt(env);
        return Integer.parseInt(props.getProperty("db.port", "5432"));
    }

    public static String getDbName() {
        String env = System.getenv("DB_NAME");
        return env != null ? env : props.getProperty("db.name", "device_management");
    }

    public static String getDbUser() {
        String env = System.getenv("DB_USER");
        return env != null ? env : props.getProperty("db.user", "devmgmt");
    }

    public static String getDbPassword() {
        String env = System.getenv("DB_PASSWORD");
        return env != null ? env : props.getProperty("db.password", "devmgmt@123");
    }

    public static int getDbPoolSize() {
        return Integer.parseInt(props.getProperty("db.pool.size", "10"));
    }

    public static int getTokenExpiryHours() {
        return Integer.parseInt(props.getProperty("security.token.expiry.hours", "8"));
    }

    public static int getMaxFailedLogin() {
        return Integer.parseInt(props.getProperty("security.max.failed.login", "5"));
    }

    public static int getLockoutMinutes() {
        return Integer.parseInt(props.getProperty("security.lockout.minutes", "15"));
    }

    public static String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", getDbHost(), getDbPort(), getDbName());
    }
}
