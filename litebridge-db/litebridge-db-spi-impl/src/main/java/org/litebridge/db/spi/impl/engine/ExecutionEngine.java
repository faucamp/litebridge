package org.litebridge.db.spi.impl.engine;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.BatchUpdateResult;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Result;
import org.litebridge.db.spi.update.UpdateOpResult;
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
     * of affected rows in an {@link UpdateOpResult} object.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return an {@link UpdateOpResult}-implementing result encapsulating the number of rows affected by the update operation
     * @throws SQLException if an error occurs while executing the SQL update
     */
    UpdateResult executeUpdate(PreparedSql preparedSql, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Execute a batch of modifying SQL operations in the database using the provided statement.
     * <p>
     * The statements must all have the same generated SQL.
     *
     * @param preparedSqls       the list of {@link PreparedSql} statements to batch.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return an {@link Result} containing e.g. the number of rows affected and any generated keys.
     * @throws SQLException if any SQL error occurs during the execution of the SELECT operation.
     */
    BatchUpdateResult executeBatch(List<PreparedSql> preparedSqls, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Executes a SELECT operation in the database using a pre-prepared {@link PreparedSql} object.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query string and bind values.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return a {@link List} of {@link Row} objects representing the results of the SELECT operation.
     * @throws SQLException if any SQL error occurs during the execution of the SELECT operation.
     */
    List<Row> executeQuery(PreparedSql preparedSql, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Retrieve the {@link TypeConverter} instance associated with the database provider.
     * <p>
     * The {@code TypeConverter} is used for converting objects between different types,
     * typically for database data type conversions and mapping domain-specific representations.
     *
     * @return the {@link TypeConverter} instance for handling data type conversions
     */
    TypeConverter typeConverter();
}
