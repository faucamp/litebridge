package org.litebridge.orm.api.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoWhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DtoWhereCriteriaBuilderTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;
    private DtoFromClauseTerminal<TestDto> fromTerminal;
    private DtoWhereCriteriaBuilder<TestDto> criteriaBuilder;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, TestDto.class, null, null, null, null);
        fromTerminal = new DtoFromClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
        criteriaBuilder = new DtoWhereCriteriaBuilder<>(fromTerminal);
    }

    @Test
    void build_whenNoConditions_returnsOriginalSelectTerminal() {
        // When
        final OrderByClauseTerminal<TestDto> result = criteriaBuilder.build();

        // Then
        assertSame(fromTerminal, result);
    }

    @Test
    void getLogicOperator_initialState_returnsNoop() {
        // When & Then
        assertEquals(LogicOperator.NOOP, criteriaBuilder.getLogicOperator());
    }

    @Test
    void add_singleCondition_usesNoopOperator() {
        // When
        final DtoWhereCriteriaBuilder<TestDto> builder = criteriaBuilder.add(q -> q.where("name").eq("Alice"));
        final OrderByClauseTerminal<TestDto> result = builder.build();

        // Then
        assertSame(criteriaBuilder, builder);
        final DtoWhereConditionClauseTerminal<TestDto> terminal = assertInstanceOf(DtoWhereConditionClauseTerminal.class, result);
        final QueryNode node = SelectTerminalInspector.getNode(terminal);
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, node);
        assertSame(selectNode, whereNode.previous());

        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(LogicOperator.NOOP, conditionNode.logicOperator());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Alice", conditionNode.rhs());
    }

    @Test
    void add_multipleConditions_defaultsToAndOperator() {
        // When
        criteriaBuilder.add(q -> q.where("name").eq("Alice"))
                .add(q -> q.where("age").gt(30));
        final OrderByClauseTerminal<TestDto> result = criteriaBuilder.build();

        // Then
        final DtoWhereConditionClauseTerminal<TestDto> terminal = assertInstanceOf(DtoWhereConditionClauseTerminal.class, result);
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, SelectTerminalInspector.getNode(terminal));

        final ConditionNode secondCondition = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(LogicOperator.AND, secondCondition.logicOperator());
        assertEquals("age", secondCondition.lhsColumn());
        assertEquals(Operator.GT, secondCondition.operator());
        assertEquals(30, secondCondition.rhs());

        final ConditionNode firstCondition = assertInstanceOf(ConditionNode.class, secondCondition.previous());
        assertEquals(LogicOperator.NOOP, firstCondition.logicOperator());
        assertEquals("name", firstCondition.lhsColumn());
        assertEquals(Operator.EQ, firstCondition.operator());
        assertEquals("Alice", firstCondition.rhs());
    }

    @Test
    void add_withExplicitLogicOperator() {
        // When
        criteriaBuilder.add(q -> q.where("name").eq("Alice"))
                .add(LogicOperator.OR, q -> q.where("name").eq("Bob"));
        final OrderByClauseTerminal<TestDto> result = criteriaBuilder.build();

        // Then
        final DtoWhereConditionClauseTerminal<TestDto> terminal = assertInstanceOf(DtoWhereConditionClauseTerminal.class, result);
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, SelectTerminalInspector.getNode(terminal));

        final ConditionNode secondCondition = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(LogicOperator.OR, secondCondition.logicOperator());
        assertEquals("name", secondCondition.lhsColumn());
        assertEquals(Operator.EQ, secondCondition.operator());
        assertEquals("Bob", secondCondition.rhs());
    }

    @Test
    void and_convenienceMethod() {
        // When: on empty builder
        final DtoWhereCriteriaBuilder<TestDto> emptyBuilder = new DtoWhereCriteriaBuilder<>(fromTerminal);
        emptyBuilder.and(q -> q.where("status").eq("ACTIVE"));
        final ConditionNode firstNode = assertInstanceOf(ConditionNode.class,
                ((WhereNode) SelectTerminalInspector.getNode(emptyBuilder.build())).condition());
        assertEquals(LogicOperator.NOOP, firstNode.logicOperator());

        // When: on builder with existing condition
        criteriaBuilder.add(q -> q.where("name").eq("Alice"))
                .and(q -> q.where("age").gte(18));
        final OrderByClauseTerminal<TestDto> result = criteriaBuilder.build();

        // Then
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, SelectTerminalInspector.getNode(result));
        final ConditionNode secondCondition = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(LogicOperator.AND, secondCondition.logicOperator());
        assertEquals("age", secondCondition.lhsColumn());
        assertEquals(Operator.GTE, secondCondition.operator());
        assertEquals(18, secondCondition.rhs());
    }

    @Test
    void or_convenienceMethod() {
        // When: on empty builder
        final DtoWhereCriteriaBuilder<TestDto> emptyBuilder = new DtoWhereCriteriaBuilder<>(fromTerminal);
        emptyBuilder.or(q -> q.where("status").eq("ACTIVE"));
        final ConditionNode firstNode = assertInstanceOf(ConditionNode.class,
                ((WhereNode) SelectTerminalInspector.getNode(emptyBuilder.build())).condition());
        assertEquals(LogicOperator.NOOP, firstNode.logicOperator());

        // When: on builder with existing condition
        criteriaBuilder.add(q -> q.where("name").eq("Alice"))
                .or(q -> q.where("name").eq("Bob"));
        final OrderByClauseTerminal<TestDto> result = criteriaBuilder.build();

        // Then
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, SelectTerminalInspector.getNode(result));
        final ConditionNode secondCondition = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(LogicOperator.OR, secondCondition.logicOperator());
        assertEquals("name", secondCondition.lhsColumn());
        assertEquals(Operator.EQ, secondCondition.operator());
        assertEquals("Bob", secondCondition.rhs());
    }

    @Test
    void logicOperator_stateTransitions() {
        // Given
        criteriaBuilder.add(q -> q.where("name").eq("Alice"));

        // Default when node != null
        assertEquals(LogicOperator.AND, criteriaBuilder.getLogicOperator());

        // Explicit set
        criteriaBuilder.setLogicOperator(LogicOperator.OR);
        assertEquals(LogicOperator.OR, criteriaBuilder.getLogicOperator());

        // Adding condition uses updated default operator
        criteriaBuilder.add(q -> q.where("age").lt(20));
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, SelectTerminalInspector.getNode(criteriaBuilder.build()));
        final ConditionNode secondCondition = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(LogicOperator.OR, secondCondition.logicOperator());

        // Reset to null falls back to AND
        criteriaBuilder.setLogicOperator(null);
        assertEquals(LogicOperator.AND, criteriaBuilder.getLogicOperator());
    }

    private static final class TestDto {
        private String name;
        private int age;
        private String status;
    }
}
