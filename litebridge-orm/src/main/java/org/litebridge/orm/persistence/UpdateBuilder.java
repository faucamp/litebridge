package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.engine.LitebridgeContext;

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
        this.node = new UpdateNode(null, null, ormTable.dtoClass());
    }

    /**
     * Adds a set node to the statement.
     *
     * @param fieldName the field name to set
     * @param value     the value to set
     */
    public void setField(final String fieldName, final @Nullable Object value) {
        this.node = new SetNode(this.node, fieldName, value);
    }

    @Override
    public PreparedOperation build() {
        return litebridgeContext.createQueryCompiler().compile(node);
    }
}
