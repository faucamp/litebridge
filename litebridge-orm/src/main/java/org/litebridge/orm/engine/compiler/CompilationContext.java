package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.sql.BindValue;

import java.util.List;

/**
 * Compilation context for generating SQL operations (logically mapped SQL statements).
 * <p>
 * The compilation context is used to store information about the SQL statement being generated,
 * and ultimately maps this information to a SQL operation via the {@link #toOperation()} method.
 */
sealed interface CompilationContext permits AbstractCompilationContext, InsertCompilationContext {

    /**
     * Returns the bind values for the generated SQL statement.
     * <p>
     * Note: this may only be called after {@link #toOperation()} has been called.
     *
     * @return a list of bind values.
     */
    List<BindValue> getBindValues();

    /**
     * Create the logical SQL operation.
     *
     * @return the mapped SQL operation.
     */
    Operation toOperation();
}
