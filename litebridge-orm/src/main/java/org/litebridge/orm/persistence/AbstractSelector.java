package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelector<T> {

    protected final List<String> columns;
    protected final DatabaseProvider databaseProvider;
    protected final List<org.litebridge.db.api.query.Condition> conditions = new ArrayList<>();
    protected final List<String> orderByColumns = new ArrayList<>();
    protected final TableMetaData tableMetaData;
    protected Integer offset;
    protected Integer limit;

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

    public Optional<T> one() {
        return Optional.ofNullable(oneOrNull());
    }

    public abstract @Nullable T oneOrNull();

    public T oneOrThrow() {
        return oneOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    public <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return one().orElseThrow(exceptionSupplier);
    }

    public Optional<T> first() {
        return Optional.ofNullable(firstOrNull());
    }

    public abstract @Nullable T firstOrNull();

    public T firstOrThrow() {
        return oneOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    public <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return one().orElseThrow(exceptionSupplier);
    }

    public abstract List<T> list();

    public abstract Stream<T> stream();

    protected Map<String, Object> getOneRecord(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            this.limit = 1;
        }

        final List<Map<String, Object>> resultList = executeQuery();

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
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
            resultList = databaseProvider.select(tableMetaData, columns, conditions, orderByColumns, offset, limit);
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        return resultList;
    }

    public SelectorStack orderBy(final String column) {
        orderByColumns.add(column);
        return new SelectorStack();
    }

    public SelectorStack offset(final int offset) {
        this.offset = offset;
        return new SelectorStack();
    }

    private SelectorStack limit(final int limit) {
        this.limit = limit;
        return new SelectorStack();
    }

    public final class SelectorStack {

        public Condition<T> where(final String field) {
            return AbstractSelector.this.where(field);
        }

        public SelectorStack orderBy(final String field) {
            return AbstractSelector.this.orderBy(field);
        }

        public SelectorStack offset(final int offset) {
            return AbstractSelector.this.offset(offset);
        }

        public SelectorStack limit(final int limit) {
            return AbstractSelector.this.limit(limit);
        }

        public void push(final Condition<T> condition) {
            conditions.add(condition);
        }

        public Optional<T> one() {
            return AbstractSelector.this.one();
        }

        public T oneOrNull() {
            return AbstractSelector.this.oneOrNull();
        }

        public T oneOrThrow() {
            return AbstractSelector.this.oneOrThrow();
        }

        public <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return AbstractSelector.this.oneOrThrow(exceptionSupplier);
        }

        public Optional<T> first() {
            return AbstractSelector.this.first();
        }

        public T firstOrNull() {
            return AbstractSelector.this.firstOrNull();
        }

        public T firstOrThrow() {
            return AbstractSelector.this.firstOrThrow();
        }

        public <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return AbstractSelector.this.firstOrThrow(exceptionSupplier);
        }

        public List<T> list() {
            return AbstractSelector.this.list();
        }

        public Stream<T> stream() {
            return AbstractSelector.this.stream();
        }
    }
}
