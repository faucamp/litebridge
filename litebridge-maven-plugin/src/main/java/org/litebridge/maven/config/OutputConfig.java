package org.litebridge.maven.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;
import org.litebridge.maven.config.reverse.RevEngJSpecifyConfig;

import java.util.StringJoiner;

/**
 * Output configuration for entity reverse engineering and metamodel generation.
 */
public class OutputConfig {

    /**
     * Output directory. This is where the {@code <outputPackage>} and generated entity/metamodel classes will be created.
     * <p>
     * Defaults to {@code ${project.build.directory}/generated-sources/java}
     */
    private @Nullable String outputDir;

    /**
     * Output package for generated entity classes/metamodels
     */
    @Parameter(required = true)
    private String outputPackage;

    /**
     * Whether to generate a {@code package-info.java} file for the output package.
     * <p>
     * Default: {@code true}
     */
    @Parameter(defaultValue = "true")
    private boolean packageInfo = true;

    /**
     * Whether to add Javadoc comments in generated entity/metamodel classes.
     * <p>
     * Default: {@code true}
     */
    @Parameter(defaultValue = "true")
    private boolean javadoc = true;

    /**
     * If {@code true}, generated entity/metamodel classes will be declared as final.
     * <p>
     * Default: {@code true}
     */
    @Parameter(defaultValue = "true")
    private boolean finalClasses = true;

    /**
     * Configuration for annotating generated classes/packages for nullability using JSpecify.
     */
    private @Nullable RevEngJSpecifyConfig jspecify;

    public @Nullable String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(final @Nullable String outputDir) {
        this.outputDir = outputDir;
    }

    public String getOutputPackage() {
        return outputPackage;
    }

    public boolean isPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(final boolean packageInfo) {
        this.packageInfo = packageInfo;
    }

    public void setOutputPackage(final String outputPackage) {
        this.outputPackage = outputPackage;
    }

    public boolean isJavadoc() {
        return javadoc;
    }

    public void setJavadoc(final boolean javadoc) {
        this.javadoc = javadoc;
    }

    public boolean isFinalClasses() {
        return finalClasses;
    }

    public void setFinalClasses(final boolean finalClasses) {
        this.finalClasses = finalClasses;
    }

    public @Nullable RevEngJSpecifyConfig getJspecify() {
        return jspecify;
    }

    public void setJspecify(final @Nullable RevEngJSpecifyConfig jspecify) {
        this.jspecify = jspecify;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OutputConfig.class.getSimpleName() + "[", "]")
                .add("outputDir='" + outputDir + "'")
                .add("outputPackage='" + outputPackage + "'")
                .add("packageInfo=" + packageInfo)
                .add("javadoc=" + javadoc)
                .add("finalClasses=" + finalClasses)
                .add("jspecify=" + jspecify)
                .toString();
    }
}
