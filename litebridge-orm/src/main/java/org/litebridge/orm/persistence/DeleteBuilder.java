package org.litebridge.orm.persistence;

import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.LitebridgeContext;

/**
 * A builder class for constructing SQL DELETE statements.
 * <p>
 * This class provides an API to facilitate the creation of DELETE statements
 * targeting a specific table with optional conditions.
 */
public final class DeleteBuilder extends AbstractConditionalStatementBuilder {

    /**
     * Constructs a {@code DeleteBuilder} for the specified ORM table.
     *
     * @param table             the table to delete from
     * @param litebridgeContext the ORM context
     */
    public DeleteBuilder(final OrmTable table, final LitebridgeContext litebridgeContext) {
        super(table, litebridgeContext);
        this.node = new DeleteNode(null, table.getMetaData().qualifiedName(), null);
    }

    @Override
    public PreparedOperation build() {
        return litebridgeContext.createQueryCompiler().compile(node);
    }
}
