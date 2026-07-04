package org.litebridgedb.maven.config.metamodel;

import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

/**
 * Input configuration for metamodel generation.
 */
public final class MetamodelInputConfig {

    /**
     * Input source directory. This is where root directory {@code <inputPackages>} will be searched for.
     * <p>
     * Defaults to the project's source directory.
     * <p>
     * This is useful when generating metamodels from reverse-engineered entities if the entities
     * are not generated in a location that is already part of the project's source directory.
     */
    @Parameter
    private @Nullable String srcDir;

    /**
     * Packages to scan for entities/DTOs
     */
    @Parameter(required = true)
    private List<String> inputPackages;

    /**
     * Controls what types of input classes are processed during metamodel generation.
     * <p>
     * If {@code true}, only annotated entities will be included in metamodel generation.
     * If {@code false}, all classes will be included, whether they are annotated or not.
     */
    @Parameter(defaultValue = "true")
    private boolean entitiesOnly;

    public @Nullable String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(@Nullable final String srcDir) {
        this.srcDir = srcDir;
    }

    public List<String> getInputPackages() {
        return inputPackages;
    }

    public void setInputPackages(final List<String> inputPackages) {
        this.inputPackages = inputPackages;
    }

    public boolean isEntitiesOnly() {
        return entitiesOnly;
    }

    public void setEntitiesOnly(final boolean entitiesOnly) {
        this.entitiesOnly = entitiesOnly;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MetamodelInputConfig.class.getSimpleName() + "[", "]")
                .add("srcDir='" + srcDir + "'")
                .add("inputPackages=" + inputPackages)
                .add("entitiesOnly=" + entitiesOnly)
                .toString();
    }
}
