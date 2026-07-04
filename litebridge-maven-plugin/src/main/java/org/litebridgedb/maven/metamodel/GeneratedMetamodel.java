package org.litebridgedb.maven.metamodel;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Represents a generated metamodel.
 *
 * @param metamodel The compilation unit representing the generated metamodel.
 * @param className The name of the generated metamodel class.
 */
public record GeneratedMetamodel(CompilationUnit metamodel, String className) {
}
