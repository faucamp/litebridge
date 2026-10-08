package org.litebridge.db.spi.impl.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.expression.AliasReference;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.impl.expression.function.aggregate.Avg;
import org.litebridge.db.spi.impl.expression.function.aggregate.Count;
import org.litebridge.db.spi.impl.expression.function.aggregate.Max;
import org.litebridge.db.spi.impl.expression.function.aggregate.Min;
import org.litebridge.db.spi.impl.expression.function.date.CurrentTimestamp;
import org.litebridge.db.spi.impl.expression.function.scalar.Abs;
import org.litebridge.db.spi.impl.expression.function.scalar.Lower;
import org.litebridge.db.spi.impl.expression.function.scalar.Substring;
import org.litebridge.db.spi.impl.expression.function.scalar.Upper;
import org.litebridge.db.spi.impl.function.Subselect;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Select;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SqlFunctionRegistryFactoryTest {

    @Test
    void create_registersAllFunctionsAndExpressions() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final SelectSqlGenerator selectSqlGenerator = mock(SelectSqlGenerator.class);
        final SqlFunctionRegistryFactory factory = new SqlFunctionRegistryFactory(labelGenerator, selectSqlGenerator);

        // When
        final SqlFunctionRegistry registry = factory.create();

        // Then
        assertNotNull(registry);

        // Select functions
        final SelectColumn selectCol = (SelectColumn) registry.select().column().create(new Column("id"), "alias", "tableAlias");
        assertNotNull(selectCol);

        final SubselectExpression subselectExpr = registry.select().subselect().create(mock(Select.class));
        assertInstanceOf(Subselect.class, subselectExpr);

        final LiteralExpression literalExpr = registry.select().literal().create("value", "alias");
        assertInstanceOf(LiteralExpressionImpl.class, literalExpr);

        final BindValueExpression bindValueExpr = registry.select().bindValue().create(1, 1, new ColumnType(Types.INTEGER, null), "alias");
        assertInstanceOf(BindValueExpressionImpl.class, bindValueExpr);

        final AliasReference aliasRef = registry.select().aliasReference().create("alias", "tableAlias");
        assertInstanceOf(AliasReferenceImpl.class, aliasRef);

        // Aggregate functions
        final SelectExpression target = mock(SelectExpression.class);
        final DelegateExpression avgExpr = registry.aggregate().avg().create(target, "avg_alias");
        assertInstanceOf(Avg.class, avgExpr);

        final DelegateExpression minExpr = registry.aggregate().min().create(target, "min_alias");
        assertInstanceOf(Min.class, minExpr);

        final DelegateExpression maxExpr = registry.aggregate().max().create(target, "max_alias");
        assertInstanceOf(Max.class, maxExpr);

        final SelectExpression countExpr = registry.aggregate().count();
        assertInstanceOf(Count.class, countExpr);

        // Scalar functions
        final DelegateExpression upperExpr = registry.scalar().upper().create(target, "upper_alias");
        assertInstanceOf(Upper.class, upperExpr);

        final DelegateExpression lowerExpr = registry.scalar().lower().create(target, "lower_alias");
        assertInstanceOf(Lower.class, lowerExpr);

        final DelegateExpression substrExpr = registry.scalar().substring().create(target, 1, 5, "sub_alias");
        assertInstanceOf(Substring.class, substrExpr);

        final DelegateExpression absExpr = registry.scalar().abs().create(target, "abs_alias");
        assertInstanceOf(Abs.class, absExpr);

        // Cast function
        final DelegateExpression castExpr = registry.cast().create(target, "cast_alias", Types.VARCHAR, 100);
        assertInstanceOf(Cast.class, castExpr);

        // Date functions
        final SelectExpression currentTimestampExpr = registry.date().currentTimestamp();
        assertInstanceOf(CurrentTimestamp.class, currentTimestampExpr);
    }

    @Test
    void createSelectReference_throwsUnsupportedOperationException() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final SelectSqlGenerator selectSqlGenerator = mock(SelectSqlGenerator.class);
        final SqlFunctionRegistryFactory factory = new SqlFunctionRegistryFactory(labelGenerator, selectSqlGenerator);

        // When / Then
        assertThrows(UnsupportedOperationException.class,
                () -> factory.createSelectReference(new Column("id"), "alias", "tableAlias"));
    }
}
