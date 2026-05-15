package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.OrderByClauseChain;
import org.litebridgedb.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridgedb.orm.persistence.OrmTable;

import java.util.Arrays;

public final class DtoOrderByClauseChain<DTO>
        extends OrderByClauseTerminalImpl<DTO, DtoSelectSpec>
        implements OrderByClauseChain<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final OrmTable table;

    public DtoOrderByClauseChain(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
    }

    @Override
    public DtoOrderByClause<DTO> then(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(table::getColumnForFieldName)
                .map(ColumnMetaData::name)
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }
}
