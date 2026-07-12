package org.litebridge.spring;

import org.junit.jupiter.api.Test;
import org.litebridge.spring.testentities.ScannedEntityOne;
import org.litebridge.spring.testentities.ScannedEntityTwo;
import org.litebridge.spring.testentities.one.PackageOneEntity;
import org.litebridge.spring.testentities.two.PackageTwoEntity;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LitebridgeEntityScannerTest {

    @Test
    void scanBasePackage_returnsOnlyClassesAnnotatedWithTable() {
        // Given
        final LitebridgeEntityScanner scanner = new LitebridgeEntityScanner();

        // When
        final Class<?>[] result = scanner.scanBasePackage("org.litebridge.spring.testentities");

        // Then
        assertEquals(
                Set.of(ScannedEntityOne.class,
                        ScannedEntityTwo.class,
                        PackageOneEntity.class,
                        PackageTwoEntity.class),
                Arrays.stream(result).collect(Collectors.toSet())
        );
    }

    @Test
    void scanBasePackage_scansAllProvidedBasePackages() {
        // Given
        final LitebridgeEntityScanner scanner = new LitebridgeEntityScanner();

        // When
        final Class<?>[] result = scanner.scanBasePackage(
                "org.litebridge.spring.testentities.one",
                "org.litebridge.spring.testentities.two"
        );

        // Then
        assertEquals(
                Set.of(
                        org.litebridge.spring.testentities.one.PackageOneEntity.class,
                        org.litebridge.spring.testentities.two.PackageTwoEntity.class
                ),
                Arrays.stream(result).collect(Collectors.toSet())
        );
    }

    @Test
    void scanBasePackage_returnsEmptyArrayWhenNoEntitiesAreFound() {
        // Given
        final LitebridgeEntityScanner scanner = new LitebridgeEntityScanner();

        // When
        final Class<?>[] result = scanner.scanBasePackage("org.litebridge.spring.noentities");

        // Then
        assertEquals(0, result.length);
    }

    @Test
    void createScanningProvider_createsProviderThatIncludesTableAnnotatedClassesOnly() {
        // Given
        final LitebridgeEntityScanner scanner = new LitebridgeEntityScanner();

        // When
        final ClassPathScanningCandidateComponentProvider provider = scanner.createScanningProvider();

        // Then
        final Set<String> beanClassNames = provider.findCandidateComponents("org.litebridge.spring.testentities")
                .stream()
                .map(beanDefinition -> beanDefinition.getBeanClassName())
                .collect(Collectors.toSet());

        assertEquals(4, beanClassNames.size());
        assertTrue(beanClassNames.contains(ScannedEntityOne.class.getName()));
        assertTrue(beanClassNames.contains(ScannedEntityTwo.class.getName()));
        assertTrue(beanClassNames.contains(PackageOneEntity.class.getName()));
        assertTrue(beanClassNames.contains(PackageTwoEntity.class.getName()));
    }
}