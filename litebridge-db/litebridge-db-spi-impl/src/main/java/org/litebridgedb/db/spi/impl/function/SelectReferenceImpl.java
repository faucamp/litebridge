package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.SelectReference;

public class SelectReferenceImpl extends SelectReference {

    /**
     * Constructor.
     *
     * @param column The target selected column to reference.
     */
    protected SelectReferenceImpl(final Column column) {
        super(column);
    }

    @Override
    public String toSql(final Operation operation) {
        return "";
    }
}
