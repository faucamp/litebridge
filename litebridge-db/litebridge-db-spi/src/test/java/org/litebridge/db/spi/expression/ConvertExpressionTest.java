//package org.litebridge.db.spi.expression;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Operation;
//
//import org.litebridge.db.spi.query.Select;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.isNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class ConvertExpressionTest {
//
//    @Test
//    void toSql() {
//        final Select operation = mock(Select.class);
//        final SelectExpression delegate = mock(SelectExpression.class);
//        final ConvertExpression expression = new ConvertExpression(delegate, String.class);
//
//        when(delegate.toSql(any(), any(), isNull())).thenReturn("COL1");
//
//        assertEquals("COL1", expression.toSql(operation, ClauseType.SELECT, null));
//        assertEquals(String.class, expression.typeOverride());
//        assertEquals(delegate, expression.target());
//    }
//}
