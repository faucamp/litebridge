package org.litebridge.spring.repository;

import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.util.Collection;
import java.util.Collections;

public class LitebridgeRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return LitebridgeRepositoryFactoryBean.class.getName();
    }

    @Override
    protected String getModulePrefix() {
        return "litebridge";
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(LitebridgeRepository.class);
    }

    @Override
    protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        return !metadata.isReactiveRepository();
    }
}
