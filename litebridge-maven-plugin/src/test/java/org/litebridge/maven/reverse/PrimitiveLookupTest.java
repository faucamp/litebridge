package org.litebridge.maven.reverse;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PrimitiveLookupTest {

    @Test
    void testGetPrimitiveClass_String() throws ClassNotFoundException {
        assertEquals(boolean.class, PrimitiveLookup.getPrimitiveClass("boolean"));
        assertEquals(int.class, PrimitiveLookup.getPrimitiveClass("int"));
        assertEquals(String.class, PrimitiveLookup.getPrimitiveClass("java.lang.String"));
        assertThrows(ClassNotFoundException.class, () -> PrimitiveLookup.getPrimitiveClass("com.nonexistent.Unknown"));
    }

    @Test
    void testGetPrimitiveClass_Class() {
        assertEquals(int.class, PrimitiveLookup.getPrimitiveClass(int.class));
        assertEquals(int.class, PrimitiveLookup.getPrimitiveClass(Integer.class));
        assertEquals(String.class, PrimitiveLookup.getPrimitiveClass(String.class));
        assertEquals(long.class, PrimitiveLookup.getPrimitiveClass(Long.class));
        assertEquals(boolean.class, PrimitiveLookup.getPrimitiveClass(Boolean.class));
    }
}
