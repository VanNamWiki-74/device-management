package com.devicemgmt.client.config;

import java.io.InputStream;
import java.util.Properties;

public class ClientConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ClientConfig.class.getClassLoader().getResourceAsStream("client.properties")) {
            if (is != null) props.load(is);
        } catch (Exception e) {
            System.err.println("Cannot load client.properties: " + e.getMessage());
        }
    }

    public static String getServerHost() {
        return props.getProperty("server.host", "localhost");
    }

    public static int getServerPort() {
        return Integer.parseInt(props.getProperty("server.port", "9000"));
    }

    public static int getTimeoutMs() {
        return Integer.parseInt(props.getProperty("server.timeout.ms", "10000"));
    }

    public static String getAppName() {
        return props.getProperty("app.name", "Quản lý thiết bị văn phòng");
    }
}
