package org.litebridge.maven.util;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;
import org.litebridge.maven.config.OutputConfig;
import org.litebridge.maven.config.reverse.RevEngJSpecifyConfig;

import static org.junit.jupiter.api.Assertions.*;

class PackageInfoGeneratorTest {

    @Test
    void testCreatePackageInfo_NoJSpecify_NoJavadoc() {
        OutputConfig config = new OutputConfig();
        config.setJavadoc(false);
        config.setJspecify(null);
        
        PackageInfoGenerator generator = new PackageInfoGenerator(config);
        CompilationUnit cu = generator.createPackageInfo("com.example", "My Comment");
        
        String code = cu.toString();
        assertTrue(code.contains("package com.example;"));
        assertFalse(code.contains("@NullMarked"));
        assertFalse(code.contains("@NullUnmarked"));
        assertFalse(code.contains("My Comment"));
    }

    @Test
    void testCreatePackageInfo_NullMarked_Javadoc() {
        OutputConfig config = new OutputConfig();
        config.setJavadoc(true);
        RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(true);
        jspecify.setNullMarked(true);
        config.setJspecify(jspecify);
        
        PackageInfoGenerator generator = new PackageInfoGenerator(config);
        CompilationUnit cu = generator.createPackageInfo("com.example", "My Comment");
        
        String code = cu.toString();
        System.out.println("Generated Package Info:\n" + code);
        assertTrue(code.contains("@NullMarked"));
        assertTrue(code.contains("package com.example;"));
        assertTrue(code.contains("My Comment"));
    }

    @Test
    void testCreatePackageInfo_NullUnmarked() {
        OutputConfig config = new OutputConfig();
        RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(true);
        jspecify.setNullMarked(false);
        config.setJspecify(jspecify);
        
        PackageInfoGenerator generator = new PackageInfoGenerator(config);
        CompilationUnit cu = generator.createPackageInfo("com.example", "My Comment");
        
        String code = cu.toString();
        assertTrue(code.contains("@NullUnmarked"));
    }

    @Test
    void testCreatePackageInfo_JSpecifyDisabled() {
        OutputConfig config = new OutputConfig();
        RevEngJSpecifyConfig jspecify = new RevEngJSpecifyConfig();
        jspecify.setAnnotate(false);
        config.setJspecify(jspecify);
        
        PackageInfoGenerator generator = new PackageInfoGenerator(config);
        CompilationUnit cu = generator.createPackageInfo("com.example", "My Comment");
        
        String code = cu.toString();
        assertFalse(code.contains("@NullMarked"));
        assertFalse(code.contains("@NullUnmarked"));
    }
}
