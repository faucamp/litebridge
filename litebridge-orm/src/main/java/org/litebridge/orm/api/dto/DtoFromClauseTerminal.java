package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.GroupBySpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public final class DtoFromClauseTerminal<DTO> extends AbstractFromClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
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
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return where(new SelectColumnSpec(column));
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    /**
     * Adds a nested condition clause.
     * <p>
     * The nested condition clause is grouped with parentheses to ensure proper SQL syntax.
     *
     * @param query Function that builds the nested condition clause
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    public DtoWhereConditionClauseTerminal<DTO> where(final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec conditionGroupSpec = selectSpec.pushWhereConditionGroup(LogicOperator.NOOP);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(conditionGroupSpec, ormTable, delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        selectSpec.popWhereConditionGroup();
        return new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate);
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
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        selectSpec.setGroupBy(new GroupBySpec(selectSpec.createSelectFieldSpecs(fields)));
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... fields) {
        selectSpec.setGroupBy(new GroupBySpec(fields));
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(selectSpec.createSelectFieldSpecs(fields)), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(fields), (DtoSelector<DTO>) delegate);
    }

    private DtoWhereConditionClauseTerminal<DTO> createWithIdClause(final Object id) {
        final String[] primaryKeyFieldNames = ormTable.getMetaData().primaryKey().stream()
                .map(columnMetaData -> ormTable.getFieldForColumnName(columnMetaData.name()).name())
                .toArray(String[]::new);

        if (primaryKeyFieldNames.length == 0) {
            throw new IllegalArgumentException("No primary key fields found for table " + ormTable.getMetaData().name());
        } else if (primaryKeyFieldNames.length == 1) {
            return where(primaryKeyFieldNames[0]).eq(id);
        } else {
            // Composite PK
            if (id instanceof List<?> idList) {
                if (idList.size() != primaryKeyFieldNames.length) {
                    throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idList.size()));
                }

                DtoWhereConditionClauseTerminal<DTO> clause = where(primaryKeyFieldNames[0]).eq(idList.getFirst());

                for (int i = 1; i < primaryKeyFieldNames.length; i++) {
                    clause = clause.and(primaryKeyFieldNames[i]).eq(idList.get(i));
                }

                return clause;
            } else if (id instanceof Object[] idArray) {
                if (idArray.length != primaryKeyFieldNames.length) {
                    throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idArray.length));
                }

                DtoWhereConditionClauseTerminal<DTO> clause = where(primaryKeyFieldNames[0]).eq(idArray[0]);

                for (int i = 1; i < primaryKeyFieldNames.length; i++) {
                    clause = clause.and(primaryKeyFieldNames[i]).eq(idArray[i]);
                }

                return clause;
            } else if (id instanceof Map<?, ?> idMap) {
                if (idMap.size() != primaryKeyFieldNames.length) {
                    throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idMap.size()));
                }

                DtoWhereConditionClauseTerminal<DTO> clause = where(primaryKeyFieldNames[0]).eq(idMap.get(primaryKeyFieldNames[0]));

                for (int i = 1; i < primaryKeyFieldNames.length; i++) {
                    clause = clause.and(primaryKeyFieldNames[i]).eq(idMap.get(primaryKeyFieldNames[i]));
                }

                return clause;
            } else {
                throw new IllegalArgumentException("Invalid composite primary key value type provided; expected: List<?>, Object[], or Map<String, ?>");
            }
        }
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = selectSpec.currentWhereConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoWhereConditionClause<>(conditionSpec, new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate), delegate.litebridgeContext());
    }
}
