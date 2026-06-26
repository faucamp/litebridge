package org.litebridgedb.db.oracle;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;

/**
 * Implementation of {@link SequenceColumnValueGenerator} for Oracle databases.
 * <p>
 * This class generates SQL fragments to retrieve the next rhs from an Oracle sequence
 * when used in SQL statements like INSERT or UPDATE.
 */
public final class OracleSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    public OracleSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    /**
     * Generate a SQL fragment to retrieve the next rhs from a sequence for direct use in an INSERT or UPDATE statement,
     * e.g. to generate "INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (sequence_name.NEXTVAL, ?)",
     * this method returns "sequence_name.NEXTVAL".
     *
     * @param columnMetaData Column to generate a rhs for
     * @return a formatted SQL string representing the next sequence rhs for direct insertion
     */
    @Override
    public String generate(final ColumnMetaData columnMetaData) {
        return "%s.NEXTVAL".formatted(sequence);
    }
}
