package org.litebridgedb.orm.api.select.impl;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.api.select.model.SelectSpec;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DelegatingSelectorTest {

    @Test
    void delegation() {
        // Given
        final AbstractSelector<String, SelectSpec> delegate = mock(AbstractSelector.class);
        final DelegatingSelector<String, SelectSpec> selector = new DelegatingSelector<>(delegate);

        when(delegate.one()).thenReturn(Optional.of("result"));
        when(delegate.list()).thenReturn(List.of("result"));
        when(delegate.toSql()).thenReturn("SELECT *");

        // When / Then
        assertTrue(selector.one().isPresent());
        assertEquals("result", selector.one().get());
        assertEquals(List.of("result"), selector.list());
        assertEquals("SELECT *", selector.toSql());

        selector.oneOrNull();
        verify(delegate).oneOrNull();

        selector.oneOrThrow();
        verify(delegate).oneOrThrow();

        selector.first();
        verify(delegate).first();

        selector.firstOrNull();
        verify(delegate).firstOrNull();

        selector.firstOrThrow();
        verify(delegate).firstOrThrow();

        selector.stream();
        verify(delegate).stream();
    }
}
