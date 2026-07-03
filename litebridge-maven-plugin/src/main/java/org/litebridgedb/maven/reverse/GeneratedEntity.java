package org.litebridgedb.maven.reverse;

import com.github.javaparser.ast.CompilationUnit;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ForeignKeyConstraint;

import java.util.List;
import java.util.Map;

public record GeneratedEntity(CompilationUnit entity,
                              List<ForeignKeyConstraint> unresolvedEntityRefs,
                              String tableName,
                              String className,
                              Map<Column, String> columnfieldMap) {
}
