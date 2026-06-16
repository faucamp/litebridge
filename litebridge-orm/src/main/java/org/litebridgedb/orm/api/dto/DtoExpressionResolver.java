package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.function.Expression;
import org.litebridgedb.orm.function.ProtoColumnExpression;
import org.litebridgedb.orm.function.ProtoExpression;
import org.litebridgedb.orm.function.SelectField;
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
                return protoExpression.resolve(selectSpec.getTable());
            }
        } else {
            return expression;
        }
    }

    private SelectField resolveSelectField(final ProtoColumnExpression protoColumnExpression) {
        // Map the input DTO field names to database column names
        final String fieldName = protoColumnExpression.column();
        final ColumnMetaData columnMetaData = selectSpec.dtoTable().getColumnForFieldName(fieldName);
        final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(selectSpec.dtoClass(), fieldName);
        return new SelectField(fieldAccessor, column);
    }
}
