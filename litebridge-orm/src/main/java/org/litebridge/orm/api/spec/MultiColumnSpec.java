package org.litebridge.orm.api.spec;

import org.jspecify.annotations.NullMarked;

/**
 * Specification of multiple database columns, used to map a single complex field to multiple
 * database columns.
 * <p>
 * Used in cases where a related DTO has a composite primary key.
 *
 * @param columnSpecs Individual columns specifications for each column in the mapping.
 */
@NullMarked
public record MultiColumnSpec(ColumnSpec[] columnSpecs) implements ColumnMapping {
}