package org.litebridgedb.commons.collector;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MapCollectorTest {

    @Test
    void toLinkedHashMap() {
        // Given
        final Stream<String> stream = Stream.of("v1", "v2", "v2", "v3");

        // When
        final LinkedHashMap<String, String> result = stream
                .collect(MapCollector.toLinkedHashMap(Function.identity(), String::toUpperCase));

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("V1", result.get("v1"));
        assertEquals("V2", result.get("v2"));
        assertEquals("V3", result.get("v3"));
    }
}