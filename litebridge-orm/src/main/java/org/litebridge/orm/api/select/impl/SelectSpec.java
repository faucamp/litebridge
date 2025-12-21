package org.litebridge.orm.api.select.impl;

import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.SelectField;

import java.util.List;

public class SelectSpec {

    private TableMetaData table;
    private List<SelectField> columns;
    private LimitSpec limit;


    public TableMetaData getTable() {
        return table;
    }

    public void setTable(final TableMetaData table) {
        this.table = table;
    }

    public List<SelectField> getColumns() {
        return columns;
    }

    public void setColumns(final List<SelectField> columns) {
        this.columns = columns;
    }

    public LimitSpec getLimit() {
        return limit;
    }

    public void setLimit(final LimitSpec limit) {
        this.limit = limit;
    }

    public LimitSpec ensureLimit() {
        if (this.limit == null) {
            limit = new LimitSpec();
        }

        return limit;
    }

    public Select toSelect() {
        return new Select(table, columns, null, null, null,  limit.toLimit());
    }
}
