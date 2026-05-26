package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;

final class PlaceholderSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    public PlaceholderSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    @Override
    public String generate(final ColumnMetaData columnMetaData) {
        throw new UnsupportedOperationException("Not supported; regression (placeholder not replaced)");
    }

    public String sequence() {
        return sequence;
    }
}
