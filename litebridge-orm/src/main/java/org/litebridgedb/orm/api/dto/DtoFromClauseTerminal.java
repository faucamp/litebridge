package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public final class DtoFromClauseTerminal<DTO> extends AbstractFromClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements DtoJoinClassTerminal<DTO> {

    private final TableRegistry tableRegistry;
    private final OrmTable ormTable;

    public DtoFromClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        tableRegistry = delegate.tableRegistry();
        ormTable = delegate.table();
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        Column column = ormTable.getColumnForFieldName(field).toColumn();

        // Use the aliased column if it is part of the SELECT clause, else use the unaliased column
        if (!selectSpec.getExpressions().isEmpty()) {
            Column replacementColumn = null;

            // Look for an exact column match
            for (ExpressionSpec expressionSpec : selectSpec.getExpressions()) {
                Column selectedColumn;

                if (expressionSpec instanceof ColumnExpressionSpec columnExpression) {
                    selectedColumn = columnExpression.column();
                } else {
                    continue;
                }

                if (selectedColumn.equalsIgnoreAlias(column)) {
                    replacementColumn = selectedColumn;
                    break;
                }
            }

            // No exact match; use the table's alias if it matches
            if (replacementColumn == null && column.table().equalsIgnoreAlias(selectSpec.getTable())) {
                replacementColumn = new Column(selectSpec.getTable(), column.name(), column.alias());
            }

            if (replacementColumn != null) {
                column = replacementColumn;
            }
        } else {
            // Select all - override the column's table with the selected one
            if (column.table() != selectSpec.getTable() && column.table().equalsIgnoreAlias(selectSpec.getTable())) {
                column = new Column(selectSpec.getTable(), column.name(), column.alias());
            }
        }

        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate), delegate.litebridgeContext());
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final FieldColumnSpec field) {
        return where(field.field().name());
    }

    /**
     * Convenience method to select a DTO by its primary key.
     *
     * @param id the primary key value
     * @return the selected DTO, if found
     */
    public Optional<DTO> withId(final Object id) {
        return createWithIdClause(id).one();
    }

    /**
     * Convenience method to select a DTO by its primary key.
     *
     * @param id the primary key value
     * @return the selected DTO, or {@code null} if not found
     */
    public @Nullable DTO withIdOrNull(final Object id) {
        return createWithIdClause(id).oneOrNull();
    }

    /**
     * Retrieves a DTO by its primary key and throws an exception if no matching entry is found.
     *
     * @param id the primary key value used to identify the DTO
     * @return the DTO associated with the given primary key
     * @throws NoSuchElementException if no DTO is found with the specified primary key
     */
    public DTO withIdOrThrow(final Object id) throws NoSuchElementException {
        return createWithIdClause(id).oneOrThrow();
    }

    /**
     * Retrieves a DTO by its primary key and throws the specified exception if no matching entry is found.
     *
     * @param id                the primary key value used to identify the DTO
     * @param exceptionSupplier a supplier that provides the exception to be thrown if the DTO is not found
     * @param <X>               the type of exception to be thrown
     * @return the DTO associated with the given primary key
     * @throws X the exception provided by the supplier if no DTO is found with the specified primary key
     */
    public <X extends Throwable> DTO withIdOrThrow(final Object id, final Supplier<? extends X> exceptionSupplier) throws X {
        return createWithIdClause(id).oneOrThrow(exceptionSupplier);
    }

    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
        final OrmTable joinTable;

        // First check for inline/contextually-registered tables
        final OrmTable contextScopedTable = ormTable.getContextTableRegistry().getTable(dtoClass);

        if (contextScopedTable != null) {
            joinTable = contextScopedTable;
        } else {
            joinTable = tableRegistry.getTableOrThrow(dtoClass);
        }

        return new DtoJoinClause<>(dtoClass, joinTable, (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(ormTable::getColumnForFieldName)
                .map(ColumnMetaData::name)
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final FieldColumnSpec... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(fieldColumnSpec -> fieldColumnSpec.columnSpec().name())
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }

    private DtoWhereConditionClauseTerminal<DTO> createWithIdClause(final Object id) {
        final String[] primaryKeyFieldNames = ormTable.getMetaData().primaryKey().stream()
                .map(columnMetaData -> ormTable.getFieldForColumnName(columnMetaData.name()).name())
                .toArray(String[]::new);

        DtoWhereConditionClauseTerminal<DTO> clause = where(primaryKeyFieldNames[0]).eq(id);

        for (int i = 1; i < primaryKeyFieldNames.length; i++) {
            clause = clause.and(primaryKeyFieldNames[i]).eq(id);
        }

        return clause;
    }
}
