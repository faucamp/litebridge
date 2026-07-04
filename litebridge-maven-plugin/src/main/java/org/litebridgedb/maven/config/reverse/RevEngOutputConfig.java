package org.litebridgedb.maven.config.reverse;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.maven.config.OutputConfig;

/**
 * Reverse engineering output configuration.
 */
public class RevEngOutputConfig extends OutputConfig {

    /**
     * Configuration for annotating generated classes/packages for nullability using JSpecify.
     */
    private @Nullable RevEngJSpecifyConfig jspecify;

    @Override
    public void setJspecify(final @Nullable RevEngJSpecifyConfig jspecify) {
        super.setJspecify(jspecify);
    }

    @Override
    public @Nullable RevEngJSpecifyConfig getJspecify() {
        return super.getJspecify();
    }
}
