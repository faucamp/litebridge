package org.litebridge.spring.boot.autoconfigure;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.LitebridgeTransactionManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@Configuration
@ConditionalOnClass(Litebridge.class)
@EnableConfigurationProperties(LitebridgeProperties.class)
public class LitebridgeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    @DependsOnDatabaseInitialization
    public LitebridgeTransactionManager litebridgeTransactionManager(final DataSource dataSource) {
        return new LitebridgeTransactionManager(dataSource);
    }

    /**
     * Instantiates configured DatabaseProvider from property-specified class with validation.
     * <p>
     * The {@link DatabaseProvider} implementation class is specified by the {@code litebridge.database-provider-class} property.
     */
    @Bean(name = "litebridgeDatabaseProvider")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "litebridge.database-provider-class")
    public DatabaseProvider configuredLitebridgeDatabaseProvider(final LitebridgeProperties properties) {
        final Class<? extends DatabaseProvider> databaseProviderClass;

        try {
            final Class<?> candidateClass = ClassUtils.forName(properties.getDatabaseProviderClass(), ClassUtils.getDefaultClassLoader());

            if (DatabaseProvider.class.isAssignableFrom(candidateClass)) {
                databaseProviderClass = (Class<? extends DatabaseProvider>) candidateClass;
            } else {
                throw new IllegalArgumentException("Failed to instantiate Litebridge; Specified class does not implement DatabaseProvider: %s".formatted(properties.getDatabaseProviderClass()));
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Failed to instantiate Litebridge; DatabaseProvider class not found: %s".formatted(properties.getDatabaseProviderClass()), ex);
        }

        final Constructor<? extends DatabaseProvider> constructor = ClassUtils.getConstructorIfAvailable(databaseProviderClass);

        if (constructor == null) {
            throw new IllegalArgumentException("Failed to instantiate Litebridge; No suitable constructor found for DatabaseProvider class: %s".formatted(properties.getDatabaseProviderClass()));
        }

        return BeanUtils.instantiateClass(constructor);
    }

    /**
     * Instantiates single DatabaseProvider via classpath scan for available {@link DatabaseProvider} implementations
     * if no {@code litebridge.database-provider-class} property is specified.
     * <p>
     * It enforces uniqueness - ensures that only one {@link DatabaseProvider} implementation is used per application.
     */
    @Bean(name = "litebridgeDatabaseProvider")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "litebridge.database-provider-class", matchIfMissing = true)
    public DatabaseProvider autoLitebridgeDatabaseProvider(final LitebridgeProperties properties) {
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(DatabaseProvider.class));
        final Set<BeanDefinition> candidates = scanner.findCandidateComponents("org.litebridge.db");

        if (CollectionUtils.isEmpty(candidates)) {
            throw new IllegalStateException("Failed to instantiate Litebridge; no DatabaseProvider implementations found");
        } else if (candidates.size() > 1) {
            throw new IllegalStateException("Failed to instantiate Litebridge; multiple DatabaseProvider implementations found: %s. Please ensure there is only one on the classpath, or specify the fully qualified class name in the 'litebridge.database-provider-class' property.".formatted(candidates));
        }

        final BeanDefinition candidate = candidates.iterator().next();
        final Class<?> candidateClass = ClassUtils.resolveClassName(Objects.requireNonNull(candidate.getBeanClassName()), ClassUtils.getDefaultClassLoader());
        return (DatabaseProvider) BeanUtils.instantiateClass(candidateClass);
    }

    @Bean
    public Litebridge litebridge(final DatabaseProvider databaseProvider, final LitebridgeTransactionManager transactionManager) {
        return new Litebridge(databaseProvider, transactionManager);
    }
}
