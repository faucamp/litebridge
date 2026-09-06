package org.litebridge.db.spi.impl.engine;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

public interface ExecutionEngine {

    /**
     * Execute a SQL INSERT operation using the provided prepared SQL statement and table metadata.
     * <p>
     * This method executes the prepared statement, retrieves any generated primary key values,
     * and wraps the results in an {@link InsertResult} object.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return an {@link InsertResult} object encapsulating the number of affected rows and a list of generated keys (if any)
     * @throws SQLException if an error occurs while executing the SQL insert or retrieving the generated keys
     */
    InsertResult executeInsert(PreparedSql preparedSql, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Execute a SQL UPDATE operation using the provided prepared SQL statement and table metadata.
     * <p>
     * This method performs the execution of a prepared update statement and wraps the number
     * of affected rows in an {@link UpdateResult} object.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return an {@link UpdateResult} object encapsulating the number of rows affected by the update operation
     * @throws SQLException if an error occurs while executing the SQL update
     */
    UpdateResult executeUpdate(PreparedSql preparedSql, ConnectionProvider connectionProvider) throws SQLException;

    List<Row> executeQuery(PreparedSql preparedSql, ConnectionProvider connectionProvider) throws SQLException;

    TypeConverter typeConverter();

    AliasTransformer aliasTransformer();
}
