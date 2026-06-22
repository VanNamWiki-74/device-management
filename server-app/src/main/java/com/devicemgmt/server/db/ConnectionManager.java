package com.devicemgmt.server.db;

import com.devicemgmt.server.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private static ConnectionManager instance;
    private final BlockingQueue<Connection> pool;
    private final int poolSize;

    private ConnectionManager() {
        poolSize = AppConfig.getDbPoolSize();
        pool = new ArrayBlockingQueue<>(poolSize);
        initPool();
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) instance = new ConnectionManager();
        return instance;
    }

    private void initPool() {
        for (int i = 0; i < poolSize; i++) {
            try {
                pool.offer(createConnection());
            } catch (SQLException e) {
                log.error("Failed to create connection for pool: {}", e.getMessage());
            }
        }
        log.info("Connection pool initialized with {} connections", pool.size());
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.getJdbcUrl(),
            AppConfig.getDbUser(),
            AppConfig.getDbPassword()
        );
    }

    public Connection getConnection() throws SQLException {
        Connection conn = pool.poll();

        if (conn == null || conn.isClosed()) {
            conn = createConnection();
        }

        return conn;
    }
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    if (!conn.getAutoCommit()) conn.setAutoCommit(true);
                    if (!pool.offer(conn)) {
                        conn.close();
                    }
                }
            } catch (SQLException e) {
                log.warn("Error releasing connection: {}", e.getMessage());
            }
        }
    }

    public void shutdown() {
        pool.forEach(c -> {
            try { c.close(); } catch (SQLException ignored) {}
        });
        pool.clear();
        log.info("Connection pool shut down");
    }
}
