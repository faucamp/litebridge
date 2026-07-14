package org.litebridge.maven.util;

import com.github.javaparser.ast.body.FieldDeclaration;

/**
 * Utility class for simplifying checks in ClassGraph.
 */
public final class ClassGraphUtil {

    private ClassGraphUtil() {
    }

    /**
     * Returns the name of the input `FieldDeclaration`.
     *
     * @param field The FieldDeclaration node from which to extract the variable name.
     * @return Field name: the name of the first variable declared within the FieldDeclaration.
     */
    public static String getFieldName(final FieldDeclaration field) {
        return field.getVariable(0).getNameAsString();
    }
}
