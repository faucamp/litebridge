package org.litebridge.db.spi.impl;

import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;

/**
 * Default sequence-based column value generator.
 * <p>
 * Generates a standard SQL fragment for fetching the next value from a database sequence.
 * This generator is intended for databases that support
 * the "{@code NEXT VALUE FOR}" syntax for sequence values.
 **/
public final class DefaultSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    /**
     * Constructs a new instance of {@code DefaultSequenceColumnValueGenerator}.
     *
     * @param sequence the name of the database sequence from which values will be generated.
     */
    public DefaultSequenceColumnValueGenerator(final String sequence) {
        super("NEXT VALUE FOR %s".formatted(sequence));
    }
}
