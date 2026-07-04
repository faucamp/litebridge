package org.litebridgedb.maven.reverse;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.TraditionalJavadocComment;
import org.jspecify.annotations.NullMarked;
import org.litebridgedb.maven.config.OutputConfig;

public final class PackageInfoGenerator {

    private final OutputConfig outputConfig;

    public PackageInfoGenerator(final OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    public CompilationUnit createPackageInfo(final String packageName) {
        final CompilationUnit cu = new CompilationUnit();
        final PackageDeclaration pkg = new PackageDeclaration(StaticJavaParser.parseName(packageName));
        cu.setPackageDeclaration(pkg);

        if (outputConfig.getJspecify() != null && outputConfig.getJspecify().isNullMarked()) {
            pkg.addMarkerAnnotation(NullMarked.class);
        }

        if (outputConfig.isJavadoc()) {
            pkg.setComment(new TraditionalJavadocComment("Litebridge entities."));
        }

        return cu;
    }
}
