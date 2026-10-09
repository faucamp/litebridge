package org.litebridge.orm.api.condition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class QueryConditionBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    void functionalInterfaceUsage() {
        // Given
        final DtoConditionClauseStart<String> start = mock(DtoConditionClauseStart.class);
        final CbDtoConditionClauseTerminal<String> terminal = mock(CbDtoConditionClauseTerminal.class);
        final DtoQueryConditionBuilder<String> builder = s -> terminal;

        // When
        final CbDtoConditionClauseTerminal<String> result = builder.apply(start);

        // Then
        assertNotNull(result);
        assertEquals(terminal, result);
    }
}
