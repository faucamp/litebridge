package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.update.Delete;

import java.util.Objects;

/**
 * A builder class for constructing SQL DELETE statements.
 * <p>
 * This class provides an API to facilitate the creation of DELETE statements
 * targeting a specific table with optional conditions.
 */
public final class DeleteBuilder extends AbstractStatementBuilder<Delete> {

    private @Nullable ConditionGroup conditions;

    public DeleteBuilder(final OrmTable table) {
        super(table);
    }

    public DeleteBuilder where(final ConditionGroup conditionGroup) {
        this.conditions = conditionGroup;
        return this;
    }

    @Override
    public Delete build() {
        return new Delete(new Table(ormTable.getMetaData().catalog(), ormTable.getMetaData().schema(), ormTable.getMetaData().name()), Objects.requireNonNull(conditions));
    }
}
