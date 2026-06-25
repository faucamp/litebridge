package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;
import org.litebridgedb.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridgedb.orm.expression.function.scalar.AbsSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.intent.ConvertIntent;
import org.litebridgedb.orm.expression.intent.ConvertSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class FnTest {

    @Test
    void testFieldAndColumn() {
        final ExpressionSpec f = Fn.f("field");
        assertInstanceOf(ProtoColumnExpressionSpec.class, f);
        assertEquals(SelectFieldSpec.class, ((ProtoColumnExpressionSpec) f).type());

        final ExpressionSpec field = Fn.field("field");
        assertInstanceOf(ProtoColumnExpressionSpec.class, field);

        final ExpressionSpec c = Fn.c("COL");
        assertInstanceOf(ProtoColumnExpressionSpec.class, c);
        assertEquals(SelectColumnSpec.class, ((ProtoColumnExpressionSpec) c).type());

        final ExpressionSpec column = Fn.column("COL");
        assertInstanceOf(ProtoColumnExpressionSpec.class, column);

        final ExpressionSpec cTableName = Fn.c("TABLE", "COL");
        assertInstanceOf(SelectColumnSpec.class, cTableName);
        assertEquals("TABLE", ((SelectColumnSpec) cTableName).column().table().name());
        assertEquals("COL", ((SelectColumnSpec) cTableName).column().name());

        Table table = new Table("TABLE");
        final ExpressionSpec cTable = Fn.c(table, "COL");
        assertInstanceOf(SelectColumnSpec.class, cTable);
        assertEquals(table, ((SelectColumnSpec) cTableName).column().table());
        assertEquals("COL", ((SelectColumnSpec) cTableName).column().name());

        assertInstanceOf(SelectColumnSpec.class, Fn.column("TABLE", "COL"));
        assertInstanceOf(SelectColumnSpec.class, Fn.column(table, "COL"));
    }

    @Test
    void testColumnAlias() {
        final Table table = new Table("TABLE");
        final ExpressionSpec caTable = Fn.ca(table, "COL", "alias");
        assertInstanceOf(SelectColumnSpec.class, caTable);
        assertEquals(table, ((SelectColumnSpec) caTable).column().table());
        assertEquals("COL", ((SelectColumnSpec) caTable).column().name());
        assertEquals("alias", ((SelectColumnSpec) caTable).column().alias());

        final ExpressionSpec ca = Fn.ca("COL", "alias");
        assertInstanceOf(ProtoColumnExpressionSpec.class, ca);
        assertEquals(SelectColumnSpec.class, ((ProtoColumnExpressionSpec) ca).type());

        final ExpressionSpec caTableName = Fn.ca("TABLE", "COL", "alias");
        assertInstanceOf(SelectColumnSpec.class, caTableName);
        assertEquals("TABLE", ((SelectColumnSpec) caTableName).column().table().name());
        assertEquals("COL", ((SelectColumnSpec) caTableName).column().name());
        assertEquals("alias", ((SelectColumnSpec) caTableName).column().alias());

        assertInstanceOf(SelectColumnSpec.class, Fn.columnAlias(table, "COL", "alias"));
        assertInstanceOf(ProtoColumnExpressionSpec.class, Fn.columnAlias("COL", "alias"));
        assertInstanceOf(SelectColumnSpec.class, Fn.columnAlias("TABLE", "COL", "alias"));
    }

    @Test
    void testConvert() {
        final ExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final ConvertSpec<Integer> intent = Fn.convert(target, Integer.class);
        assertEquals(target, intent.target());
        assertEquals(Integer.class, intent.returnType());
    }

    @Test
    void testAggregates() {
        final TypeOverrideExpressionSpec<Number> avg = Fn.avg("COL");
        assertInstanceOf(ProtoNestableTOExpr.class, avg);
        assertEquals(AvgSpec.class, ((ProtoNestableTOExpr<?>) avg).type());

        final TypeOverrideExpressionSpec<Number> avgSpec = Fn.avg(new SelectColumnSpec(mock(Column.class)));
        assertInstanceOf(ProtoNestableTOExpr.class, avg);
        assertEquals(AvgSpec.class, ((ProtoNestableTOExpr<?>) avg).type());

        final TypeOverrideExpressionSpec<Number> max = Fn.max("COL");
        assertInstanceOf(ProtoNestableTOExpr.class, max);
        assertEquals(MaxSpec.class, ((ProtoNestableTOExpr<?>) max).type());

        final TypeOverrideExpressionSpec<Number> maxSpec = Fn.max(new SelectColumnSpec(mock(Column.class)));
        assertInstanceOf(ProtoNestableTOExpr.class, maxSpec);
        assertEquals(MaxSpec.class, ((ProtoNestableTOExpr<?>) maxSpec).type());

        final TypeOverrideExpressionSpec<Number> min = Fn.min("COL");
        assertInstanceOf(ProtoNestableTOExpr.class, min);
        assertEquals(MinSpec.class, ((ProtoNestableTOExpr<?>) min).type());

        final TypeOverrideExpressionSpec<Number> minSpec = Fn.min(new SelectColumnSpec(mock(Column.class)));
        assertInstanceOf(ProtoNestableTOExpr.class, minSpec);
        assertEquals(MinSpec.class, ((ProtoNestableTOExpr<?>) minSpec).type());

        final TypeOverrideExpressionSpec<Long> count = Fn.count();
        assertInstanceOf(CountSpec.class, count);
    }

    @Test
    void testScalars() {
        final ProtoNestableTOExpr<String> upper = Fn.upper("COL");
        assertEquals(UpperSpec.class, upper.type());

        final ProtoNestableTOExpr<String> upperSpec = Fn.upper(new SelectColumnSpec(mock(Column.class)));
        assertEquals(UpperSpec.class, upper.type());

        final ProtoNestableTOExpr<String> lower = Fn.lower("COL");
        assertEquals(LowerSpec.class, lower.type());

        final ProtoNestableTOExpr<String> lowerSpec = Fn.lower(new SelectColumnSpec(mock(Column.class)));
        assertEquals(LowerSpec.class, lowerSpec.type());

        final ProtoNestableTOExpr<Number> abs = Fn.abs("COL");
        assertEquals(AbsSpec.class, abs.type());

        final ProtoNestableTOExpr<Number> absSpec = Fn.abs(new SelectColumnSpec(mock(Column.class)));
        assertEquals(AbsSpec.class, abs.type());
    }

    @Test
    void testSubstring() {
        final ProtoNestableTOExpr<String> sub1 = Fn.substring("COL", 1);
        assertEquals(SubstringSpec.class, sub1.type());
        assertArrayEquals(new @Nullable Object[]{1, null}, sub1.args());

        final ProtoNestableTOExpr<String> sub1Spec = Fn.substring(new SelectColumnSpec(mock(Column.class)), 1);
        assertEquals(SubstringSpec.class, sub1Spec.type());
        assertArrayEquals(new @Nullable Object[]{1, null}, sub1Spec.args());

        final ProtoNestableTOExpr<String> sub2 = Fn.substring("COL", 1, 10);
        assertEquals(SubstringSpec.class, sub2.type());
        assertArrayEquals(new Object[]{1, 10}, sub2.args());

        final ProtoNestableTOExpr<String> sub2Spec = Fn.substring(new SelectColumnSpec(null), 1, 10);
        assertEquals(SubstringSpec.class, sub2Spec.type());
        assertArrayEquals(new Object[]{1, 10}, sub2Spec.args());
    }

    @Test
    void testCurrentTimestamp() {
        assertInstanceOf(CurrentTimestampSpec.class, Fn.currentTimestamp());
    }
}
