package org.litebridge.orm.api.register;

import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;

/**
 * A placeholder implementation of {@link SequenceColumnValueGenerator}.
 * <p>
 * This class throws an exception when attempting to generate a SQL fragment for a sequence value.
 * It is overridden by Litebridge with the actual database provider's sequence generator during registration.
 */
final class PlaceholderSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    /**
     * Creates a new instance of {@link PlaceholderSequenceColumnValueGenerator}.
     *
     * @param sequence The name of the sequence associated with this generator.
     */
    public PlaceholderSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    /**
     * Retrieves the name of the sequence associated with this generator.
     *
     * @return The sequence name as a {@code String}.
     */
    public String sequence() {
        return sequenceNextValSqlFragment;
    }
}
