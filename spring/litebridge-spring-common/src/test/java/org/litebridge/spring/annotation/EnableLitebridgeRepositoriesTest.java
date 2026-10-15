package org.litebridge.spring.annotation;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.repository.LitebridgeRepository;
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
        public Litebridge litebridge() {
            return Mockito.mock(Litebridge.class);
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

    @Test
    @SuppressWarnings("unchecked")
    void enableLitebridgeRepositories_repositoryProxySupportsPagingAndSorting() {
        // Given
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();
            final SampleRepository repository = context.getBean(SampleRepository.class);
            final Litebridge litebridgeCore = context.getBean(Litebridge.class);
            final org.litebridge.orm.api.select.dto.DtoFromClauseTerminal<Object> dtoSelectTerminal =
                    Mockito.mock(org.litebridge.orm.api.select.dto.DtoFromClauseTerminal.class);
            Mockito.when(litebridgeCore.select(Object.class)).thenReturn(dtoSelectTerminal);
            Mockito.when(dtoSelectTerminal.list()).thenReturn(java.util.Collections.emptyList());

            // When / Then
            assertDoesNotThrow(() -> {
                repository.findAll(org.springframework.data.domain.Sort.unsorted());
                repository.findAll(org.springframework.data.domain.Pageable.unpaged());
            });
        }
    }
}
