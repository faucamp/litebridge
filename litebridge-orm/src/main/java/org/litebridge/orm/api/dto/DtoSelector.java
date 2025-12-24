package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.FromClauseTerminalImpl;
import org.litebridge.orm.api.select.model.OrderBySpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public class DtoSelector<DTO> extends AbstractSelector<DTO> {

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

    public FromClauseTerminalImpl<DTO> select(final String... fields) {
        return select(Arrays.stream(fields).map(SelectField::new));
    }

    public FromClauseTerminalImpl<DTO> select(final SelectField... fields) {
        return select(Arrays.stream(fields));
    }

    public FromClauseTerminalImpl<DTO> selectAll() {
        selectSpec.setColumns(table.getMetaData().getColumns().keySet().stream().map(SelectField::new).toList());
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

    @Override
    protected List<LinkedHashMap<String, Object>> executeQuery(final SelectSpec selectSpec) {
        // Translate the SelectSpec from DTO field names to database column names
        final SelectSpec mappedSelectSpec = new SelectSpec();
        mappedSelectSpec.setColumns(selectSpec.getColumns());
        mappedSelectSpec.setTable(selectSpec.getTable());
        mappedSelectSpec.setJoins(selectSpec.getJoins());
        mappedSelectSpec.setLimit(selectSpec.getLimit());

        if (selectSpec.getWhereConditions() != null) {
            selectSpec.getWhereConditions()
                    .forEach(whereCondition -> whereCondition.setColumn(columnName(whereCondition.getColumn())));
            mappedSelectSpec.setWhereConditions(selectSpec.getWhereConditions());
        }

        if (selectSpec.getOrderBys() != null) {
            mappedSelectSpec.setOrderBys(selectSpec.getOrderBys().stream()
                    .map(orderBySpec -> {
                        final String[] mappedColumnNames = Arrays.stream(orderBySpec.columns())
                                .map(this::columnName)
                                .toArray(String[]::new);
                        return new OrderBySpec(mappedColumnNames, orderBySpec.isAsc());
                    })
                    .toList());
        }

        return super.executeQuery(mappedSelectSpec);
    }

    private String columnName(final String fieldName) {
        return table.getColumnForFieldName(fieldName).getName();
    }
}
