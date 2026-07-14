package org.litebridge.maven.config.reverse;

import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;
import org.litebridge.maven.config.OutputConfig;

/**
 * Reverse engineering output configuration.
 */
public class RevEngOutputConfig extends OutputConfig {

    /**
     * Configuration for annotating generated classes/packages for nullability using JSpecify.
     */
    private @Nullable RevEngJSpecifyConfig jspecify;

    /**
     * Controls how related entities are mapped when foreign keys are encountered.
     * <p>
     * Setting it to {@code false} disables entity resolution; foreign key fields will use the "flat" database type
     * (e.g. {@code Long personId}) instead of replacing the field with the related entity (e.g. {@code Person person}.
     * <p>
     * This is the global setting for all tables. It can be overridden on a per-table basis.
     * <p>
     * Default: {@code true}
     */
    @Parameter(defaultValue = "true")
    private boolean resolveRelationships = true;

    @Override
    public void setJspecify(final @Nullable RevEngJSpecifyConfig jspecify) {
        super.setJspecify(jspecify);
    }

    @Override
    public @Nullable RevEngJSpecifyConfig getJspecify() {
        return super.getJspecify();
    }

    public boolean isResolveRelationships() {
        return resolveRelationships;
    }

    public void setResolveRelationships(final boolean resolveRelationships) {
        this.resolveRelationships = resolveRelationships;
    }
}
