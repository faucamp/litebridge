package org.litebridge.maven.config.metamodel;

import org.jspecify.annotations.Nullable;
import org.litebridge.maven.config.OutputConfig;

import java.util.StringJoiner;

/**
 * Metamodel generation output configuration.
 */
public final class MetamodelOutputConfig extends OutputConfig {

    /**
     * Prefix to add to generated class names.
     * <p>
     * Default: empty string.
     * <p>
     * Example: if the source DTO/entity is named `Person` and {@code <classNamePrefix>} is "My",
     * then the generated metamodel class name becomes `MyPerson`.
     * <p>
     * This is combined with `classNameSuffix`.
     */
    private @Nullable String classNamePrefix;

    /**
     * Suffix to add to generated class names.
     * <p>
     * Default: "Meta".
     * <p>
     * Example: if the source DTO/entity is named `Person` and {@code <classNameSuffix>} is "Meta",
     * then the generated metamodel class name becomes `PersonMeta`.
     * <p>
     * This is combined with `classNamePrefix`.
     */
    private @Nullable String classNameSuffix;

    public String getClassNamePrefix() {
        if (classNamePrefix == null) {
            return "";
        }

        return classNamePrefix;
    }

    public void setClassNamePrefix(final @Nullable String classNamePrefix) {
        this.classNamePrefix = classNamePrefix;
    }

    public String getClassNameSuffix() {
        if (classNameSuffix == null) {
            if (classNamePrefix == null) {
                return "Meta";
            } else {
                return "";
            }
        }

        return classNameSuffix;
    }

    public void setClassNameSuffix(final @Nullable String classNameSuffix) {
        this.classNameSuffix = classNameSuffix;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MetamodelOutputConfig.class.getSimpleName() + "[", "]")
                .add("classNamePrefix='" + classNamePrefix + "'")
                .add("classNameSuffix='" + classNameSuffix + "'")
                .toString();
    }
}
