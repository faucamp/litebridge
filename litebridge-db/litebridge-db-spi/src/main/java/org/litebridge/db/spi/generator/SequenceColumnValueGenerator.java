package org.litebridge.db.spi.generator;

import org.litebridge.db.spi.ColumnMetaData;

/**
 * Abstract base class for generating SQL fragments to retrieve the next value from a database sequence
 * to be used in SQL statements such as INSERT or UPDATE.
 * <p>
 * Subclasses must implement the {@link #generate(ColumnMetaData)} method to provide database-specific
 * SQL syntax for fetching the next sequence value.
 */
public abstract class SequenceColumnValueGenerator implements ColumnValueGenerator {

    /**
     * The name of the database sequence.
     */
    protected final String sequence;

    /**
     * Constructs a new instance of {@code SequenceColumnValueGenerator}.
     *
     * @param sequence the name of the database sequence from which values will be generated.
     *                 This sequence name is used to create the SQL fragment for retrieving
     *                 the next value in the sequence.
     */
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
