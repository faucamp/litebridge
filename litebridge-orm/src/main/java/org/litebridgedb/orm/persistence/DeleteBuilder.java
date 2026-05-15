package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.update.Delete;

import java.util.ArrayList;
import java.util.List;

public final class DeleteBuilder extends AbstractStatementBuilder<Delete> {

    private final List<Condition> conditions = new ArrayList<>();

    public DeleteBuilder(final OrmTable table) {
        super(table);
    }

    public DeleteBuilder where(final Condition condition) {
        conditions.add(condition);
        return this;
    }

    @Override
    public Delete build() {
        return new Delete(new Table(ormTable.getMetaData().catalog(), ormTable.getMetaData().schema(), ormTable.getMetaData().name()), conditions);
    }
}
