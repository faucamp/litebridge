package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.expression.Expression;
import org.litebridgedb.orm.expression.ProtoColumnExpression;
import org.litebridgedb.orm.expression.ProtoExpression;
import org.litebridgedb.orm.expression.select.SelectField;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

final class DtoExpressionResolver {

    private final DtoSelectSpec selectSpec;
    private final AliasGenerator aliasGenerator;
    private final ClassFieldAccessorCache classFieldAccessorCache;

    DtoExpressionResolver(final DtoSelectSpec selectSpec, final AliasGenerator aliasGenerator, final ClassFieldAccessorCache classFieldAccessorCache) {
        this.selectSpec = selectSpec;
        this.aliasGenerator = aliasGenerator;
        this.classFieldAccessorCache = classFieldAccessorCache;
    }

    Expression resolveExpression(final Expression expression) {
        if (expression instanceof ProtoExpression protoExpression) {
            if (protoExpression.type() == SelectField.class) {
                return resolveSelectField((ProtoColumnExpression) protoExpression);
            } else {
                return protoExpression.resolve(getColumn(protoExpression));
            }
        } else {
            return expression;
        }
    }

    private SelectField resolveSelectField(final ProtoColumnExpression protoColumnExpression) {
        // Map the input DTO field names to database column names
        final Column column = getColumn(protoColumnExpression);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(selectSpec.dtoClass(), protoColumnExpression.column());
        return new SelectField(fieldAccessor, column);
    }

    private Column getColumn(final ProtoExpression protoExpression) {
        // Map the input DTO field names to database column names
        final String fieldName = protoExpression.column();
        final ColumnMetaData columnMetaData = selectSpec.dtoTable().getColumnForFieldName(fieldName);
        return aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData);
    }
}
