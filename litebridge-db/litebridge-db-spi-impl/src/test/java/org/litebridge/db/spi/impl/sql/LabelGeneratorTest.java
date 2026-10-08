package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelGeneratorTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void createAliasAs() {
        assertEquals(" AS \"my_alias\"", labelGenerator.createAliasAs("my_alias"));
    }

    @Test
    void quoteAlias() {
        assertEquals("\"my_alias\"", labelGenerator.quoteAlias("my_alias"));
    }

    @Test
    void quoteIdentifier_reservedWord_isQuoted() {
        assertEquals("\"SELECT\"", labelGenerator.quoteIdentifier("SELECT"));
        assertEquals("\"WHERE\"", labelGenerator.quoteIdentifier("WHERE"));
    }

    @Test
    void quoteIdentifier_nonReservedWord_isNotQuoted() {
        assertEquals("my_column", labelGenerator.quoteIdentifier("my_column"));
        assertEquals("user_table", labelGenerator.quoteIdentifier("user_table"));
    }
}
