package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.ColumnExpression;
import org.litebridgedb.db.spi.query.SelectExpression;
import org.litebridgedb.orm.function.Avg;
import org.litebridgedb.orm.function.Count;
import org.litebridgedb.orm.function.Expression;
import org.litebridgedb.orm.function.ProtoColumnExpression;
import org.litebridgedb.orm.function.ProtoExpression;
import org.litebridgedb.orm.function.ProtoTOColumnExpression;
import org.litebridgedb.orm.function.SelectColumn;
import org.litebridgedb.orm.function.SelectField;

final class SelectExpressionMapper {

    private final SqlFunctionRegistry sqlFunctionRegistry;

    SelectExpressionMapper(final SqlFunctionRegistry sqlFunctionRegistry) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
    }

    SelectExpression toSelectExpression(final Expression expression) {
        return switch (expression) {
            case SelectField selectField -> toSelectColumn(selectField);
            case SelectColumn selectColumn -> toSelectColumn(selectColumn);
            case Avg avg -> sqlFunctionRegistry.aggregate().avg().create(avg.column());
            case Count count -> sqlFunctionRegistry.aggregate().count();
            case ProtoExpression protoExpression ->
                    throw new IllegalStateException("ProtoExpression not resolved: " + protoExpression);
        };
    }

    ColumnExpression toSelectColumn(final SelectField selectField) {
        return sqlFunctionRegistry.selectColumnFactory()
                .create(ObjectUtils.requireNonNull(selectField.column(),
                        () -> new IllegalStateException("SelectField.column not set")));
    }

    ColumnExpression toSelectColumn(final SelectColumn selectColumn) {
        return sqlFunctionRegistry.selectColumnFactory()
                .create(selectColumn.column());
    }
}
