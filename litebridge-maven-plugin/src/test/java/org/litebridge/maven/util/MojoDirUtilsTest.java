package org.litebridge.maven.util;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MojoDirUtilsTest {

    @Test
    void testGetOutputDir() {
        MavenProject project = mock(MavenProject.class);
        Build build = mock(Build.class);
        when(project.getBuild()).thenReturn(build);
        when(build.getDirectory()).thenReturn("/target");

        // Case 1: outputDir is not null
        assertEquals("/custom", MojoDirUtils.getOutputDir("/custom", project));

        // Case 2: outputDir is null
        assertEquals("/target/generated-sources/java", MojoDirUtils.getOutputDir(null, project));
    }
}
