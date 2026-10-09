package org.litebridge.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.boot.autoconfigure.test.repository.TestPersonRepository;
import org.litebridge.spring.boot.autoconfigure.test.repository.TestRepositoryConfiguration;
import org.litebridge.spring.repository.EnableLitebridgeRepositories;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LitebridgeRepositoriesAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LitebridgeAutoConfiguration.class,
                    LitebridgeRepositoriesAutoConfiguration.class))
            .withUserConfiguration(TestRepositoryConfiguration.class);

    @Test
    void autoConfiguration_registersRepositoriesAutomatically() {
        // Given / When / Then
        this.contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TestPersonRepository.class);
            assertThat(context).hasSingleBean(Litebridge.class);
        });
    }

    @Test
    void autoConfiguration_disabledViaProperty() {
        // Given / When / Then
        this.contextRunner
                .withPropertyValues("litebridge.repositories.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(TestPersonRepository.class);
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
                    assertThat(context).hasSingleBean(TestPersonRepository.class);
                });
    }

    @Test
    void autoConfiguration_backsOffWhenLitebridgeMissing() {
        // Given / When / Then
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LitebridgeRepositoriesAutoConfiguration.class))
                .withUserConfiguration(TestRepositoryConfiguration.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(TestPersonRepository.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableLitebridgeRepositories(basePackageClasses = TestPersonRepository.class)
    static class ManualConfig {
    }
}
