package org.litebridge.orm.persistence;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.dto.DtoDataSpec;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.expression.DelegateExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A blueprint for creating and managing data transfer objects (DTOs).
 * <p>
 * This class encapsulates the primary data and any joined data associated with a DTO.
 */
public class DtoBlueprint {

    private final SelectDtoData selectDtoData;
    private final List<JoinDtoData> joinedDtoData = new ArrayList<>();

    /**
     * Creates a new DTO blueprint.
     *
     * @param dtoSelectSpec the select specification for the DTO
     * @param primaryKey    the primary key values
     * @param dtoRow        the database row data
     */
    public DtoBlueprint(final DtoSelectSpec dtoSelectSpec, final List<Object> primaryKey, final Row dtoRow) {
        this.selectDtoData = new SelectDtoData(dtoSelectSpec, primaryKey, dtoRow);
    }

    /**
     * Returns the primary DTO data.
     *
     * @return the primary DTO data
     */
    public SelectDtoData dtoData() {
        return selectDtoData;
    }

    /**
     * Returns the list of joined DTO data.
     *
     * @return the list of joined DTO data
     */
    public List<JoinDtoData> joinedDtoData() {
        return joinedDtoData;
    }

    /**
     * Adds joined DTO data to this blueprint.
     *
     * @param dtoJoinSpec the join specification
     * @param primaryKey  the primary key values
     * @param dtoRow      the database row data
     */
    public void addJoinedDtoData(final DtoJoinSpec dtoJoinSpec, final List<Object> primaryKey, final Row dtoRow) {
        joinedDtoData.add(new JoinDtoData(dtoJoinSpec, primaryKey, dtoRow));
    }

    /**
     * Base class for entity/DTO-specific blueprint data.
     * <p>
     * This class is designed to be extended by specific implementations such as
     * {@code SelectDtoData} and {@code JoinDtoData}.
     *
     * @param <S> The type parameter defining the specification for the DTO data, which must extend {@link DtoDataSpec}.
     */
    public static sealed abstract class DtoData<S extends DtoDataSpec> permits SelectDtoData, JoinDtoData {
        /**
         * The specification associated with this data.
         */
        protected final S spec;
        private final List<DtoSelectSpec.FieldColumn> fieldColumns;
        private final List<Object> primaryKey;
        private final Row row;

        private DtoData(final S spec, final List<DtoSelectSpec.FieldColumn> fieldColumns, final List<Object> primaryKey, final Row row) {
            this.spec = spec;
            this.fieldColumns = fieldColumns;
            this.primaryKey = primaryKey;
            this.row = row;
        }

        /**
         * Provides the class type of the Data Transfer Object (DTO) associated with the implementing class.
         *
         * @return The {@link Class} object representing the type of the DTO.
         */
        public abstract Class<?> dtoClass();

        /**
         * Retrieves the list of field-to-column mappings managed by this DTO data instance.
         * Each mapping associates a field accessor with a corresponding database column.
         *
         * @return A list of {@link DtoSelectSpec.FieldColumn} objects representing the field-to-column mappings.
         */
        public List<DtoSelectSpec.FieldColumn> fieldColumns() {
            return fieldColumns;
        }

        /**
         * Retrieves the primary key values associated with this DTO data instance.
         * The primary key is a unique identifier for the data represented by this instance.
         *
         * @return A list of the primary key values.
         */
        public List<Object> primaryKey() {
            return primaryKey;
        }

        /**
         * Retrieves the row associated with this DTO data instance.
         * The row represents the data in the database corresponding to the DTO.
         *
         * @return The {@link Row} object representing the database row.
         */
        public Row row() {
            return row;
        }

        /**
         * Retrieves the specification associated with this data instance.
         * The specification defines the structural and functional details
         * for the Data Transfer Object (DTO).
         *
         * @return The specification object of type {@code S} associated with this data.
         */
        public S spec() {
            return spec;
        }
    }

    /**
     * Specialised data container for DTO-based selections in the context of a {@link DtoBlueprint}.
     * <p>
     * This class extends the generic {@code DtoData} class and provides specific functionality
     * related to selections defined by a {@link DtoSelectSpec}.
     * <p>
     * Each instance manages the mapping of a {@link DtoSelectSpec} to the corresponding columns,
     * primary keys, and rows which collectively form the representation of the DTO data.
     * <p>
     * The constructor automatically filters and maps the provided expressions from the {@code DtoSelectSpec},
     * isolating only those that correspond to {@link SelectFieldSpec} for the relevant table.
     */
    public static final class SelectDtoData extends DtoData<DtoSelectSpec> {
        /**
         * Creates a new select DTO data instance.
         *
         * @param dtoSelectSpec the select specification
         * @param primaryKey    the primary key values
         * @param dtoRows       the database row data
         */
        public SelectDtoData(final DtoSelectSpec dtoSelectSpec, final List<Object> primaryKey, final Row dtoRows) {
            super(dtoSelectSpec,
                    dtoSelectSpec.getExpressions().stream()
                            .map(expressionSpec -> {
                                if (expressionSpec instanceof DelegateExpressionSpec delegateExpressionSpec) {
                                    return delegateExpressionSpec.target();
                                } else {
                                    return expressionSpec;
                                }
                            })
                            .filter(expression -> expression instanceof SelectFieldSpec)
                            .map(expression -> (SelectFieldSpec) expression)
                            .filter(selectField -> selectField.getColumn().table().equals(dtoSelectSpec.getTable()))
                            .map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.getColumn()))
                            .toList(),
                    primaryKey,
                    dtoRows);
        }

        @Override
        public Class<?> dtoClass() {
            return spec.dtoClass();
        }
    }

    /**
     * Specialised data container for DTO-based joins in the context of a {@link DtoBlueprint}.
     */
    public static final class JoinDtoData extends DtoData<DtoJoinSpec> {

        /**
         * Creates a new join DTO data instance.
         *
         * @param dtoJoinSpec the join specification
         * @param primaryKey  the primary key values
         * @param dtoRows     the database row data
         */
        public JoinDtoData(final DtoJoinSpec dtoJoinSpec, final List<Object> primaryKey, final Row dtoRows) {
            super(dtoJoinSpec,
                    Objects.requireNonNull(dtoJoinSpec.getFieldColumns()),
                    primaryKey,
                    dtoRows);
        }

        @Override
        public Class<?> dtoClass() {
            return spec.dtoClass();
        }
    }
}
