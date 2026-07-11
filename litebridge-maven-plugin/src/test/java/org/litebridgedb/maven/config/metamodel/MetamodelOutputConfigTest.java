package org.litebridgedb.maven.config.metamodel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetamodelOutputConfigTest {

    @Test
    void testGetClassNamePrefix() {
        final MetamodelOutputConfig config = new MetamodelOutputConfig();
        
        // Default: null -> ""
        assertEquals("", config.getClassNamePrefix());
        
        config.setClassNamePrefix("My");
        assertEquals("My", config.getClassNamePrefix());
    }

    @Test
    void testGetClassNameSuffix() {
        final MetamodelOutputConfig config = new MetamodelOutputConfig();
        
        // Case 1: Both null -> "Meta"
        assertEquals("Meta", config.getClassNameSuffix());
        
        // Case 2: Suffix null, Prefix not null -> ""
        config.setClassNamePrefix("My");
        assertEquals("", config.getClassNameSuffix());
        
        // Case 3: Suffix not null -> use it
        config.setClassNameSuffix("Suf");
        assertEquals("Suf", config.getClassNameSuffix());
    }

    @Test
    void testToString() {
        final MetamodelOutputConfig config = new MetamodelOutputConfig();
        config.setClassNamePrefix("Pre");
        config.setClassNameSuffix("Suf");
        String s = config.toString();
        assertTrue(s.contains("Pre"));
        assertTrue(s.contains("Suf"));
    }
}
