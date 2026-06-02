package org.litebridgedb.orm.e2e.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton manager for the PostgreSQL testcontainer.
 * Ensures the container starts only once and is reused across all tests.
 */
public class PostgresContainerManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresContainerManager.class);
    private static final PostgresContainerManager INSTANCE = new PostgresContainerManager();
    
    private final PostgreSQLContainer<?> container;
    private volatile boolean started = false;
    
    private PostgresContainerManager() {
        LOGGER.info("Initializing PostgreSQL testcontainer singleton");
        this.container = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("LB")
                .withPassword("password")
                .withReuse(true);
        
        // Register shutdown hook to stop container when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down PostgreSQL testcontainer");
            stop();
        }));
    }
    
    public static PostgresContainerManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Starts the PostgreSQL container if not already started.
     * This method is thread-safe.
     */
    public synchronized void start() {
        if (!started) {
            LOGGER.info("Starting PostgreSQL testcontainer (first use)");
            container.start();
            started = true;
            LOGGER.info("PostgreSQL testcontainer started at: {}", container.getJdbcUrl());
        } else {
            LOGGER.debug("PostgreSQL testcontainer already started, reusing existing instance");
        }
    }
    
    /**
     * Stops the PostgreSQL container.
     * Note: This is typically called only on JVM shutdown.
     */
    public synchronized void stop() {
        if (started) {
            LOGGER.info("Stopping PostgreSQL testcontainer");
            container.stop();
            started = false;
        }
    }
    
    public PostgreSQLContainer<?> getContainer() {
        if (!started) {
            throw new IllegalStateException("Container has not been started. Call start() first.");
        }
        return container;
    }
    
    public boolean isStarted() {
        return started;
    }
}
