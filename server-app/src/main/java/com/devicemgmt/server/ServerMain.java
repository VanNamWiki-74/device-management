package com.devicemgmt.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devicemgmt.server.config.AppConfig;
import com.devicemgmt.server.db.ConnectionManager;
import com.devicemgmt.server.handler.RequestRouter;
import com.devicemgmt.server.security.TokenManager;


public class ServerMain {
    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        int port = AppConfig.getServerPort();
        int maxClients = AppConfig.getMaxClients();
        int threadPoolSize = AppConfig.getThreadPoolSize();

        System.out.println(BCrypt.hashpw("Admin@123", BCrypt.gensalt(12)));
        System.out.println(BCrypt.hashpw("123456", BCrypt.gensalt(12)));

        log.info(BCrypt.hashpw("Admin@123", BCrypt.gensalt(12)));
        log.info(BCrypt.hashpw("123456", BCrypt.gensalt(12)));

        log.info("=================================================");
        log.info("  Device Management Server");
        log.info("  Port        : {}", port);
        log.info("  Max Clients : {}", maxClients);
        log.info("  Thread Pool : {}", threadPoolSize);
        log.info("  DB URL      : {}", AppConfig.getJdbcUrl());
        log.info("=================================================");

        // Init DB pool (fail fast)
        try {
            ConnectionManager.getInstance();
            log.info("Database connection pool initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database: {}", e.getMessage());
            System.exit(1);
        }

        // Periodically clean expired tokens
        java.util.concurrent.Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(TokenManager.getInstance()::cleanExpired, 30, 30, TimeUnit.MINUTES);

        RequestRouter router = new RequestRouter();
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        Semaphore semaphore = new Semaphore(maxClients);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Server shutting down...");
            executor.shutdown();
            ConnectionManager.getInstance().shutdown();
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Server is listening on port {}", port);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    semaphore.acquire();
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(() -> {
                        try {
                            new ClientHandler(clientSocket, router).run();
                        } finally {
                            semaphore.release();
                        }
                    });
                } catch (Exception e) {
                    log.warn("Accept error: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Server error: {}", e.getMessage());
        }
    }
}
