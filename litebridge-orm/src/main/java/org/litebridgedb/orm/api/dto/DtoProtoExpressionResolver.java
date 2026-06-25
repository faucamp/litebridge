package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.orm.api.select.impl.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.intent.ConvertSpec;
import org.litebridgedb.orm.expression.intent.ExpressionSpecArray;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.stream.Stream;

public final class DtoProtoExpressionResolver extends ProtoExpressionResolver {

    private final DtoSelectSpec selectSpec;
    private final AliasGenerator aliasGenerator;
    private final ClassFieldAccessorCache classFieldAccessorCache;

    public DtoProtoExpressionResolver(final DtoSelectSpec selectSpec, final AliasGenerator aliasGenerator, final ClassFieldAccessorCache classFieldAccessorCache) {
        this.selectSpec = selectSpec;
        this.aliasGenerator = aliasGenerator;
        this.classFieldAccessorCache = classFieldAccessorCache;
    }

    @Override
    protected ColumnExpressionSpec resolveSelectField(final Resolvable resolvable) {
        // Map the input DTO field names to database column names
        final Column column = getColumn(resolvable);
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(((DtoSelectSpec) selectSpec).dtoClass(), resolvable.column());
        return new SelectFieldSpec(fieldAccessor, column);
    }

    @Override
    protected Column getColumn(final Resolvable resolvable) {
        // Map the input DTO field names to database column names
        final String fieldName = resolvable.column();
        final ColumnMetaData columnMetaData = ((DtoSelectSpec) selectSpec).dtoTable().getColumnForFieldName(fieldName);
        return aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData);
    }
}
