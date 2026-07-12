package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;
import org.litebridge.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridge.orm.expression.function.scalar.AbsSpec;
import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.intent.ConvertIntent;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;

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
        assertEquals("TABLE", ((SelectColumnSpec) cTableName).getColumn().table().name());
        assertEquals("COL", ((SelectColumnSpec) cTableName).getColumn().name());

        Table table = new Table("TABLE");
        final ExpressionSpec cTable = Fn.c(table, "COL");
        assertInstanceOf(SelectColumnSpec.class, cTable);
        assertEquals(table, ((SelectColumnSpec) cTableName).getColumn().table());
        assertEquals("COL", ((SelectColumnSpec) cTableName).getColumn().name());

        assertInstanceOf(SelectColumnSpec.class, Fn.column("TABLE", "COL"));
        assertInstanceOf(SelectColumnSpec.class, Fn.column(table, "COL"));
    }

    @Test
    void testFieldAndColumnDto() {
        final ExpressionSpec f = Fn.f(Object.class, "field");
        assertInstanceOf(ProtoColumnExpressionSpec.class, f);
        assertEquals(SelectFieldSpec.class, ((ProtoColumnExpressionSpec) f).type());
        assertArrayEquals(new Object[]{Object.class}, ((ProtoColumnExpressionSpec) f).args());

        final ExpressionSpec field = Fn.field(Object.class, "field");
        assertInstanceOf(ProtoColumnExpressionSpec.class, field);
        assertArrayEquals(new Object[]{Object.class}, ((ProtoColumnExpressionSpec) field).args());
    }

    @Test
    void testColumnAlias() {
        final Table table = new Table("TABLE");
        final ExpressionSpec caTable = Fn.ca(table, "COL", "alias");
        assertInstanceOf(SelectColumnSpec.class, caTable);
        assertEquals(table, ((SelectColumnSpec) caTable).getColumn().table());
        assertEquals("COL", ((SelectColumnSpec) caTable).getColumn().name());
        assertEquals("alias", ((SelectColumnSpec) caTable).getColumn().alias());

        final ExpressionSpec ca = Fn.ca("COL", "alias");
        assertInstanceOf(ProtoColumnExpressionSpec.class, ca);
        assertEquals(SelectColumnSpec.class, ((ProtoColumnExpressionSpec) ca).type());

        final ExpressionSpec caTableName = Fn.ca("TABLE", "COL", "alias");
        assertInstanceOf(SelectColumnSpec.class, caTableName);
        assertEquals("TABLE", ((SelectColumnSpec) caTableName).getColumn().table().name());
        assertEquals("COL", ((SelectColumnSpec) caTableName).getColumn().name());
        assertEquals("alias", ((SelectColumnSpec) caTableName).getColumn().alias());

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
    void testConvertIntent() {
        final ExpressionSpec e1 = new SelectColumnSpec(mock(Column.class));
        final ExpressionSpec e2 = new SelectColumnSpec(mock(Column.class));
        final ConvertIntent<Integer> intent = Fn.convert(Integer.class, e1, e2);
        assertEquals(Integer.class, intent.returnType());
        assertArrayEquals(new ExpressionSpec[]{e1, e2}, intent.target());

        final ConvertIntent<org.litebridge.db.spi.Row> rowIntent = Fn.row(e1, e2);
        assertEquals(org.litebridge.db.spi.Row.class, rowIntent.returnType());
        assertArrayEquals(new ExpressionSpec[]{e1, e2}, rowIntent.target());
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
