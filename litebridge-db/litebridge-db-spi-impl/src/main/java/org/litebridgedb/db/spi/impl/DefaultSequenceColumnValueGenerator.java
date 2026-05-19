package org.litebridgedb.db.spi.impl;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;

public class DefaultSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    public DefaultSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    /**
     * Generate a SQL fragment to retrieve the next value from a sequence for direct use in an INSERT or UPDATE statement,
     * e.g. to generate "INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (NEXT VALUE FOR sequence_name, ?)",
     * this method returns "NEXT VALUE FOR sequence_name".
     *
     * @param columnMetaData Column to generate a value for
     * @return a formatted SQL string representing the next sequence value for direct insertion
     */
    @Override
    public String generate(final ColumnMetaData columnMetaData) {
        return "NEXT VALUE FOR %s".formatted(sequence);
    }
}
