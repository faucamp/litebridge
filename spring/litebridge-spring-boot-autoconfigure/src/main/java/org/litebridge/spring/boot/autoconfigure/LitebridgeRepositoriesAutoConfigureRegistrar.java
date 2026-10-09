package org.litebridge.spring.boot.autoconfigure;

import org.litebridge.spring.repository.EnableLitebridgeRepositories;
import org.litebridge.spring.repository.LitebridgeRepositoryConfigurationExtension;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar} used to auto-configure Litebridge
 * repositories.
 */
class LitebridgeRepositoriesAutoConfigureRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableLitebridgeRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableLitebridgeRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new LitebridgeRepositoryConfigurationExtension();
    }

    @EnableLitebridgeRepositories
    private static final class EnableLitebridgeRepositoriesConfiguration {
    }
}
