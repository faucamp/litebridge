package org.litebridge.orm.api.condition;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class QueryConditionBuilderTest {

    @Test
    void functionalInterfaceUsage() {
        // Given
        final AbstractConditionClauseStart<String> start = mock(DtoConditionClauseStart.class);
        final AbstractCbConditionClauseTerminal<String> terminal = mock(CbDtoConditionClauseTerminal.class);
        final QueryConditionBuilder<String> builder = s -> terminal;

        // When
        final AbstractCbConditionClauseTerminal<String> result = builder.apply(start);

        // Then
        assertNotNull(result);
        assertEquals(terminal, result);
    }
}
