package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.JoinClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.OrmTable;

import java.util.Arrays;

public final class DtoJoinConditionClauseTerminal<DTO>
        extends AbstractJoinConditionClauseTerminal<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec,
        DtoJoinSpec>

        implements JoinClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>,

        DtoJoinClassTerminal<DTO> {

    private final OrmTable table;
    private final AliasGenerator aliasGenerator;

    public DtoJoinConditionClauseTerminal(final DtoJoinSpec joinSpec, final DtoSelector<DTO> delegate, final AliasGenerator aliasGenerator) {
        super(joinSpec, delegate);
        this.table = delegate.table();
        this.aliasGenerator = aliasGenerator;
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), table.getColumnForFieldName(field));
        return new DtoJoinConditionClause<>(joinSpec.newCondition(column), this);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), table.getColumnForFieldName(field));
        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate));
    }

    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(table::getColumnForFieldName)
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
