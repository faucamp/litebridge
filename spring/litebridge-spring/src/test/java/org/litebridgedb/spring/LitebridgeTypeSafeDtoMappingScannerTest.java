package org.litebridgedb.spring;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.spring.testmappings.ScannedMappingOne;
import org.litebridgedb.spring.testmappings.ScannedMappingTwo;
import org.litebridgedb.spring.testmappings.one.PackageOneMapping;
import org.litebridgedb.spring.testmappings.two.PackageTwoMapping;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LitebridgeTypeSafeDtoMappingScannerTest {

    @Test
    void scanBasePackage_returnsOnlyTypeSafeDtoTableMappings() {
        // Given
        final LitebridgeTypeSafeDtoMappingScanner scanner = new LitebridgeTypeSafeDtoMappingScanner();

        // When
        final TypeSafeDtoTableMapping[] result = scanner.scanBasePackage("org.litebridgedb.spring.testmappings");

        // Then
        assertEquals(
                Set.of(
                        ScannedMappingOne.class,
                        ScannedMappingTwo.class,
                        PackageOneMapping.class,
                        PackageTwoMapping.class
                ),
                Arrays.stream(result)
                        .map(Object::getClass)
                        .collect(Collectors.toSet())
        );
    }

    @Test
    void scanBasePackage_instantiatesDiscoveredMappings() {
        // Given
        final LitebridgeTypeSafeDtoMappingScanner scanner = new LitebridgeTypeSafeDtoMappingScanner();

        // When
        final TypeSafeDtoTableMapping[] result = scanner.scanBasePackage("org.litebridgedb.spring.testmappings.one");

        // Then
        assertEquals(1, result.length);
        assertInstanceOf(PackageOneMapping.class, result[0]);
    }

    @Test
    void scanBasePackage_scansAllProvidedBasePackages() {
        // Given
        final LitebridgeTypeSafeDtoMappingScanner scanner = new LitebridgeTypeSafeDtoMappingScanner();

        // When
        final TypeSafeDtoTableMapping[] result = scanner.scanBasePackage(
                "org.litebridgedb.spring.testmappings.one",
                "org.litebridgedb.spring.testmappings.two"
        );

        // Then
        assertEquals(
                Set.of(PackageOneMapping.class, PackageTwoMapping.class),
                Arrays.stream(result)
                        .map(Object::getClass)
                        .collect(Collectors.toSet())
        );
    }

    @Test
    void scanBasePackage_returnsEmptyArrayWhenNoMappingsAreFound() {
        // Given
        final LitebridgeTypeSafeDtoMappingScanner scanner = new LitebridgeTypeSafeDtoMappingScanner();

        // When
        final TypeSafeDtoTableMapping[] result = scanner.scanBasePackage("org.litebridgedb.spring.testmappings.nomappings");

        // Then
        assertEquals(0, result.length);
    }

    @Test
    void createScanningProvider_createsProviderThatIncludesTypeSafeDtoTableMappingsOnly() {
        // Given
        final LitebridgeTypeSafeDtoMappingScanner scanner = new LitebridgeTypeSafeDtoMappingScanner();

        // When
        final ClassPathScanningCandidateComponentProvider provider = scanner.createScanningProvider();

        // Then
        final Set<String> beanClassNames = provider.findCandidateComponents("org.litebridgedb.spring.testmappings")
                .stream()
                .map(beanDefinition -> beanDefinition.getBeanClassName())
                .collect(Collectors.toSet());

        assertEquals(4, beanClassNames.size());
        assertTrue(beanClassNames.contains(ScannedMappingOne.class.getName()));
        assertTrue(beanClassNames.contains(ScannedMappingTwo.class.getName()));
        assertTrue(beanClassNames.contains(PackageOneMapping.class.getName()));
        assertTrue(beanClassNames.contains(PackageTwoMapping.class.getName()));
    }
}