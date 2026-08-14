package org.litebridge.orm.persistence;

import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.orm.api.insert.model.InsertSpec;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;

/**
 * A builder class for constructing SQL INSERT statements.
 * <p>
 * The {@code InsertBuilder} is responsible for creating instances of the {@link Insert} class
 * by specifying the target table, column values to insert, and whether to return generated keys.
 * <p>
 * Instances of this class support method chaining for a fluent API style.
 */
final class InsertBuilder extends AbstractStatementBuilder {

    public InsertBuilder(final OrmTable table, final LitebridgeContext litebridgeContext) {
        super(table, litebridgeContext);
        this.node = new InsertNode(null, ormTable.getMetaData().toTable(), null);
    }

    @Override
    public PreparedOperation build() {
        final InsertSpec insertSpec = new InsertSpec(ormTable.getMetaData().toTable(), litebridgeContext.selectExpressionMapper());
        litebridgeContext.createQueryCompiler().compile(node, insertSpec);
        return insertSpec.toInsert(litebridgeContext.tableMetaDataCache(), litebridgeContext.typeConverter());
    }
}
