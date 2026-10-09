package org.litebridge.spring.repository;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.LitebridgeCore;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnableLitebridgeRepositoriesTest {

    @Configuration
    @EnableLitebridgeRepositories(
            basePackageClasses = SampleRepository.class,
            considerNestedRepositories = true
    )
    public static class TestConfig {

        @Bean
        public LitebridgeCore litebridgeCore() {
            return Mockito.mock(LitebridgeCore.class);
        }
    }

    public interface SampleRepository extends LitebridgeRepository<Object, Long> {
    }

    @Test
    void enableLitebridgeRepositories_bootstrapsWithoutMissingAttributeException() {
        // Given / When / Then
        assertDoesNotThrow(() -> {
            try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
                context.register(TestConfig.class);
                context.refresh();
                assertNotNull(context.getBean(SampleRepository.class));
            }
        });
    }
}
