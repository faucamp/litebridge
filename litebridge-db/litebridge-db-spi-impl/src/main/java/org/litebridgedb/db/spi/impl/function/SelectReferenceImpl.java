package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;

public class SelectReferenceImpl extends SelectReference {

    private final ColumnIdentifierGenerator columnIdentifierGenerator;

    /**
     * Constructor.
     *
     * @param column                    The target selected column to reference.
     * @param columnIdentifierGenerator The column identifier generator to use.
     */
    public SelectReferenceImpl(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column);
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    @Override
    public String toSql(final Operation operation) {
        return columnIdentifierGenerator.createColumnReference(column);
    }
}
