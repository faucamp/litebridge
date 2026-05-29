package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;

/**
 * A placeholder implementation of {@link SequenceColumnValueGenerator}.
 * <p>
 * This class throws an exception when attempting to generate a SQL fragment for a sequence value.
 * It is overridden by Litebridge with the actual database provider's sequence generator during registration.
 */
final class PlaceholderSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    public PlaceholderSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    @Override
    public String generate(final ColumnMetaData columnMetaData) {
        throw new UnsupportedOperationException("Not supported; regression (placeholder not replaced)");
    }

    /**
     * Retrieves the name of the sequence associated with this generator.
     *
     * @return The sequence name as a {@code String}.
     */
    public String sequence() {
        return sequence;
    }
}
