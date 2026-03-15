package org.litebridge.db.spi;

import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Update;
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
     * @param select             the {@link Select} statement containing information about the table, columns,
     *                           joins, conditions, ordering, and optional limits for the query.
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return a {@link List} of {@link Row} objects representing the results of the SELECT operation.
     * @throws SQLException if any SQL error occurs during the execution of the SELECT operation.
     */
    List<Row> select(Select select, ConnectionProvider connectionProvider) throws SQLException;

    String toSql(Select select);

    /**
     * Retrieve the {@link TypeConverter} instance associated with the database provider.
     * <p>
     * The {@code TypeConverter} is used for converting objects between different types,
     * typically for database data type conversions and mapping domain-specific representations.
     *
     * @return the {@link TypeConverter} instance for handling data type conversions
     */
    TypeConverter getTypeConverter();
}
