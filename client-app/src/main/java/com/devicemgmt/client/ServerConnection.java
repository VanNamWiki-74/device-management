package com.devicemgmt.client;

import com.devicemgmt.client.config.ClientConfig;
import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);
    private static final ServerConnection INSTANCE = new ServerConnection();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private boolean connected = false;

    private ServerConnection() {}

    public static ServerConnection getInstance() { return INSTANCE; }

    public synchronized boolean connect() {
        try {
            if (connected && socket != null && !socket.isClosed()) return true;
            socket = new Socket(ClientConfig.getServerHost(), ClientConfig.getServerPort());
            socket.setSoTimeout(ClientConfig.getTimeoutMs());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            connected = true;
            log.info("Connected to server {}:{}", ClientConfig.getServerHost(), ClientConfig.getServerPort());
            return true;
        } catch (Exception e) {
            connected = false;
            log.error("Cannot connect to server: {}", e.getMessage());
            return false;
        }
    }

    public synchronized Response send(Request req) {
        if (!connected && !connect()) {
            return Response.error("Không thể kết nối đến server. Vui lòng kiểm tra kết nối.");
        }
        try {
            out.println(gson.toJson(req));
            String line = in.readLine();
            if (line == null) {
                connected = false;
                return Response.error("Server ngắt kết nối.");
            }
            return gson.fromJson(line, Response.class);
        } catch (Exception e) {
            connected = false;
            log.error("send error: {}", e.getMessage());
            return Response.error("Lỗi kết nối: " + e.getMessage());
        }
    }

    public synchronized void close() {
        try {
            connected = false;
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }

    public boolean isConnected() { return connected; }
}
