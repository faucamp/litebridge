package org.litebridgedb.db.spi.impl;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;

/**
 * A concrete implementation of {@code SequenceColumnValueGenerator} that provides
 * a standardized SQL fragment for fetching the next value from a database sequence.
 * This generator is intended to work with databases that support the "NEXT VALUE FOR"
 * syntax for sequence values.
 * <p>
 * This class constructs the sequence-based SQL fragment by using the provided sequence
 * name, which must be supplied during the instantiation of the class. The generated
 * SQL can be directly embedded into INSERT or UPDATE statements.
 */
public class DefaultSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    /**
     * Constructs a new instance of {@code DefaultSequenceColumnValueGenerator}.
     *
     * @param sequence the name of the database sequence from which values will be generated.
     */
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
