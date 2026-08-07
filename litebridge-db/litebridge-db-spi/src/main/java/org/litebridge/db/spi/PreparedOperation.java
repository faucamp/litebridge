package org.litebridge.db.spi;

import org.litebridge.db.spi.sql.BindValue;

import java.util.List;

/**
 * A database operation with bind values.
 * <p>
 * This is a precursor to the final {@link org.litebridge.db.spi.sql.PreparedSql} instance that is executed by the database provider.
 *
 * @param operation  The structured database operation.
 * @param bindValues The bind values to be used in the operation.
 */
public record PreparedOperation(Operation operation, List<BindValue> bindValues) {
}
