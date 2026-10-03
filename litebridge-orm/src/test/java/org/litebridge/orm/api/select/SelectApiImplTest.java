package org.litebridge.orm.api.select;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.TypeOverride;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelectApiImplTest {

    private SelectEngine selectEngine;
    private LitebridgeContext litebridgeContext;
    private SelectApiImpl selectApi;

    private static class TestDto {
    }

    private static class ContextDto {
    }

    @BeforeEach
    void setUp() {
        selectEngine = mock(SelectEngine.class);
        litebridgeContext = mock(LitebridgeContext.class);
        when(litebridgeContext.selectEngine()).thenReturn(selectEngine);

        selectApi = new SelectApiImpl(litebridgeContext);
    }

    @Test
    void select_dtoClass() {
        // Given
        @SuppressWarnings("unchecked") final DtoFromClauseTerminal<TestDto> expectedTerminal = mock(DtoFromClauseTerminal.class);
        when(selectEngine.select(TestDto.class, litebridgeContext)).thenReturn(expectedTerminal);

        // When
        final DtoFromClauseTerminal<TestDto> result = selectApi.select(TestDto.class);

        // Then
        assertNotNull(result);
        assertSame(expectedTerminal, result);
        verify(selectEngine).select(TestDto.class, litebridgeContext);
    }

    @Test
    void select_dtoClass_withDifferentRelatedDtoStrategy() {
        // Given
        @SuppressWarnings("unchecked") final DtoFromClauseTerminal<TestDto> expectedTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridgeContext.getRelatedDtoStrategy()).thenReturn(RelatedDtoStrategy.NULL_IF_NO_JOIN);
        when(selectEngine.select(TestDto.class, litebridgeContext)).thenReturn(expectedTerminal);

        // When
        final DtoFromClauseTerminal<TestDto> result = selectApi.select(TestDto.class, RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

        // Then
        assertNotNull(result);
        assertSame(expectedTerminal, result);

        final InOrder inOrder = inOrder(litebridgeContext, selectEngine);
        inOrder.verify(litebridgeContext).setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
        inOrder.verify(selectEngine).select(TestDto.class, litebridgeContext);
        inOrder.verify(litebridgeContext).setRelatedDtoStrategy(RelatedDtoStrategy.NULL_IF_NO_JOIN);
    }

    @Test
    void select_dtoClass_withSameRelatedDtoStrategy() {
        // Given
        @SuppressWarnings("unchecked") final DtoFromClauseTerminal<TestDto> expectedTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridgeContext.getRelatedDtoStrategy()).thenReturn(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
        when(selectEngine.select(TestDto.class, litebridgeContext)).thenReturn(expectedTerminal);

        // When
        final DtoFromClauseTerminal<TestDto> result = selectApi.select(TestDto.class, RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

        // Then
        assertNotNull(result);
        assertSame(expectedTerminal, result);

        verify(litebridgeContext, never()).setRelatedDtoStrategy(any());
        verify(selectEngine).select(TestDto.class, litebridgeContext);
    }

    @Test
    void select_dtoClass_withContextDtoClass() {
        // Given
        @SuppressWarnings("unchecked") final DtoFromClauseTerminal<TestDto> expectedTerminal = mock(DtoFromClauseTerminal.class);
        when(selectEngine.select(TestDto.class, ContextDto.class, litebridgeContext)).thenReturn(expectedTerminal);

        // When
        final DtoFromClauseTerminal<TestDto> result = selectApi.select(TestDto.class, ContextDto.class);

        // Then
        assertNotNull(result);
        assertSame(expectedTerminal, result);
        verify(selectEngine).select(TestDto.class, ContextDto.class, litebridgeContext);
    }

    @Test
    void select_fieldsOrColumns() {
        // Given
        final String[] columns = new String[]{"col1", "col2"};
        final FromClauseStart expectedFromClauseStart = mock(FromClauseStart.class);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Function<LitebridgeContext.Mode, LitebridgeContext>> captor =
                ArgumentCaptor.forClass(Function.class);
        when(selectEngine.select(eq(columns), captor.capture())).thenReturn(expectedFromClauseStart);

        // When
        final FromClauseStart result = selectApi.select(columns);

        // Then
        assertNotNull(result);
        assertSame(expectedFromClauseStart, result);

        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = captor.getValue();
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.DTO));
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.SQL));
    }

    @Test
    void select_expressionSpecs() {
        // Given
        final ExpressionSpec[] expressions = new ExpressionSpec[]{Fn.count()};
        final FromClauseStart expectedFromClauseStart = mock(FromClauseStart.class);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Function<LitebridgeContext.Mode, LitebridgeContext>> captor =
                ArgumentCaptor.forClass(Function.class);
        when(selectEngine.select(eq(expressions), captor.capture())).thenReturn(expectedFromClauseStart);

        // When
        final FromClauseStart result = selectApi.select(expressions);

        // Then
        assertNotNull(result);
        assertSame(expectedFromClauseStart, result);

        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = captor.getValue();
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.DTO));
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.SQL));
    }

    @Test
    void select_typeOverrideExpression() {
        // Given
        final TypeOverride<Long> expression = Fn.count();
        @SuppressWarnings("unchecked") final FromClauseStartTypeOverride<Long> expectedFromClauseStart = mock(FromClauseStartTypeOverride.class);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Function<LitebridgeContext.Mode, LitebridgeContext>> captor =
                ArgumentCaptor.forClass(Function.class);
        when(selectEngine.select(eq(expression), captor.capture())).thenReturn(expectedFromClauseStart);

        // When
        final FromClauseStartTypeOverride<Long> result = selectApi.select(expression);

        // Then
        assertNotNull(result);
        assertSame(expectedFromClauseStart, result);

        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = captor.getValue();
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.DTO));
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.SQL));
    }

    @Test
    void select_noArgs() {
        // Given
        final FromClauseStart expectedFromClauseStart = mock(FromClauseStart.class);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Function<LitebridgeContext.Mode, LitebridgeContext>> captor =
                ArgumentCaptor.forClass(Function.class);
        when(selectEngine.select(captor.capture())).thenReturn(expectedFromClauseStart);

        // When
        final FromClauseStart result = selectApi.select();

        // Then
        assertNotNull(result);
        assertSame(expectedFromClauseStart, result);

        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = captor.getValue();
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.DTO));
        assertSame(litebridgeContext, contextCreator.apply(LitebridgeContext.Mode.SQL));
    }
}
