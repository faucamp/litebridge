package org.litebridgedb.orm.e2e.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.OracleContainer;

/**
 * Singleton manager for the Oracle testcontainer.
 * Ensures the container starts only once and is reused across all tests.
 */
public class OracleContainerManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleContainerManager.class);
    private static final OracleContainerManager INSTANCE = new OracleContainerManager();
    
    private final OracleContainer container;
    private volatile boolean started = false;
    
    private OracleContainerManager() {
        LOGGER.info("Initializing Oracle testcontainer singleton");
        this.container = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
                .withDatabaseName("testdb")
                .withUsername("LB")
                .withPassword("password")
                .withReuse(true); // Enable container reuse if Testcontainers supports it
        
        // Register shutdown hook to stop container when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down Oracle testcontainer");
            stop();
        }));
    }
    
    public static OracleContainerManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Starts the Oracle container if not already started.
     * This method is thread-safe.
     */
    public synchronized void start() {
        if (!started) {
            LOGGER.info("Starting Oracle testcontainer (first use)");
            container.start();
            started = true;
            LOGGER.info("Oracle testcontainer started at: {}", container.getJdbcUrl());
        } else {
            LOGGER.debug("Oracle testcontainer already started, reusing existing instance");
        }
    }
    
    /**
     * Stops the Oracle container.
     * Note: This is typically called only on JVM shutdown.
     */
    public synchronized void stop() {
        if (started) {
            LOGGER.info("Stopping Oracle testcontainer");
            container.stop();
            started = false;
        }
    }
    
    public OracleContainer getContainer() {
        if (!started) {
            throw new IllegalStateException("Container has not been started. Call start() first.");
        }
        return container;
    }
    
    public boolean isStarted() {
        return started;
    }
}