package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.Collections;
import java.util.List;

/**
 * A builder class for constructing SQL UPDATE statements.
 * <p>
 * The {@code UpdateBuilder} is responsible for creating instances of the {@link Update} class
 * by specifying the target table, column values to update, and the conditions that determine
 * which rows are affected.
 * <p>
 * Instances of this class support method chaining for a fluent API style.
 * <p>
 * It extends {@link AbstractConditionalStatementBuilder} with {@link Update} as the specific statement type.
 */
final class UpdateBuilder extends AbstractConditionalStatementBuilder {

    public UpdateBuilder(final OrmTable ormTable,
                         final LitebridgeContext litebridgeContext) {
        super(ormTable, litebridgeContext);
        this.node = new UpdateNode(null, ormTable.getMetaData().toTable());
    }

    /**
     * Adds a set node to the statement.
     *
     * @param column    the column to set
     * @param value     the value to set
     * @param bindValue whether to bind the value as a parameter
     */
    public void addSetNode(Column column, @Nullable Object value, boolean bindValue) {
        this.node = new SetNode(this.node, column, value, bindValue);
    }

    /**
     * Adds a column value to the statement.
     *
     * @param columnValue the column value to add
     */
    public void addColumn(final ColumnValue columnValue) {
        addSetNode(columnValue.column(), columnValue.value(), true);
    }

    @Override
    public PreparedOperation build() {
        final UpdateSpec updateSpec = new UpdateSpec(ormTable.getMetaData().toTable(), litebridgeContext.selectExpressionMapper());
        litebridgeContext.createQueryCompiler().compile(node, updateSpec);
        return updateSpec.toUpdate(litebridgeContext.tableMetaDataCache(), litebridgeContext.typeConverter());
    }
}
