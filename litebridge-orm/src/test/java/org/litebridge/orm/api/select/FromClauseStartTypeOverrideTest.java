package org.litebridge.orm.api.select;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.api.select.sql.SqlFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FromClauseStartTypeOverrideTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    @SuppressWarnings("unchecked")
    private Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator = mock(Function.class);

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        when(litebridgeContextCreator.apply(LitebridgeContext.Mode.DTO)).thenReturn(litebridgeContext);
        when(litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL)).thenReturn(litebridgeContext);
    }

    @Test
    void from_dtoClass_delegatesToFromWithNullStrategy() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("id")};
        final FromClauseStartTypeOverride<String> fromClauseStart =
                new FromClauseStartTypeOverride<>(String.class, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<String> terminal = fromClauseStart.from(TestDto.class);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        verify(litebridgeContext, never()).setRelatedDtoStrategy(any());

        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, null, expressionSpecs, new Class<?>[]{String.class});
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClassAndContextDtoClass() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("id")};
        final FromClauseStartTypeOverride<String> fromClauseStart =
                new FromClauseStartTypeOverride<>(String.class, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<String> terminal = fromClauseStart.from(TestDto.class, ContextDto.class);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);

        final SelectNode expectedNode = new SelectNode(null, TestDto.class, ContextDto.class, null, expressionSpecs, new Class<?>[]{String.class});
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClassAndRelatedDtoStrategy_withTypeOverrideExpressions() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.count()};
        final FromClauseStartTypeOverride<Integer> fromClauseStart =
                new FromClauseStartTypeOverride<>(Integer.class, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<Integer> terminal = fromClauseStart.from(TestDto.class, RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        verify(litebridgeContext).setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

        // Fn.count() returns Long.class, so expressionReturnTypes should override typeOverride
        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, null, expressionSpecs, new Class<?>[]{Long.class});
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClassAndRelatedDtoStrategy_withoutTypeOverrideExpressions_andNullStrategy() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("name")};
        final FromClauseStartTypeOverride<String> fromClauseStart =
                new FromClauseStartTypeOverride<>(String.class, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<String> terminal = fromClauseStart.from(TestDto.class, (RelatedDtoStrategy) null);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        verify(litebridgeContext, never()).setRelatedDtoStrategy(any());

        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, null, expressionSpecs, new Class<?>[]{String.class});
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClassAndRelatedDtoStrategy_withoutTypeOverrideExpressions_andNonNullStrategy() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("name")};
        final FromClauseStartTypeOverride<String> fromClauseStart =
                new FromClauseStartTypeOverride<>(String.class, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<String> terminal = fromClauseStart.from(TestDto.class, RelatedDtoStrategy.NULL_IF_NO_JOIN);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        verify(litebridgeContext).setRelatedDtoStrategy(RelatedDtoStrategy.NULL_IF_NO_JOIN);

        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, null, expressionSpecs, new Class<?>[]{String.class});
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_table() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("id")};
        final FromClauseStartTypeOverride<String> fromClauseStart =
                new FromClauseStartTypeOverride<>(String.class, expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final SqlFromClauseTerminal terminal = fromClauseStart.from("users");

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.SQL);

        final SelectNode expectedNode = new SelectNode("users", null, null, null, expressionSpecs, new Class<?>[]{String.class});
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    private static class TestDto {
    }

    private static class ContextDto {
    }
}
