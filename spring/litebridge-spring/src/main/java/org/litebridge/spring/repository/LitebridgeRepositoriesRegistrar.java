package org.litebridge.spring.repository;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

public class LitebridgeRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableLitebridgeRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new LitebridgeRepositoryConfigurationExtension();
    }
}
