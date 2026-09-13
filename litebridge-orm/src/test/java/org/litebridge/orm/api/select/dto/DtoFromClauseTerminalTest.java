package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoFromClauseTerminalTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;
    private DtoFromClauseTerminal<SelectTestDto> terminal;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
        terminal = new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
    }

    @Test
    void where_withField() {
        // Given
        final String field = "name";

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.where(field);

        // Then
        assertNotNull(clause);
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.eq("Alice");
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(WhereNode.class, node);
        final WhereNode whereNode = (WhereNode) node;
        assertSame(selectNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Alice", conditionNode.rhs());
    }

    @Test
    void where_withExpression() {
        // Given
        final ExpressionSpec expression = Fn.column("age");

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.where(expression);

        // Then
        assertNotNull(clause);
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.gt(18);
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(WhereNode.class, node);
        final WhereNode whereNode = (WhereNode) node;
        assertSame(selectNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(expression, conditionNode.lhsExpression());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void where_withQueryConditionBuilder() {
        // Given
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").gt(21);

        // When
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.where(builder);

        // Then
        assertNotNull(nextTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(WhereNode.class, node);
        final WhereNode whereNode = (WhereNode) node;
        assertSame(selectNode, whereNode.previous());
        final ConditionNode condition = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("age", condition.lhsColumn());
        assertEquals(Operator.GT, condition.operator());
        assertEquals(21, condition.rhs());
    }

    @Test
    void withId() {
        // Given
        final SelectTestDto expectedDto = new SelectTestDto(1L, "Bob", 30);
        when(selectEngineTerminal.fetchOne(any(), any())).thenReturn(Optional.of(expectedDto));

        // When
        final Optional<SelectTestDto> result = terminal.withId(1L);

        // Then
        assertEquals(Optional.of(expectedDto), result);
        verify(selectEngineTerminal).fetchOne(any(WhereNode.class), any());
    }

    @Test
    void withIdOrNull() {
        // Given
        final SelectTestDto expectedDto = new SelectTestDto(2L, "Charlie", 25);
        when(selectEngineTerminal.fetchOneOrNull(any(), any())).thenReturn(expectedDto);

        // When
        final SelectTestDto result = terminal.withIdOrNull(2L);

        // Then
        assertSame(expectedDto, result);
        verify(selectEngineTerminal).fetchOneOrNull(any(WhereNode.class), any());
    }

    @Test
    void withIdOrThrow() {
        // Given
        final SelectTestDto expectedDto = new SelectTestDto(3L, "David", 40);
        when(selectEngineTerminal.fetchOneOrThrow(any(), any())).thenReturn(expectedDto);

        // When
        final SelectTestDto result = terminal.withIdOrThrow(3L);

        // Then
        assertSame(expectedDto, result);
        verify(selectEngineTerminal).fetchOneOrThrow(any(WhereNode.class), any());
    }

    @Test
    void withIdOrThrow_withExceptionSupplier() {
        // Given
        final SelectTestDto expectedDto = new SelectTestDto(4L, "Eve", 28);
        when(selectEngineTerminal.fetchOneOrThrow(any(), any(), any())).thenReturn(expectedDto);

        // When
        final SelectTestDto result = terminal.withIdOrThrow(4L, () -> new IllegalStateException("Not found"));

        // Then
        assertSame(expectedDto, result);
        verify(selectEngineTerminal).fetchOneOrThrow(any(WhereNode.class), any(), any());
    }

    @Test
    void join() {
        // Given
        final Class<SelectTestDto> joinDtoClass = SelectTestDto.class;

        // When
        final DtoJoinClause<SelectTestDto> joinClause = terminal.join(joinDtoClass);

        // Then
        assertNotNull(joinClause);
        final DtoJoinConditionClauseTerminal<SelectTestDto> joinTerminal = joinClause.on("id");
        final QueryNode node = SelectTerminalInspector.getNode(joinTerminal);
        assertInstanceOf(JoinNode.class, node);
        final JoinNode joinNode = (JoinNode) node;
        assertSame(selectNode, joinNode.previous());
        assertEquals("INNER", joinNode.type());
        assertEquals(joinDtoClass, joinNode.dtoClass());
    }

    @Test
    void groupBy_withFields() {
        // Given
        final String[] fields = new String[]{"name", "age"};

        // When
        final DtoGroupByClauseTerminal<SelectTestDto> groupByTerminal = terminal.groupBy(fields);

        // Then
        assertNotNull(groupByTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(groupByTerminal);
        assertInstanceOf(GroupByNode.class, node);
        final GroupByNode groupByNode = (GroupByNode) node;
        assertSame(selectNode, groupByNode.previous());
        assertEquals(fields, groupByNode.columns());
    }

    @Test
    void groupBy_withExpressions() {
        // Given
        final ExpressionSpec[] expressions = new ExpressionSpec[]{Fn.column("name")};

        // When
        final DtoGroupByClauseTerminal<SelectTestDto> groupByTerminal = terminal.groupBy(expressions);

        // Then
        assertNotNull(groupByTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(groupByTerminal);
        assertInstanceOf(GroupByNode.class, node);
        final GroupByNode groupByNode = (GroupByNode) node;
        assertSame(selectNode, groupByNode.previous());
        assertEquals(expressions, groupByNode.expressions());
    }

    @Test
    void orderBy_withFields() {
        // Given
        final String[] fields = new String[]{"name"};

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy(fields);

        // Then
        assertNotNull(orderByClause);
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.asc();
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        assertInstanceOf(OrderByNode.class, node);
        final OrderByNode orderByNode = (OrderByNode) node;
        assertSame(selectNode, orderByNode.previous());
        assertEquals("name", orderByNode.column());
        assertEquals(true, orderByNode.ascending());
    }

    @Test
    void orderBy_withExpressions() {
        // Given
        final ExpressionSpec[] expressions = new ExpressionSpec[]{Fn.column("age")};

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy(expressions);

        // Then
        assertNotNull(orderByClause);
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.desc();
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        assertInstanceOf(OrderByNode.class, node);
        final OrderByNode orderByNode = (OrderByNode) node;
        assertSame(selectNode, orderByNode.previous());
        assertEquals(expressions[0], orderByNode.expression());
        assertEquals(false, orderByNode.ascending());
    }
}
