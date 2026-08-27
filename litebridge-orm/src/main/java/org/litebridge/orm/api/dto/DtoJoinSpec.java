package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.impl.AbstractJoinSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.tracking.FieldAccessor;

import java.util.Collections;
import java.util.List;

/**
 * Specification for a JOIN clause in a DTO-based query.
 */
public final class DtoJoinSpec extends AbstractJoinSpec implements DtoDataSpec {

    private final Class<?> dtoClass;
    private final OrmTable ormTable;
    @Nullable
    private Class<?> sourceDtoClass;

    private final SelectExpressionMapper selectExpressionMapper;
    @Nullable
    private List<DtoSelectSpec.FieldColumn> fieldColumns;
    @Nullable
    private FieldAccessor collectionField;
    @Nullable
    private FieldAccessor reverseCollectionField;

    /**
     * Creates a new instance of {@code DtoJoinSpec}.
     *
     * @param dtoClass the class of the DTO being joined
     * @param ormTable the metadata of the table being joined
     * @param table the database table representation
     * @param selectExpressionMapper the mapper for select expressions
     */
    public DtoJoinSpec(final Class<?> dtoClass,
                       final OrmTable ormTable,
                       final Table table,
                       final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
        this.dtoClass = dtoClass;
        this.ormTable = ormTable;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    /**
     * Returns the DTO class being joined.
     *
     * @return the DTO class
     */
    public Class<?> dtoClass() {
        return dtoClass;
    }

    @Override
    public OrmTable dtoTable() {
        return ormTable;
    }

    /**
     * Returns the source DTO class for this join.
     *
     * @return the source DTO class
     */
    public @Nullable Class<?> sourceDtoClass() {
        return sourceDtoClass;
    }

    /**
     * Sets the source DTO class for this join.
     *
     * @param sourceDtoClass the source DTO class
     */
    public void setSourceDtoClass(@Nullable final Class<?> sourceDtoClass) {
        this.sourceDtoClass = sourceDtoClass;
    }

    /**
     * Returns the list of field columns for this join.
     *
     * @return the list of field columns
     */
    public List<DtoSelectSpec.FieldColumn> getFieldColumns() {
        return fieldColumns != null ? fieldColumns : Collections.emptyList();
    }

    /**
     * Sets the list of field columns for this join.
     *
     * @param fieldColumns the list of field columns
     */
    public void setFieldColumns(@Nullable final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        this.fieldColumns = fieldColumns;
    }

    /**
     * Returns the collection field for this join.
     *
     * @return the collection field, or null if not a collection join
     */
    public @Nullable FieldAccessor collectionField() {
        return collectionField;
    }

    /**
     * Sets the collection field for this join.
     *
     * @param collectionField the collection field
     */
    public void setCollectionField(@Nullable final FieldAccessor collectionField) {
        this.collectionField = collectionField;
    }

    /**
     * Returns the reverse collection field for this join.
     *
     * @return the reverse collection field, or null if none
     */
    public @Nullable FieldAccessor reverseCollectionField() {
        return reverseCollectionField;
    }

    /**
     * Sets the reverse collection field for this join.
     *
     * @param reverseCollectionField the reverse collection field
     */
    public void setReverseCollectionField(@Nullable final FieldAccessor reverseCollectionField) {
        this.reverseCollectionField = reverseCollectionField;
    }
}
