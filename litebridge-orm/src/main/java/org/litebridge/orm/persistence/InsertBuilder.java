package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.Insert;

import java.util.ArrayList;
import java.util.List;

final class InsertBuilder extends AbstractStatementBuilder<Insert> {

    private final List<DtoRowValue> rows = new ArrayList<>();

    public InsertBuilder(final Table table) {
        super(table);
    }

    public InsertBuilder add(final DtoRowValue dtoRowValue) {
        rows.add(dtoRowValue);
        return this;
    }

    @Override
    public Insert build() {
        return new Insert(table.getMetaData(), rows.stream().map(DtoRowValue::rowValue).toList());
    }
}
