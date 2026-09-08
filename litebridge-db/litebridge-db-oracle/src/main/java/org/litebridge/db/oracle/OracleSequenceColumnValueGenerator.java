package org.litebridge.db.oracle;

import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;

/**
 * Implementation of {@link SequenceColumnValueGenerator} for Oracle databases.
 * <p>
 * This class generates SQL fragments to retrieve the next value from an Oracle sequence
 * when used in SQL statements like INSERT or UPDATE, in the format: "sequence_name.NEXTVAL".
 * <p>
 * For example, to generate {@code INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (sequence_name.NEXTVAL, ?)},
 * this generator returns "{@code sequence_name.NEXTVAL}".
 */
public final class OracleSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    /**
     * Constructs a new {@code OracleSequenceColumnValueGenerator} with the specified sequence name.
     *
     * @param sequence the name of the sequence to use
     */
    public OracleSequenceColumnValueGenerator(final String sequence) {
        super("%s.NEXTVAL".formatted(sequence));
    }
}
