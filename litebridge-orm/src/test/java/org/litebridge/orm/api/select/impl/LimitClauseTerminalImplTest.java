package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.model.LimitSpec;
import org.litebridge.orm.api.select.model.SelectSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LimitClauseTerminalImplTest {

    @Test
    void offset() {
        // Given
        final AbstractSelector<String, SelectSpec> delegate = mock(AbstractSelector.class);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final LimitSpec limitSpec = new LimitSpec();
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(selectSpec.ensureLimit()).thenReturn(limitSpec);
        final LimitClauseTerminalImpl<String, SelectSpec> terminal = new LimitClauseTerminalImpl<>(delegate);

        // When
        terminal.offset(20);

        // Then
        assertEquals(20, limitSpec.getOffset().orElseThrow());
    }
}
