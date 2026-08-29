package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    private final List<LinkedHashMap<String, @Nullable  Object>> rows = new ArrayList<>();

    public InsertBuilder(final OrmTable table, final LitebridgeContext litebridgeContext) {
        super(table, litebridgeContext);
    }

    public void addRow(final LinkedHashMap<String, @Nullable Object> fieldValues) {
        rows.add(fieldValues);
    }

    @Override
    public PreparedOperation build() {
        return litebridgeContext.createQueryCompiler().compile(node);
    }

    @Override
    public QueryNode node() {
        if (node == null) {
            final String[] insertFields = rows.getFirst()
                    .sequencedKeySet()
                    .toArray(String[]::new);
            node = new InsertNode(null, ormTable.dtoClass(), insertFields);

            for (LinkedHashMap<String, @Nullable Object> fieldValues : rows) {
                final Object[] values = fieldValues.sequencedValues().toArray(Object[]::new);
                node = new InsertValuesNode(node, values);
            }
        }

        return node;
    }
}
