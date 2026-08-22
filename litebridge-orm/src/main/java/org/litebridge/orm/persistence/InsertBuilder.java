package org.litebridge.orm.persistence;

import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.List;

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
    }

    public void addRow(final List<ColumnValue> columnValues) {
        if (node == null) {
            final String[] insertColumns = columnValues.stream()
                    .map(columnValue -> columnValue.column().name())
                    .toArray(String[]::new);
            node = new InsertNode(null, ormTable.dtoClass(), insertColumns);
        }

        final Object[] values = columnValues.stream()
                .map(columnValue -> columnValue.value())
                .toArray(Object[]::new);
        node = new InsertValuesNode(node, values);
    }

    @Override
    public PreparedOperation build() {
        return litebridgeContext.createQueryCompiler().compile(node);
    }
}
