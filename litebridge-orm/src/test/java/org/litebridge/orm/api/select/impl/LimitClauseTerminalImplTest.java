//package org.litebridge.orm.api.select.impl;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.orm.api.select.ast.LimitNode;
//import org.litebridge.orm.api.select.model.SelectSpec;
//
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//class LimitClauseTerminalImplTest {
//
//    @Test
//    void offset() {
//        // Given
//        final AbstractSelector<String, SelectSpec> delegate = mock(AbstractSelector.class);
//        when(delegate.selectSpec()).thenReturn(mock(SelectSpec.class));
//        when(delegate.withNode(any())).thenReturn(delegate);
//        final LimitClauseTerminalImpl<String, SelectSpec> terminal = new LimitClauseTerminalImpl<>(delegate);
//
//        // When
//        terminal.offset(20);
//
//        // Then
//        verify(delegate).withNode(argThat(node ->
//                node instanceof LimitNode limitNode
//                && limitNode.offset().equals(Optional.of(20))));
//    }
//}
