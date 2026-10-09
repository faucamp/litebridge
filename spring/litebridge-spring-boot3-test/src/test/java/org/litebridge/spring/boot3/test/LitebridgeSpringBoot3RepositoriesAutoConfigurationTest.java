package org.litebridge.spring.boot3.test;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.boot.autoconfigure.LitebridgeAutoConfiguration;
import org.litebridge.spring.boot.autoconfigure.LitebridgeRepositoriesAutoConfiguration;
import org.litebridge.spring.boot3.test.repository.SpringBoot3PersonRepository;
import org.litebridge.spring.boot3.test.repository.SpringBoot3RepositoryConfig;
import org.litebridge.spring.repository.EnableLitebridgeRepositories;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LitebridgeSpringBoot3RepositoriesAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LitebridgeAutoConfiguration.class,
                    LitebridgeRepositoriesAutoConfiguration.class))
            .withUserConfiguration(SpringBoot3RepositoryConfig.class);

    @Test
    void autoConfiguration_registersRepositoriesAutomatically() {
        // Given / When / Then
        this.contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(SpringBoot3PersonRepository.class);
            assertThat(context).hasSingleBean(Litebridge.class);
        });
    }

    @Test
    void autoConfiguration_disabledViaProperty() {
        // Given / When / Then
        this.contextRunner
                .withPropertyValues("spring.data.litebridge.repositories.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(SpringBoot3PersonRepository.class);
                    assertThat(context).hasSingleBean(Litebridge.class);
                });
    }

    @Test
    void autoConfiguration_backsOffWhenManualEnableLitebridgeRepositoriesPresent() {
        // Given / When / Then
        this.contextRunner
                .withUserConfiguration(ManualConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(SpringBoot3PersonRepository.class);
                });
    }

    @Test
    void autoConfiguration_backsOffWhenLitebridgeMissing() {
        // Given / When / Then
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LitebridgeRepositoriesAutoConfiguration.class))
                .withUserConfiguration(SpringBoot3RepositoryConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(SpringBoot3PersonRepository.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableLitebridgeRepositories(basePackageClasses = SpringBoot3PersonRepository.class)
    static class ManualConfig {
    }
}
