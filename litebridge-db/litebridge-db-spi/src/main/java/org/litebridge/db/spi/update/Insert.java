package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;

import java.util.List;

/**
 * Logical representation of a SQL {@code INSERT} statement.
 * <p>
 * It is used by {@link org.litebridge.db.spi.DatabaseProvider} implementations
 * to generate SQL {@code INSERT} statement strings.
 *
 * @param table               The target table for the insertion operation.
 * @param columns             The list of columns involved in the insertion operation.
 * @param rows                The list of rows to be inserted.
 * @param returnGeneratedKeys If true, generated keys will be returned after the insert operation.
 */
public record Insert(Table table,
                     List<UpdateColumn> columns,
                     int rows,
                     boolean returnGeneratedKeys)
        implements UpdateStatement {
}
