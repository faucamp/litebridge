package org.litebridge.orm.nativesql;

import java.util.List;

/**
 * Represents a parsed SQL statement with positional parameters and their corresponding names.
 * <p>
 * This class encapsulates a SQL statement where named parameters are replaced with
 * positional placeholders (e.g., "?") and provides a list of the original parameter names.
 * It is used for transforming SQL statements with named parameters into a format
 * that can be executed using standard JDBC APIs.
 *
 * @param sql                the SQL statement with named parameters replaced by positional placeholders
 * @param bindParameterNames a list of the original names of the bind parameters in the order
 *                           they appear in the statement
 */
public record ParsedSql(String sql, List<String> bindParameterNames) {
}
