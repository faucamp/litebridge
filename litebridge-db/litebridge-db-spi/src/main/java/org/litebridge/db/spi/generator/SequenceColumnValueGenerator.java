package org.litebridge.db.spi.generator;

/**
 * Abstract base class for generating SQL fragments to retrieve the next value from a database sequence
 * to be used in SQL statements such as INSERT or UPDATE.
 */
public abstract class SequenceColumnValueGenerator implements ColumnValueGenerator {

    protected final String sequenceNextValSqlFragment;

    /**
     * Constructs a new instance of {@code SequenceColumnValueGenerator}.
     *
     * @param sequenceNextValSqlFragment the SQL fragment for retrieving the next value in the sequence.
     */
    public SequenceColumnValueGenerator(final String sequenceNextValSqlFragment) {
        this.sequenceNextValSqlFragment = sequenceNextValSqlFragment;
    }

    /**
     * Generate a SQL fragment to retrieve the next value from a sequence.
     * <p>
     * This fragment is meant for direct use in an INSERT or UPDATE statement.
     * For example, to generate {@code INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (NEXT VALUE FOR sequence_name, ?)},
     * this method would return the {@code NEXT VALUE FOR sequence_name} SQL fragment.
     *
     * @return a formatted SQL string representing the next sequence value for direct insertion
     */
    @Override
    public String generate() {
        return sequenceNextValSqlFragment;
    }
}
