package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.StringJoiner;

public class OutputConfig {

    /**
     * Output package for generated entity classes
     */
    @Parameter(required = true)
    private String outputPackage;

    /**
     * Whether to add Javadoc comments in generated entity classes.
     */
    @Parameter(defaultValue = "true")
    private boolean javadoc = true;

    /**
     * If {@code true}, generated entity classes will be declared as final.
     */
    @Parameter(defaultValue = "false")
    private boolean finalClasses;

    public String getOutputPackage() {
        return outputPackage;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", OutputConfig.class.getSimpleName() + "[", "]")
                .add("outputPackage='" + outputPackage + "'")
                .add("finalClasses=" + finalClasses)
                .toString();
    }
}
