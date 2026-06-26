package org.litebridgedb.db.spi;

import org.litebridgedb.db.spi.alias.AliasTransformer;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.update.Delete;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.db.spi.update.UpdateResult;

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
     * Execute an INSERT operation in the database using the provided {@link Insert} statement.
     *
     * @param insert             the {@link Insert} statement containing the table, columns, and rows to insert.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return an {@link InsertResult} containing the number of rows affected and any generated keys.
     * @throws SQLException if any SQL error occurs during the execution of the insert operation.
     */
    InsertResult insert(Insert insert, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Execute an UPDATE operation in the database using the provided {@link Update} statement.
     *
     * @param update             the {@link Update} statement containing the table, columns, and rows to update.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return an {@link UpdateResult} containing the number of rows affected.
     * @throws SQLException if any SQL error occurs during the execution of the update operation.
     */
    UpdateResult update(Update update, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Execute a SELECT operation in the database using the provided {@link Select} statement.
     *
     * @param select             the {@link Select} statement containing information about the table, expressions,
     *                           joins, conditions, ordering, and optional limits for the query.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return a {@link List} of {@link Row} objects representing the results of the SELECT operation.
     * @throws SQLException if any SQL error occurs during the execution of the SELECT operation.
     */
    List<Row> select(Select select, ConnectionProvider connectionProvider) throws SQLException;

    /**
     * Execute a DELETE operation in the database using the provided {@link Delete} statement.
     *
     * @param delete             the {@link Delete} statement containing the table and rows to delete.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return an {@link UpdateResult} containing the number of rows affected.
     * @throws SQLException if any SQL error occurs during the execution of the delete operation.
     */
    UpdateResult delete(Delete delete, ConnectionProvider connectionProvider) throws SQLException;

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
    String toSql(Operation operation, ConnectionProvider connectionProvider);

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
     * fetch the next rhs from a specified database sequence. This is typically used in SQL statements
     * such as INSERT or UPDATE to generate unique values from a sequence.
     *
     * @param sequence the name of the database sequence from which the values will be generated.
     *                 It is used to create the SQL fragment to retrieve the next rhs in the sequence.
     * @return a {@link SequenceColumnValueGenerator} instance that generates SQL fragments for retrieving sequence values.
     * @throws UnsupportedOperationException if the database provider does not support sequence-based rhs generation.
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

    AliasTransformer getAliasTransformer();
}
