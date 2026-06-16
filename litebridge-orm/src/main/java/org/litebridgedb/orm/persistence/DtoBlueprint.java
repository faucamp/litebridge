package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.dto.DtoDataSpec;
import org.litebridgedb.orm.api.dto.DtoJoinSpec;
import org.litebridgedb.orm.api.dto.DtoSelectSpec;
import org.litebridgedb.orm.function.SelectField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DtoBlueprint {

    private final SelectDtoData selectDtoData;
    private final List<JoinDtoData> joinedDtoData = new ArrayList<>();

    public DtoBlueprint(final DtoSelectSpec dtoSelectSpec, final List<Object> primaryKey, final Row dtoRow) {
        this.selectDtoData = new SelectDtoData(dtoSelectSpec, primaryKey, dtoRow);
    }

    public SelectDtoData dtoData() {
        return selectDtoData;
    }

    public List<JoinDtoData> joinedDtoData() {
        return joinedDtoData;
    }

    public void addJoinedDtoData(final DtoJoinSpec dtoJoinSpec, final List<Object> primaryKey, final Row dtoRow) {
        joinedDtoData.add(new JoinDtoData(dtoJoinSpec, primaryKey, dtoRow));
    }

    public static sealed abstract class DtoData<S extends DtoDataSpec> permits SelectDtoData, JoinDtoData {
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

        public abstract Class<?> dtoClass();

        public List<DtoSelectSpec.FieldColumn> fieldColumns() {
            return fieldColumns;
        }

        public List<Object> primaryKey() {
            return primaryKey;
        }

        public Row row() {
            return row;
        }

        public S spec() {
            return spec;
        }
    }

    public static final class SelectDtoData extends DtoData<DtoSelectSpec> {
        public SelectDtoData(final DtoSelectSpec dtoSelectSpec, final List<Object> primaryKey, final Row dtoRows) {
            super(dtoSelectSpec,
                    dtoSelectSpec.getExpressions().stream()
                            .filter(expression -> expression instanceof SelectField)
                            .map(expression -> (SelectField) expression)
                            .filter(selectField -> selectField.column().table().equals(dtoSelectSpec.getTable()))
                            .map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.column()))
                            .toList(),
                    primaryKey,
                    dtoRows);
        }

        @Override
        public Class<?> dtoClass() {
            return spec.dtoClass();
        }
    }

    public static final class JoinDtoData extends DtoData<DtoJoinSpec> {

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
