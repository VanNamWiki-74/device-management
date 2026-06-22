package com.devicemgmt.server;

import com.devicemgmt.common.dto.Request;
import com.devicemgmt.common.dto.Response;
import com.devicemgmt.server.handler.RequestRouter;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket socket;
    private final RequestRouter router;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket, RequestRouter router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        String clientAddr = socket.getRemoteSocketAddress().toString();
        log.info("Client connected: {}", clientAddr);
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Request req = gson.fromJson(line, Request.class);
                    Response resp = router.route(req);
                    out.println(gson.toJson(resp));
                    log.debug("Action={} success={}", req.getAction(), resp.isSuccess());
                } catch (Exception e) {
                    log.warn("Error processing request from {}: {}", clientAddr, e.getMessage());
                    out.println(gson.toJson(Response.error("Lỗi xử lý yêu cầu: " + e.getMessage())));
                }
            }
        } catch (Exception e) {
            log.info("Client disconnected: {} ({})", clientAddr, e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
