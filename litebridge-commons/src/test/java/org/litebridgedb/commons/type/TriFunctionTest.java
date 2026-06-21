package org.litebridgedb.commons.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TriFunctionTest {

    @Test
    void apply() {
        final TriFunction<String, String, String, String> triFunction = (a, b, c) -> a + b + c;
        assertEquals("abc", triFunction.apply("a", "b", "c"));
    }
}