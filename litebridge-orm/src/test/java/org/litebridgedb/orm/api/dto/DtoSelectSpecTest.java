package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoSelectSpecTest {

    @Test
    void constructorAndBasicGetters() {
        // Given
        final Class<?> dtoClass = TestDto.class;
        final OrmTable ormTable = mock(OrmTable.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final Table table = new Table("TEST_TABLE");
        when(ormTable.dtoClass()).thenReturn((Class) dtoClass);
        when(ormTable.getMetaData()).thenReturn(new org.litebridgedb.db.spi.TableMetaData(table, List.of(), List.of()));

        // When
        final DtoSelectSpec spec = new DtoSelectSpec(dtoClass, ormTable, aliasGenerator, litebridgeContext);

        // Then
        assertEquals(dtoClass, spec.dtoClass());
        assertEquals(ormTable, spec.dtoTable());
        assertEquals(table, spec.getTable());
        assertNull(spec.typeOverride());
    }

    @Test
    void constructorWithTypeOverride() {
        // Given
        final Class<?> dtoClass = TestDto.class;
        final Class<?> typeOverride = String.class;
        final OrmTable ormTable = mock(OrmTable.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final Table table = new Table("TEST_TABLE");
        when(ormTable.dtoClass()).thenReturn((Class) dtoClass);
        when(ormTable.getMetaData()).thenReturn(new org.litebridgedb.db.spi.TableMetaData(table, List.of(), List.of()));

        // When
        final DtoSelectSpec spec = new DtoSelectSpec(dtoClass, ormTable, aliasGenerator, litebridgeContext, typeOverride);

        // Then
        assertEquals(typeOverride, spec.typeOverride());
    }

    @Test
    void newJoinSpec() {
        // Given
        final Class<?> dtoClass = TestDto.class;
        final OrmTable ormTable = mock(OrmTable.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final Table table = new Table("TEST_TABLE");
        when(ormTable.dtoClass()).thenReturn((Class) dtoClass);
        when(ormTable.getMetaData()).thenReturn(new org.litebridgedb.db.spi.TableMetaData(table, List.of(), List.of()));
        final DtoSelectSpec spec = new DtoSelectSpec(dtoClass, ormTable, aliasGenerator, litebridgeContext);
        spec.setProtoExpressionResolver(mock(DtoProtoExpressionResolver.class));

        final Class<?> joinDtoClass = AnotherDto.class;
        final OrmTable joinOrmTable = mock(OrmTable.class);
        final Table joinTable = new Table("JOIN_TABLE", "T2");

        // When
        final DtoJoinSpec joinSpec = spec.newJoinSpec(joinDtoClass, joinOrmTable, joinTable);

        // Then
        assertNotNull(joinSpec);
        assertEquals(1, spec.getJoins().size());
        assertEquals(joinSpec, spec.getJoins().get(0));
    }

    @Test
    void createSelectFieldSpecs() {
        // Given
        final Class<?> dtoClass = TestDto.class;
        final OrmTable ormTable = mock(OrmTable.class);
        final AliasGenerator aliasGenerator = new NoOpAliasGenerator();
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final Table table = new Table("TEST_TABLE");
        when(ormTable.dtoClass()).thenReturn((Class) dtoClass);
        when(ormTable.getMetaData()).thenReturn(new org.litebridgedb.db.spi.TableMetaData(table, List.of(), List.of()));
        final DtoSelectSpec spec = new DtoSelectSpec(dtoClass, ormTable, aliasGenerator, litebridgeContext);

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.name()).thenReturn("TEST_COLUMN");
        when(columnMetaData.toColumn()).thenReturn(new org.litebridgedb.db.spi.Column(table, "TEST_COLUMN"));
        when(ormTable.getColumnForFieldName("testField")).thenReturn(columnMetaData);
        final FieldAccessor fieldAccessor = mock(FieldAccessor.class);
        when(ormTable.getFieldForColumnName("TEST_COLUMN")).thenReturn(fieldAccessor);

        // When
        final List<ExpressionSpec> fieldSpecs = spec.createSelectFieldSpecs(new String[]{"testField"});

        // Then
        assertEquals(1, fieldSpecs.size());
    }

    private static class TestDto {}
    private static class AnotherDto {}
}
