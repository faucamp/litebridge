package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.FromClauseTerminalImpl;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;
import java.util.stream.Stream;

public class DtoSelector<DTO> extends AbstractSelector<DTO> {

    private final Class<DTO> dtoClass;
    private final Table table;

    public DtoSelector(final Class<DTO> dtoClass, final Table table, final DatabaseProvider databaseProvider) {
        super(new SelectSpec(), databaseProvider, new DefaultDtoMapper<>(dtoClass, table, databaseProvider.getTypeConverter()));
        this.dtoClass = dtoClass;
        this.table = table;
    }

    public FromClauseTerminalImpl<DTO> select(final String... fields) {
        return select(Arrays.stream(fields).map(SelectField::new));
    }

    public FromClauseTerminalImpl<DTO> select(final SelectField... fields) {
        return select(Arrays.stream(fields));
    }

    public FromClauseTerminalImpl<DTO> from() {
        return new FromClauseTerminalImpl<>(this);
    }

    private FromClauseTerminalImpl<DTO> select(final Stream<SelectField> fields) {
        selectSpec.setColumns(fields.map(selectField -> {
                    // Map the input DTO field names to database column names
                    final Column column = table.getColumnForFieldName(selectField.name());
                    return new SelectField(column.getName(), selectField.alias());
                })
                .toList());
        return new FromClauseTerminalImpl<>(this);
    }
}
