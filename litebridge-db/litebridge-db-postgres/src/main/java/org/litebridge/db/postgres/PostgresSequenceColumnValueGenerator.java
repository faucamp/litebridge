package org.litebridge.db.postgres;

import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;

/**
 * Implementation of {@link SequenceColumnValueGenerator} for PostgreSQL databases.
 * <p>
 * This class generates SQL fragments to retrieve the next value from a Postgres sequence
 * when used in SQL statements like INSERT or UPDATE, in the format: "nextval('sequence_name')".
 * <p>
 * For example, to generate {@code INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (nextval('sequence_name'), ?)},
 * this generator returns "{@code nextval('sequence_name')}".
 */
public final class PostgresSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    /**
     * Constructs a new {@code PostgresSequenceColumnValueGenerator} with the specified sequence name.
     *
     * @param sequence the name of the sequence to use
     */
    public PostgresSequenceColumnValueGenerator(final String sequence) {
        super("nextval('%s')".formatted(sequence));
    }
}
