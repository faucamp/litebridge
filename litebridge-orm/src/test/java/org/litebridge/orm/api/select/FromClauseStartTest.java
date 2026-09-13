package org.litebridge.orm.api.select;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.api.select.sql.SqlFromClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FromClauseStartTest {

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
    void from_dtoClass_withExpressionSpecsConstructor() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.count()};
        final FromClauseStart fromClauseStart = new FromClauseStart(expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<TestDto> terminal = fromClauseStart.from(TestDto.class);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, null, expressionSpecs, null);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClass_withColumnsConstructor() {
        // Given
        final String[] columns = new String[]{"col1", "col2"};
        final FromClauseStart fromClauseStart = new FromClauseStart(columns, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<TestDto> terminal = fromClauseStart.from(TestDto.class);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, columns, null, null);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClass_withSelectAllConstructor() {
        // Given
        final FromClauseStart fromClauseStart = new FromClauseStart(selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<TestDto> terminal = fromClauseStart.from(TestDto.class);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        final SelectNode expectedNode = new SelectNode(null, TestDto.class, null, null, null, null);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_dtoClassAndContextDtoClass() {
        // Given
        final String[] columns = new String[]{"col1"};
        final FromClauseStart fromClauseStart = new FromClauseStart(columns, selectEngineTerminal, litebridgeContextCreator);

        // When
        final DtoFromClauseTerminal<TestDto> terminal = fromClauseStart.from(TestDto.class, ContextDto.class);

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.DTO);
        final SelectNode expectedNode = new SelectNode(null, TestDto.class, ContextDto.class, columns, null, null);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_table_withNullExpressionSpecs() {
        // Given
        final String[] columns = new String[]{"col1", "col2"};
        final FromClauseStart fromClauseStart = new FromClauseStart(columns, selectEngineTerminal, litebridgeContextCreator);

        // When
        final SqlFromClauseTerminal terminal = fromClauseStart.from("users");

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.SQL);
        final SelectNode expectedNode = new SelectNode("users", null, null, columns, null, null);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_table_withExpressionSpecs_withoutTypeOverrides() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("col1"), Fn.column("col2")};
        final FromClauseStart fromClauseStart = new FromClauseStart(expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final SqlFromClauseTerminal terminal = fromClauseStart.from("users");

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.SQL);
        final SelectNode expectedNode = new SelectNode("users", null, null, null, expressionSpecs, null);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_table_withExpressionSpecs_withMultipleTypeOverrides() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.count(), Fn.count()};
        final FromClauseStart fromClauseStart = new FromClauseStart(expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final SqlFromClauseTerminal terminal = fromClauseStart.from("users");

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.SQL);
        final Class<?>[] expectedResultTypes = new Class<?>[]{Long.class, Long.class};
        final SelectNode expectedNode = new SelectNode("users", null, null, null, expressionSpecs, expectedResultTypes);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    @Test
    void from_table_withExpressionSpecs_withMixedTypeOverrides() {
        // Given
        final ExpressionSpec[] expressionSpecs = new ExpressionSpec[]{Fn.column("col1"), Fn.count(), Fn.column("col2")};
        final FromClauseStart fromClauseStart = new FromClauseStart(expressionSpecs, selectEngineTerminal, litebridgeContextCreator);

        // When
        final SqlFromClauseTerminal terminal = fromClauseStart.from("users");

        // Then
        assertNotNull(terminal);
        verify(litebridgeContextCreator).apply(LitebridgeContext.Mode.SQL);
        final Class<?>[] expectedResultTypes = new Class<?>[]{null, Long.class, null};
        final SelectNode expectedNode = new SelectNode("users", null, null, null, expressionSpecs, expectedResultTypes);
        assertEquals(expectedNode, SelectTerminalInspector.getNode(terminal));
    }

    private static class TestDto {
    }

    private static class ContextDto {
    }
}
