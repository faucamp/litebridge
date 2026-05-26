package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;

import java.util.Arrays;

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
        for (Column selectedColumn : selectSpec.columns()) {
            if (selectedColumn.equalsIgnoreAlias(column)) {
                column = selectedColumn;
                break;
            }
        }

        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate));
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final FieldColumnSpec field) {
        return where(field.field().name());
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
}
