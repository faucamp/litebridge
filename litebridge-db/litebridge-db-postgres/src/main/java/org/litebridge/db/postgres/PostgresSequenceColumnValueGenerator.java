package org.litebridge.db.postgres;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;

/**
 * Implementation of {@link SequenceColumnValueGenerator} for PostgreSQL databases.
 * <p>
 * This class generates SQL fragments to retrieve the next value from a Postgres sequence
 * when used in SQL statements like INSERT or UPDATE.
 */
public class PostgresSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    public PostgresSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    /**
     * Generate a SQL fragment to retrieve the next value from a sequence for direct use in an INSERT or UPDATE statement,
     * e.g. to generate "INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (nextval('sequence_name'), ?)",
     * this method returns "nextval('sequence_name')".
     *
     * @param columnMetaData Column to generate a value for
     * @return a formatted SQL string representing the next sequence value for direct insertion
     */
    @Override
    public String generate(final ColumnMetaData columnMetaData) {
        return "nextval('%s')".formatted(sequence);
    }
}
