package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Aliased;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;

public final class DtoSelector<DTO> extends AbstractSelector<DTO> {

    private final Class<DTO> dtoClass;
    private final Table table;

    public DtoSelector(final Class<DTO> dtoClass,
                       final Table table,
                       final DatabaseProvider databaseProvider,
                       final DtoMapper<DTO> dtoMapper) {
        super(new SelectSpec(), databaseProvider, dtoMapper);
        this.dtoClass = dtoClass;
        this.table = table;
        selectSpec.setTable(table.getMetaData());
    }

    public DtoFromClauseTerminal<DTO> select(final String... fields) {
        selectSpec.setColumns(Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData column = table.getColumnForFieldName(field);
                    return new Column(table.getMetaData(), column.name());
                })
                .toList());

        return new DtoFromClauseTerminal<>(this);
    }

    public DtoFromClauseTerminal<DTO> select(final Aliased... fields) {
        selectSpec.setColumns(Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData column = table.getColumnForFieldName(field.name());
                    return new Column(table.getMetaData(), column.name(), field.alias());
                })
                .toList());

        return new DtoFromClauseTerminal<>(this);
    }

    public DtoFromClauseTerminal<DTO> selectAll() {
        selectSpec.setColumns(table.getMetaData().columns().stream()
                .map(column -> (Column) column)
                .toList());
        return new DtoFromClauseTerminal<>(this);
    }

    Table table() {
        return table;
    }
}
