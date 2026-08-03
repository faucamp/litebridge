//package org.litebridge.db.spi.query;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.expression.ColumnExpressionTest;
//import org.litebridge.db.spi.expression.LiteralExpression;
//
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class ConditionGroupTest {
//
//    @Test
//    void isEmpty() {
//        assertTrue(new ConditionGroup(Collections.emptyList()).isEmpty());
//
//        final LogicCondition condition = new LogicCondition(
//                ColumnExpressionTest.select(new Column(new Table("T"), "C")),
//                Operator.EQ,
//                new LiteralExpression(1)
//        );
//        assertFalse(new ConditionGroup(List.of(condition)).isEmpty());
//        assertFalse(new ConditionGroup(condition).isEmpty());
//    }
//
//    @Test
//    void logicConditionGroup() {
//        final ConditionGroup innerGroup = new ConditionGroup(Collections.emptyList());
//        final LogicConditionGroup group = new LogicConditionGroup(LogicOperator.AND, innerGroup);
//        assertEquals(LogicOperator.AND, group.logicOperator());
//        assertEquals(innerGroup, group.conditionGroup());
//    }
//}
