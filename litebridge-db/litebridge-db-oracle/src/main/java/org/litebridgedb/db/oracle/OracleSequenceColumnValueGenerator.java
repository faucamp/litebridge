package org.litebridgedb.db.oracle;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;

public class OracleSequenceColumnValueGenerator extends SequenceColumnValueGenerator {

    public OracleSequenceColumnValueGenerator(final String sequence) {
        super(sequence);
    }

    @Override
    public String generate(final ColumnMetaData columnMetaData) {
        return "%s.NEXTVAL".formatted(sequence);
    }
}
