package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;
import java.util.stream.Stream;

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
        return select(Arrays.stream(fields).map(SelectField::new));
    }

    public DtoFromClauseTerminal<DTO> select(final SelectField... fields) {
        return select(Arrays.stream(fields));
    }

    public DtoFromClauseTerminal<DTO> selectAll() {
        selectSpec.setColumns(table.getMetaData().getColumns().keySet().stream().map(SelectField::new).toList());
        return new DtoFromClauseTerminal<>(this);
    }

    public DtoFromClauseTerminal<DTO> select(final Stream<SelectField> fields) {
        selectSpec.setColumns(fields.map(selectField -> {
                    // Map the input DTO field names to database column names
                    final Column column = table.getColumnForFieldName(selectField.name());
                    return new SelectField(column.getName(), selectField.alias());
                })
                .toList());
        return new DtoFromClauseTerminal<>(this);
    }

    Table table() {
        return table;
    }
}
