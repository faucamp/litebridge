package org.litebridgedb.db.spi.impl.sql;

import org.litebridgedb.db.spi.sql.BindValue;

import java.util.List;

/**
 * A prepared row with associated rhs specifiers and bound values.
 * <p>
 * This record is a data structure that holds information about a row in which
 * each element is defined by a list of rhs specifiers and a corresponding
 * list of bind values. Commonly used in scenarios involving prepared statements
 * or database row mappings.
 * <p>
 * The {@code valueSpecifiers} list contains the string representations or placeholders
 * defining the schema or format for the data in the row.
 * <p>
 * The {@code bindValues} list contains the bound or parameterized values that align
 * with the associated specifiers.
 * <p>
 * It is the caller's responsibility to ensure that the {@code valueSpecifiers} and
 * {@code bindValues} lists are properly aligned, with each rhs specifier corresponding
 * to its respective bind rhs.
 * <p>
 * This class is immutable and thread-safe by design.
 *
 * @param valueSpecifiers the list of specifiers defining data format or schema
 * @param bindValues      the list of bound values corresponding to the specifiers
 */
public record PreparedRow(List<String> valueSpecifiers, List<BindValue> bindValues) {
}