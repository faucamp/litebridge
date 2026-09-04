package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A builder class for constructing SQL INSERT statements.
 */
final class InsertBuilder extends AbstractStatementBuilder {

    private final List<LinkedHashMap<String, @Nullable Object>> rows = new ArrayList<>();

    public InsertBuilder(final OrmTable table, final LitebridgeContext litebridgeContext) {
        super(table, litebridgeContext);
    }

    public void addRow(final LinkedHashMap<String, @Nullable Object> fieldValues) {
        rows.add(fieldValues);
    }

    @Override
    public void setField(final String fieldName, final @Nullable Object value) {
        if (!rows.isEmpty()) {
            rows.getLast().put(fieldName, value);
            this.node = null;
        }
    }

    @Override
    public PreparedOperation build() {
        return litebridgeContext.createQueryCompiler().compile(node());
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
