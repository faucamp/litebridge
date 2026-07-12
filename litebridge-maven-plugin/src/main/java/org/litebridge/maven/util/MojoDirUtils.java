package org.litebridge.maven.util;

import org.apache.maven.project.MavenProject;
import org.jspecify.annotations.Nullable;

public final class MojoDirUtils {

    private MojoDirUtils() {
    }

    public static String getOutputDir(final @Nullable String outputDir, final MavenProject project) {
        return outputDir != null ? outputDir : "%s/generated-sources/java".formatted(project.getBuild().getDirectory());
    }
}
