package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Main interface for interacting with a database.
 * <p>
 * This interface defines the operations for retrieving metadata, executing SQL queries, and managing
 * data within a specific database backend.
 */
public interface DatabaseProvider {

    /**
     * Retrieve metadata for the specified table.
     *
     * @param table              the {@link Table} object representing the table for which metadata is to be retrieved.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return a {@link TableMetaData} object containing metadata information about the specified table,
     * including its columns, primary key, and other details.
     * @throws SQLException if any SQL error occurs while retrieving the metadata.
     */
    TableMetaData tableMetaData(Table table, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Execute an modifying SQL operation in the database using the provided statement.
     *
     * @param preparedSql        the {@link PreparedSql} for the statement.
     * @param resultType         the expected resulting {@link UpdateResult} type
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return an {@link InsertResult} containing the number of rows affected and any generated keys.
     * @throws SQLException if any SQL error occurs during the execution of the SELECT operation.
     */
    <T extends UpdateResult> T executeUpdate(PreparedSql preparedSql, Class<T> resultType, ConnectionProvider connectionProvider) throws SQLException;

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
     * Converts the given {@link Operation} into its corresponding SQL representation.
     * <p>
     * This does not execute the operation, it only generates the SQL string.
     *
     * @param operation          the {@link Operation} object representing the database operation
     *                           (e.g., SELECT, INSERT, UPDATE, DELETE) to be converted to a SQL string.
     * @param connectionProvider the {@link ConnectionProvider} used to obtain database-specific context
     *                           or information required for SQL generation, such as metadata or dialect.
     * @return a {@link String} containing the SQL representation of the given {@link Operation}.
     */
    String toSql(final Operation operation, final ConnectionProvider connectionProvider);

    /**
     * Executes a SQL query with the given SQL string and a list of positional bind parameters.
     *
     * @param sql                the SQL query string to be executed; must not be {@code null}
     * @param bindParameters     the list of parameters to bind to the query
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return a list of {@code Row} objects representing the result set of the query
     * @throws SQLException if an error occurs during query execution
     */
    List<Row> nativeSqlQuery(String sql, final List<@Nullable Object> bindParameters, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Executes a SQL update statement with the given SQL string and a list of positional bind parameters.
     * <p>
     * This method delegates to the overloaded execute method that accepts a list of bind parameters.
     *
     * @param sql                the SQL update statement to execute; must not be {@code null}
     * @param bindParameters     the list of named parameters to bind to the statement
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return an {@code UpdateResult} object that encapsulates the outcome of the update operation
     * @throws SQLException if an error occurs while executing the update statement
     */
    UpdateResult nativeSqlUpdate(String sql, final List<@Nullable Object> bindParameters, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Retrieve the {@link TypeConverter} instance associated with the database provider.
     * <p>
     * The {@code TypeConverter} is used for converting objects between different types,
     * typically for database data type conversions and mapping domain-specific representations.
     *
     * @return the {@link TypeConverter} instance for handling data type conversions
     */
    TypeConverter getTypeConverter();

    /**
     * Retrieves a {@link SequenceColumnValueGenerator} instance for generating SQL fragments that
     * fetch the next value from a specified database sequence. This is typically used in SQL statements
     * such as INSERT or UPDATE to generate unique values from a sequence.
     *
     * @param sequence the name of the database sequence from which the values will be generated.
     *                 It is used to create the SQL fragment to retrieve the next value in the sequence.
     * @return a {@link SequenceColumnValueGenerator} instance that generates SQL fragments for retrieving sequence values.
     * @throws UnsupportedOperationException if the database provider does not support sequence-based value generation.
     */
    SequenceColumnValueGenerator getSequenceColumnValueGenerator(String sequence) throws UnsupportedOperationException;

    /**
     * Retrieve the {@link SqlFunctionRegistry} instance associated with the database provider.
     * <p>
     * The {@code SqlFunctionRegistry} is used for registering and managing SQL functions
     * that can be used in SQL queries executed by the database provider.
     *
     * @return the {@link SqlFunctionRegistry} instance for managing SQL functions
     */
    SqlFunctionRegistry getSqlFunctionRegistry();

    /**
     * Retrieve the {@link AliasTransformer} instance associated with the database provider.
     *
     * @return the {@link AliasTransformer} instance for transforming aliases
     */
    AliasTransformer getAliasTransformer();
}
