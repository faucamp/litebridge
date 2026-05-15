package org.litebridgedb.spring.boot.autoconfigure;

import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.spring.LitebridgeTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Set;

/**
 * Litebridge Spring Boot autoconfiguration
 */
@Configuration
@ConditionalOnClass(Litebridge.class)
@EnableConfigurationProperties(LitebridgeProperties.class)
public class LitebridgeAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(LitebridgeAutoConfiguration.class);

    /**
     * Creates a Litebridge Spring transaction manager bean.
     *
     * @param dataSource DataSource to use
     * @return Litebridge Spring transaction manager
     */
    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    @DependsOnDatabaseInitialization
    public LitebridgeTransactionManager litebridgeTransactionManager(final DataSource dataSource) {
        return new LitebridgeTransactionManager(dataSource);
    }

    /**
     * Instantiates a Litebridge instance.
     * <p>
     * The DatabaseProvider is specified by the {@code litebridgedb.database-provider.class} property,
     * or by detecting an implementation on the classpath if not specified.
     *
     * @param properties         Litebridge Spring Boot autoconfiguration properties
     * @param transactionManager Litebridge Spring transaction manager
     * @return Litebridge instance
     */
    @Bean(name = "litebridge")
    @ConditionalOnMissingBean
    public Litebridge litebridge(final LitebridgeProperties properties, final LitebridgeTransactionManager transactionManager) {
        final DatabaseProvider databaseProvider;

        if (properties.getDatabaseProvider().getProviderClass() != null) {
            // Specific database provider class configured
            databaseProvider = configBasedDatabaseProvider(properties);
        } else {
            // Database provider class not explicitly set; detect it from the classpath
            databaseProvider = autoDetectDatabaseProvider(properties);
        }

        return new Litebridge(databaseProvider, transactionManager);
    }

    /**
     * Instantiates configured DatabaseProvider from property-specified class with validation.
     * <p>
     * The {@link DatabaseProvider} implementation class is specified by the {@code litebridgedb.database-provider.class} property.
     */
    @SuppressWarnings("unchecked")
    private static DatabaseProvider configBasedDatabaseProvider(final LitebridgeProperties properties) {
        final String databaseProviderClassName = Objects.requireNonNull(properties.getDatabaseProvider().getProviderClass());
        LOGGER.debug("Litebridge: Initialising configured DatabaseProvider class: {}", databaseProviderClassName);
        final Class<? extends DatabaseProvider> databaseProviderClass;

        try {
            final Class<?> candidateClass = ClassUtils.forName(databaseProviderClassName, ClassUtils.getDefaultClassLoader());

            if (DatabaseProvider.class.isAssignableFrom(candidateClass)) {
                databaseProviderClass = (Class<? extends DatabaseProvider>) candidateClass;
            } else {
                throw new IllegalArgumentException("Failed to instantiate Litebridge; Specified class does not implement DatabaseProvider: %s".formatted(databaseProviderClassName));
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Failed to instantiate Litebridge; DatabaseProvider class not found: %s".formatted(databaseProviderClassName), ex);
        }

        final Constructor<? extends DatabaseProvider> constructor = ClassUtils.getConstructorIfAvailable(databaseProviderClass);

        if (constructor == null) {
            throw new IllegalArgumentException("Failed to instantiate Litebridge; No suitable constructor found for DatabaseProvider class: %s".formatted(databaseProviderClassName));
        }

        return BeanUtils.instantiateClass(constructor);
    }


    /**
     * Instantiates single DatabaseProvider via classpath scan for available {@link DatabaseProvider} implementations
     * if no {@code litebridgedb.database-provider.class} property is specified.
     * <p>
     * It enforces uniqueness - ensures that only one {@link DatabaseProvider} implementation is used per application.
     */
    private static DatabaseProvider autoDetectDatabaseProvider(final LitebridgeProperties properties) {
        LOGGER.debug("Litebridge: Auto-detecting DatabaseProvider from classpath");
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(DatabaseProvider.class));
        final Set<BeanDefinition> candidates = scanner.findCandidateComponents(properties.getDatabaseProvider().getScanBasePackage());

        if (CollectionUtils.isEmpty(candidates)) {
            throw new IllegalStateException("Failed to instantiate Litebridge; no DatabaseProvider implementations found by scanning base package: " + properties.getDatabaseProvider().getScanBasePackage());
        } else if (candidates.size() > 1) {
            throw new IllegalStateException("Failed to instantiate Litebridge; multiple DatabaseProvider implementations found: %s. Please ensure there is only one on the classpath, or specify the fully qualified class name in the 'litebridgedb.database-provider.class' property.".formatted(candidates));
        }

        final BeanDefinition candidate = candidates.iterator().next();
        final Class<?> databaseProviderClass = ClassUtils.resolveClassName(Objects.requireNonNull(candidate.getBeanClassName()), ClassUtils.getDefaultClassLoader());
        LOGGER.debug("Litebridge: Auto-detected DatabaseProvider class: {}", databaseProviderClass.getName());
        return (DatabaseProvider) BeanUtils.instantiateClass(databaseProviderClass);
    }
}
