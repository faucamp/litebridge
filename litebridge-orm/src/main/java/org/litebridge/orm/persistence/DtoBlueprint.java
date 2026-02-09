package org.litebridge.orm.persistence;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.dto.DtoDataSpec;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;

import java.util.ArrayList;
import java.util.List;

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

    public static abstract class DtoData<S extends DtoDataSpec> {
        protected final S spec;
        private final List<Object> primaryKey;
        private final Row row;

        public DtoData(final S spec, final List<Object> primaryKey, final Row row) {
            this.spec = spec;
            this.primaryKey = primaryKey;
            this.row = row;
        }

        public abstract Class<?> dtoClass();

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

    public static class SelectDtoData extends DtoData<DtoSelectSpec> {
        public SelectDtoData(final DtoSelectSpec dtoSelectSpec, final List<Object> primaryKey, final Row dtoRows) {
            super(dtoSelectSpec, primaryKey, dtoRows);
        }

        @Override
        public Class<?> dtoClass() {
            return spec.dtoClass();
        }
    }

    public static class JoinDtoData extends DtoData<DtoJoinSpec> {

        public JoinDtoData(final DtoJoinSpec dtoJoinSpec, final List<Object> primaryKey, final Row dtoRows) {
            super(dtoJoinSpec, primaryKey, dtoRows);
        }

        @Override
        public Class<?> dtoClass() {
            return spec.dtoClass();
        }
    }
}
