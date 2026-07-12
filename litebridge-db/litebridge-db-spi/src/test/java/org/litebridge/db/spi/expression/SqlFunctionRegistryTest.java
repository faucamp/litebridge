package org.litebridge.db.spi.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SqlFunctionRegistryTest {

    @Test
    void record_contracts() {
        final SqlFunctionRegistry.Select select = new SqlFunctionRegistry.Select(
                mock(ColumnExpressionFactory.class),
                mock(SubselectExpressionFactory.class),
                mock(LiteralExpressionFactory.class),
                mock(SelectReferenceExpressionFactory.class)
        );
        final SqlFunctionRegistry.Aggregate aggregate = new SqlFunctionRegistry.Aggregate(
                mock(DelegateExpressionFactory.class),
                mock(DelegateExpressionFactory.class),
                mock(DelegateExpressionFactory.class),
                mock(SelectExpression.class)
        );
        final SqlFunctionRegistry.Scalar scalar = new SqlFunctionRegistry.Scalar(
                mock(DelegateExpressionFactory.class),
                mock(DelegateExpressionFactory.class),
                mock(DelegateExpressionFactory.class),
                mock(DelegateExpressionFactory.class)
        );
        final SqlFunctionRegistry.Date date = new SqlFunctionRegistry.Date(
                mock(SelectExpression.class)
        );

        final SqlFunctionRegistry registry = new SqlFunctionRegistry(select, aggregate, scalar, date);

        assertEquals(select, registry.select());
        assertEquals(aggregate, registry.aggregate());
        assertEquals(scalar, registry.scalar());
        assertEquals(date, registry.date());

        assertNotNull(registry.toString());
        assertEquals(registry, new SqlFunctionRegistry(select, aggregate, scalar, date));
        assertEquals(registry.hashCode(), new SqlFunctionRegistry(select, aggregate, scalar, date).hashCode());
    }

    @Test
    void nested_records_toString() {
        final SqlFunctionRegistry.Select select = new SqlFunctionRegistry.Select(null, null, null, null);
        assertNotNull(select.toString());

        final SqlFunctionRegistry.Aggregate aggregate = new SqlFunctionRegistry.Aggregate(null, null, null, null);
        assertNotNull(aggregate.toString());

        final SqlFunctionRegistry.Scalar scalar = new SqlFunctionRegistry.Scalar(null, null, null, null);
        assertNotNull(scalar.toString());

        final SqlFunctionRegistry.Date date = new SqlFunctionRegistry.Date(null);
        assertNotNull(date.toString());
    }
}
