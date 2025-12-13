package org.litebridge.orm.persistence;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSelector<T> {

    protected final List<String> columns;
    protected final DatabaseProvider databaseProvider;
    protected final List<org.litebridge.db.api.query.Condition> conditions = new ArrayList<>();
    protected final TableMetaData tableMetaData;

    public AbstractSelector(final List<String> columns, final TableMetaData tableMetaData, final DatabaseProvider databaseProvider) {
        this.columns = columns;
        this.tableMetaData = tableMetaData;
        this.databaseProvider = databaseProvider;
    }

    /**
     * Creates a new query condition based on the specified field name.
     * This method retrieves the corresponding column name for the given field name
     * and initializes a new {@link Condition} instance which can be used to build query conditions.
     *
     * @param column The name of the table column field for which the condition is being created.
     * @return A new {@link Condition} instance representing the condition on the specified field.
     * @throws IllegalArgumentException if there is no column mapped to the given field name in the table.
     */
    public Condition<T> where(final String column) {
        return new Condition<>(column, new AbstractSelector<T>.SelectorStack());
    }

    protected abstract T get();
    public abstract List<T> getAll();
    public abstract Stream<T> stream();

    protected Map<String, Object> getRecord() {
        final List<Map<String, Object>> resultList = executeQuery();

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }

    /**
     * Retrieves all matching DTOs from the query result as a list.
     *
     * @return a list of DTOs matching the query conditions
     */
    protected List<Map<String, Object>> getAllRecords() {
        return streamRecords().toList();
    }

    /**
     * Provides a sequential stream of records from the query result.
     *
     * @return a {@link Stream} of @{code Map<String, Object>} corresponding to records return from query result.
     */
    protected Stream<Map<String, Object>> streamRecords() {
        final List<Map<String, Object>> resultList = executeQuery();

        if (resultList != null) {
            return resultList.stream();
        } else {
            return Stream.empty();
        }
    }

    private List<Map<String, Object>> executeQuery() {
        // Execute SQL query
        final List<Map<String, Object>> resultList;

        try {
            resultList = databaseProvider.select(tableMetaData, columns, conditions);
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        return resultList;
    }

    public final class SelectorStack {

        public Condition<T> where(final String field) {
            return AbstractSelector.this.where(field);
        }

        public void push(final Condition<T> condition) {
            conditions.add(condition);
        }

        public T get() {
            return AbstractSelector.this.get();
        }

        public List<T> getAll() {
            return AbstractSelector.this.getAll();
        }

        public Stream<T> stream() {
            return AbstractSelector.this.stream();
        }
    }
}
