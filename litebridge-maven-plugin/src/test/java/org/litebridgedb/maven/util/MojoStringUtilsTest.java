package org.litebridgedb.maven.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MojoStringUtilsTest {

    @Test
    void testCamelCase() {
        assertEquals("myVariableName", MojoStringUtils.camelCase("my_variable_name", true));
        assertEquals("MyVariableName", MojoStringUtils.camelCase("my_variable_name", false));
        assertEquals("myVariableName", MojoStringUtils.camelCase("my variable name", true));
        assertEquals("MyVariableName", MojoStringUtils.camelCase("my variable name", false));
        assertEquals("a", MojoStringUtils.camelCase("a", true));
        assertEquals("A", MojoStringUtils.camelCase("a", false));
        assertEquals("", MojoStringUtils.camelCase("", true));
        assertEquals("", MojoStringUtils.camelCase("   ", true));
        
        // Test with multiple separators
        assertEquals("myVariableName", MojoStringUtils.camelCase("my__variable--name", true));
        
        assertThrows(NullPointerException.class, () -> MojoStringUtils.camelCase(null, true));
    }

    @Test
    void testLowerFirst() {
        assertEquals("test", MojoStringUtils.lowerFirst("Test"));
        assertEquals("test", MojoStringUtils.lowerFirst("test"));
        assertEquals("", MojoStringUtils.lowerFirst(""));
        assertNull(MojoStringUtils.lowerFirst(null));
        assertEquals("a", MojoStringUtils.lowerFirst("A"));
    }

    @Test
    void testPluralise() {
        // Default rule
        assertEquals("accounts", MojoStringUtils.pluralise("account"));
        assertEquals("users", MojoStringUtils.pluralise("user"));

        // Ends in y preceded by consonant
        assertEquals("companies", MojoStringUtils.pluralise("company"));
        assertEquals("categories", MojoStringUtils.pluralise("category"));
        
        // Ends in y preceded by vowel (a, e, i, o, u)
        assertEquals("days", MojoStringUtils.pluralise("day"));
        assertEquals("keys", MojoStringUtils.pluralise("key"));
        assertEquals("alibis", MojoStringUtils.pluralise("alibi")); // Actually alibi ends in i, but test y preceded by i if possible?
        // Wait, alibi doesn't end in y.
        // Need words ending in y preceded by i or u.
        // "obloquy" (preceded by u, but q is the consonant? no, qu is often treated together)
        // "soliloquy" -> soliloquies (it's actually consonant-y in some rules)
        
        // Let's just use made up words to cover the branches
        assertEquals("ays", MojoStringUtils.pluralise("ay"));
        assertEquals("eys", MojoStringUtils.pluralise("ey"));
        assertEquals("iys", MojoStringUtils.pluralise("iy"));
        assertEquals("oys", MojoStringUtils.pluralise("oy"));
        assertEquals("uys", MojoStringUtils.pluralise("uy"));
        assertEquals("bies", MojoStringUtils.pluralise("by"));

        // Ends in s, sh, ch, x, or z
        assertEquals("buses", MojoStringUtils.pluralise("bus"));
        assertEquals("wishes", MojoStringUtils.pluralise("wish"));
        assertEquals("benches", MojoStringUtils.pluralise("bench"));
        assertEquals("boxes", MojoStringUtils.pluralise("box"));
        assertEquals("buzzes", MojoStringUtils.pluralise("buzz"));

        // Null or empty
        assertNull(MojoStringUtils.pluralise(null));
        assertEquals("", MojoStringUtils.pluralise(""));
        
        // y but length 1
        assertEquals("ys", MojoStringUtils.pluralise("y"));
    }
}
