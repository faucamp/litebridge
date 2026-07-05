package org.litebridgedb.maven.reverse;

import com.github.javaparser.ast.CompilationUnit;
import org.litebridgedb.db.spi.Column;

import java.util.Map;

/**
 * Represents a generated entity from reverse engineering.
 *
 * @param entity         The compilation unit representing the generated entity.
 * @param tableName      The name of the database table that the entity is based on.
 * @param className      The name of the generated entity class.
 * @param columnfieldMap A map of columns to their corresponding field names in the generated entity class.
 */
public record GeneratedEntity(CompilationUnit entity,
                              String tableName,
                              String className,
                              Map<Column, String> columnfieldMap) {
}
