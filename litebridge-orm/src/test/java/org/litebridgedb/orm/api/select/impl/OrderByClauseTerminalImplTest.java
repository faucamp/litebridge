package org.litebridgedb.orm.api.select.impl;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.api.select.model.LimitSpec;
import org.litebridgedb.orm.api.select.model.SelectSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderByClauseTerminalImplTest {

    @Test
    void limit() {
        // Given
        final AbstractSelector<String, SelectSpec> delegate = mock(AbstractSelector.class);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final LimitSpec limitSpec = new LimitSpec();
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(selectSpec.ensureLimit()).thenReturn(limitSpec);
        final OrderByClauseTerminalImpl<String, SelectSpec> terminal = new OrderByClauseTerminalImpl<>(delegate);

        // When
        terminal.limit(10);

        // Then
        assertEquals(10, limitSpec.getLimit().orElseThrow());
    }
}
