package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;

public final class DtoOrderByClauseChain<DTO>
        extends OrderByClauseTerminalImpl<DTO>
        implements OrderByClauseChain<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final Table table;

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
