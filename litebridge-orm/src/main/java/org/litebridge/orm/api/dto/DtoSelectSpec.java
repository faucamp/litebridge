package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.DelegateExpressionSpec;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Specification for a DTO select query.
 */
public final class DtoSelectSpec extends SelectSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable dtoTable;
    private final @Nullable Class<?> typeOverride;

    /**
     * Creates a new DtoSelectSpec.
     *
     * @param dtoClass          the DTO class
     * @param dtoTable          the DTO table
     * @param aliasGenerator    the alias generator
     * @param litebridgeContext the litebridge context
     * @param typeOverride      the type override
     */
    public DtoSelectSpec(final Class<?> dtoClass,
                         final OrmTable dtoTable,
                         final AliasGenerator aliasGenerator,
                         final LitebridgeContext litebridgeContext,
                         final @Nullable Class<?> typeOverride) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
        this.dtoTable = dtoTable;
        this.table = aliasGenerator.aliasTable(dtoTable);
        this.typeOverride = typeOverride;
    }

    /**
     * Creates a new DtoSelectSpec without type override.
     *
     * @param dtoClass          the DTO class
     * @param dtoTable          the DTO table
     * @param aliasGenerator    the alias generator
     * @param litebridgeContext the litebridge context
     */
    public DtoSelectSpec(final Class<?> dtoClass,
                         final OrmTable dtoTable,
                         final AliasGenerator aliasGenerator,
                         final LitebridgeContext litebridgeContext) {
        this(dtoClass, dtoTable, aliasGenerator, litebridgeContext, null);
    }

    /**
     * Returns the DTO class.
     *
     * @return the DTO class
     */
    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return dtoTable;
    }

    /**
     * Returns the type override.
     *
     * @return the type override, or null if none
     */
    public @Nullable Class<?> typeOverride() {
        return typeOverride;
    }

    /**
     * Creates a new join specification.
     *
     * @param dtoClass the DTO class to join
     * @param ormTable the ORM table to join
     * @param table    the database table to join
     * @return the new join specification
     */
    public DtoJoinSpec newJoinSpec(final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        return addNewJoinSpecBefore(null, dtoClass, ormTable, table);
    }

    /**
     * Creates a new join specification before another join specification.
     *
     * @param other    the join specification to insert before
     * @param dtoClass the DTO class to join
     * @param ormTable the ORM table to join
     * @param table    the database table to join
     * @return the new join specification
     */
    public DtoJoinSpec newJoinSpecBefore(final DtoJoinSpec other, final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        return addNewJoinSpecBefore(other, dtoClass, ormTable, table);
    }

    private DtoJoinSpec addNewJoinSpecBefore(final @Nullable DtoJoinSpec other, final Class<?> dtoClass, final OrmTable ormTable, final Table table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final DtoJoinSpec joinSpec = new DtoJoinSpec(dtoClass, ormTable, table, selectExpressionMapper());

        if (other != null) {
            joins.add(joins.indexOf(other), joinSpec);
        } else {
            joins.add(joinSpec);
        }

        return joinSpec;
    }

    /**
     * Creates select field specifications for the given fields.
     *
     * @param fields the field names
     * @return the list of expression specifications
     */
    public List<ExpressionSpec> createSelectFieldSpecs(final String[] fields) {
        return Arrays.stream(fields)
                .map(this::createSelectFieldSpec)
                .toList();
    }

    private ExpressionSpec createSelectFieldSpec(final String field) {
        final ColumnMetaData columnMetaData = dtoTable.getColumnForFieldName(field);
        final FieldAccessor fieldAccessor = dtoTable.getFieldForColumnName(columnMetaData.name());
        return new SelectFieldSpec(fieldAccessor, columnMetaData.toColumn());
    }

    /**
     * Returns the list of field columns for this select.
     *
     * @return the list of field columns
     */
    public List<FieldColumn> getFieldColumns() {
        return expressionSpecs.stream()
                .flatMap(this::extractFieldColumns)
                .toList();
    }

    private Stream<FieldColumn> extractFieldColumns(final ExpressionSpec expression) {
        if (expression instanceof SelectFieldSpec sfs) {
            return Stream.of(new FieldColumn(sfs.field(), sfs.getColumn()));
        } else if (expression instanceof DelegateExpressionSpec des) {
            return extractFieldColumns(des.target()).map(fc -> new FieldColumn(fc.fieldAccessor(), des.getColumn()));
        } else if (expression instanceof TypeOverrideExpressionSpec<?> toes) {
            if (toes instanceof org.litebridge.orm.expression.intent.ConvertSpec<?> cs) {
                return extractFieldColumns(cs.target());
            }
        }
        return Stream.empty();
    }

    /**
     * Represents a pair of a field accessor and its corresponding database column.
     *
     * @param fieldAccessor the field accessor
     * @param column        the database column
     */
    public record FieldColumn(FieldAccessor fieldAccessor, Column column) {
    }
}
