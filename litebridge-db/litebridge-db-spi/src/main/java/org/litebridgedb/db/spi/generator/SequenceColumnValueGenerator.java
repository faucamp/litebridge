package org.litebridgedb.db.spi.generator;

import org.litebridgedb.db.spi.ColumnMetaData;

public abstract class SequenceColumnValueGenerator implements ColumnValueGenerator {

    protected final String sequence;

    public SequenceColumnValueGenerator(final String sequence) {
        this.sequence = sequence;
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
    public abstract String generate(final ColumnMetaData columnMetaData);
}
