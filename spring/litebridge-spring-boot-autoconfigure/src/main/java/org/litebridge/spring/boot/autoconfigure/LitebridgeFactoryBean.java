package org.litebridge.spring.boot.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.spring.LitebridgeEntityScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.invoke.MethodHandles;

/**
 * Factory bean that creates a specific type of Litebridge bean based on the configured database provider.
 */
public class LitebridgeFactoryBean implements FactoryBean<LitebridgeCore> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LitebridgeFactoryBean.class);

    private final DatabaseProvider databaseProvider;
    private final TransactionManager transactionManager;
    private final LitebridgeProperties properties;
    private final @Nullable LitebridgeConfigurer configurer;

    /**
     * Creates a new {@code LitebridgeFactoryBean} instance.
     *
     * @param databaseProvider   The configured database provider.
     * @param transactionManager The configured transaction manager.
     * @param properties         Litebridge properties.
     * @param configurer         Optional custom Litebridge configurer.
     */
    public LitebridgeFactoryBean(final DatabaseProvider databaseProvider,
                                 final TransactionManager transactionManager,
                                 final LitebridgeProperties properties,
                                 final @Nullable LitebridgeConfigurer configurer) {
        this.databaseProvider = databaseProvider;
        this.transactionManager = transactionManager;
        this.properties = properties;
        this.configurer = configurer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LitebridgeCore getObject() throws Exception {
        LOGGER.trace("Creating Litebridge instance with database provider: {}, transaction manager: {}", databaseProvider.getClass().getName(), transactionManager.getClass().getName());
        final LitebridgeConfig litebridgeConfig = new LitebridgeConfig(properties.getRelatedDtoStrategy());

        final LitebridgeBuilder<?> litebridgeBuilder;

        if (databaseProvider instanceof LitebridgeOverrideDatabaseProvider<?> litebridgeOverrideDatabaseProvider) {
            litebridgeBuilder = Litebridge.withDatabase((LitebridgeOverrideDatabaseProvider<? extends LitebridgeCore>) litebridgeOverrideDatabaseProvider);
        } else {
            litebridgeBuilder = Litebridge.withDatabase(databaseProvider);
        }

        final LitebridgeCore litebridge = litebridgeBuilder
                .withTransactionManager(transactionManager)
                .withConfig(litebridgeConfig)
                .withLookup(MethodHandles.lookup())
                .build();
        final String[] scanBasePackages = properties.getScanBasePackage();

        if (scanBasePackages != null) {
            final Class<?>[] entityClasses = new LitebridgeEntityScanner().scanBasePackage(scanBasePackages);
            LOGGER.debug("Found {} entity classes after scanning base packages: {}", entityClasses.length, scanBasePackages);
            LOGGER.trace("Found entity classes: {}", (Object) entityClasses);

            if (entityClasses.length > 0) {
                litebridge.register(entityClasses);
            }
        }

        if (configurer != null) {
            LOGGER.trace("Applying LitebridgeConfigurer: {}", configurer.getClass().getName());
            configurer.configure(litebridge);
        }

        LOGGER.trace("Litebridge Spring Boot autoconfiguration complete");
        return litebridge;
    }

    @Override
    public Class<?> getObjectType() {
        if (databaseProvider instanceof LitebridgeOverrideDatabaseProvider<?> litebridgeOverrideDatabaseProvider) {
            return litebridgeOverrideDatabaseProvider.litebridgeClass();
        }

        return Litebridge.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
