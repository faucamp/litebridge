package org.litebridge.maven.reverse;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.maven.DebugMojoLog;
import org.litebridge.maven.config.reverse.ColumnMappingConfig;
import org.litebridge.maven.config.reverse.RevEngOutputConfig;
import org.litebridge.maven.config.reverse.SqlTypeMappingConfig;
import org.litebridge.maven.config.reverse.TableMappingConfig;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EntityGeneratorTest {

    @Test
    void createEntityClassForTable_basic() throws MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final EntityGenerator generator = new EntityGenerator(null, null, output, log);

        final TableMetaData table = mock(TableMetaData.class);
        when(table.name()).thenReturn("person");
        when(table.qualifiedName()).thenReturn("person");

        final ColumnMetaData column = mock(ColumnMetaData.class);
        when(column.name()).thenReturn("id");
        when(column.getDataType()).thenReturn(Types.BIGINT);
        when(column.isNullable()).thenReturn(false);
        when(column.toColumn()).thenReturn(new Column(new Table("person"), "id"));

        when(table.columns()).thenReturn(List.of(column));

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(table, Collections.emptyMap(), new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), new HashMap<>());

        // Then
        assertNotNull(result);
        assertEquals("Person", result.className());
        final String code = result.entity().toString();
        assertTrue(code.contains("class Person"));
        assertTrue(code.contains("private long id;"));
    }

    @Test
    void createEntityClassForTable_sqlTypeMapping_complexity() throws MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        // Define multiple overlapping mappings to test the reduction logic
        final List<SqlTypeMappingConfig> mappings = new ArrayList<>();

        // Mapping 1: basic match
        final SqlTypeMappingConfig m1 = new SqlTypeMappingConfig();
        m1.setJdbcType(java.sql.JDBCType.INTEGER);
        m1.setFieldType("java.lang.Integer");
        mappings.add(m1);

        // Mapping 2: match with precision
        final SqlTypeMappingConfig m2 = new SqlTypeMappingConfig();
        m2.setJdbcType(java.sql.JDBCType.INTEGER);
        m2.setPrecision(10);
        m2.setFieldType("java.lang.Long");
        mappings.add(m2);

        // Mapping 3: match with NOT NULL
        final SqlTypeMappingConfig m3 = new SqlTypeMappingConfig();
        m3.setJdbcType(java.sql.JDBCType.INTEGER);
        m3.setNotNull(true);
        m3.setFieldType("int");
        mappings.add(m3);

        final EntityGenerator generator = new EntityGenerator(mappings, null, output, log);

        final TableMetaData table = mock(TableMetaData.class);
        when(table.name()).thenReturn("test");
        when(table.qualifiedName()).thenReturn("public.test");

        final Table testTableSpi = new Table(null, "public", "test");

        // Test case 1: matches m2 (precision 10)
        final ColumnMetaData column1 = new ColumnMetaData(testTableSpi, "val1", false, Types.INTEGER, 10);

        // Test case 2: matches m3 (not null)
        final ColumnMetaData column2 = new ColumnMetaData(testTableSpi, "val2", false, Types.INTEGER, 5);

        // Test case 3: matches m1 (basic)
        final ColumnMetaData column3 = new ColumnMetaData(testTableSpi, "val3", true, Types.INTEGER, 5);

        when(table.columns()).thenReturn(List.of(column1, column2, column3));

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(table, Collections.emptyMap(), new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), new HashMap<>());

        // Then
        assertNotNull(result);
        final String code = result.entity().toString();
        assertTrue(code.contains("private Long val1;")); // m2 wins, it is NOT converted to primitive automatically if mapped via SqlTypeMapping?
        assertTrue(code.contains("private int val2;")); // m3 wins, mapped to primitive "int"
        assertTrue(code.contains("private Integer val3;")); // m1 wins
    }

    @Test
    void createEntityClassForTable_columnMapping_fieldTypeNotFound() throws MojoExecutionException {
        // Given
        final Log log = mock(Log.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final TableMappingConfig tableConfig = new TableMappingConfig();
        tableConfig.setTable("person");
        final ColumnMappingConfig colConfig = new ColumnMappingConfig();
        colConfig.setColumn("id");
        colConfig.setFieldType("com.nonexistent.Unknown");
        tableConfig.setColumnMappings(List.of(colConfig));

        final EntityGenerator generator = new EntityGenerator(null, List.of(tableConfig), output, log);

        final TableMetaData table = mock(TableMetaData.class);
        when(table.name()).thenReturn("person");
        when(table.qualifiedName()).thenReturn("person");

        final ColumnMetaData column = mock(ColumnMetaData.class);
        when(column.name()).thenReturn("id");
        when(column.getDataType()).thenReturn(Types.BIGINT);
        when(column.isNullable()).thenReturn(false);
        when(column.toColumn()).thenReturn(new Column(new Table("person"), "id"));

        when(table.columns()).thenReturn(List.of(column));

        // When / Then should throw MojoExecutionException because getPrimitiveClass will throw CNFE
        assertThrows(MojoExecutionException.class, () -> generator.createEntityClassForTable(table, Collections.emptyMap(), new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), new HashMap<>()));
    }

    @Test
    void createEntityClassForTable_sqlTypeMapping_fieldTypeNotFound() throws MojoExecutionException {
        // Given
        final Log log = mock(Log.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final SqlTypeMappingConfig m1 = new SqlTypeMappingConfig();
        m1.setJdbcType(java.sql.JDBCType.BIGINT);
        m1.setFieldType("com.nonexistent.Unknown");

        final EntityGenerator generator = new EntityGenerator(List.of(m1), null, output, log);

        final TableMetaData table = mock(TableMetaData.class);
        when(table.name()).thenReturn("person");
        when(table.qualifiedName()).thenReturn("person");

        final ColumnMetaData column = mock(ColumnMetaData.class);
        when(column.name()).thenReturn("id");
        when(column.getDataType()).thenReturn(Types.BIGINT);
        when(column.isNullable()).thenReturn(false);
        when(column.toColumn()).thenReturn(new Column(new Table("person"), "id"));

        when(table.columns()).thenReturn(List.of(column));

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(table, Collections.emptyMap(), new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), new HashMap<>());

        // Then
        assertNotNull(result);
        assertTrue(result.entity().toString().contains("private com.nonexistent.Unknown id;"));
        verify(log).warn(anyString());
    }

    @Test
    void createEntityClassForTable_manyToMany() throws MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final EntityGenerator generator = new EntityGenerator(null, null, output, log);

        final Table personTableSpi = new Table("person");
        final TableMetaData personTable = mock(TableMetaData.class);
        when(personTable.name()).thenReturn("person");
        when(personTable.qualifiedName()).thenReturn("person");

        final ColumnMetaData idColumn = new ColumnMetaData(personTableSpi, "id", false, Types.BIGINT);
        when(personTable.columns()).thenReturn(List.of(idColumn));

        final Table roleTableSpi = new Table("role");
        final TableMetaData roleTable = mock(TableMetaData.class);
        when(roleTable.name()).thenReturn("role");
        when(roleTable.qualifiedName()).thenReturn("role");

        final Table joinTable = new Table("person_role");
        Column leftJoinColumn = new Column(joinTable, "person_id");
        Column rightJoinColumn = new Column(joinTable, "role_id");

        ManyToManyMapping mtm = new ManyToManyMapping(
                personTable, idColumn,
                joinTable, leftJoinColumn, rightJoinColumn,
                roleTable, new ColumnMetaData(roleTableSpi, "id", false, Types.BIGINT)
        );

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(personTable, Collections.emptyMap(), new ManyToManyMappingResult(List.of(mtm), Collections.emptySet()), new HashMap<>());

        // Then
        assertNotNull(result);
        final String code = result.entity().toString();
        assertTrue(code.contains("@ManyToMany"));
        assertTrue(code.contains("List<Role> roles"));
    }

    @Test
    void createEntityClassForTable_oneToMany() throws MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final EntityGenerator generator = new EntityGenerator(null, null, output, log);

        final Table personTableSpi = new Table(null, "public", "person");
        final TableMetaData personTable = mock(TableMetaData.class);
        when(personTable.name()).thenReturn("person");
        when(personTable.qualifiedName()).thenReturn("public.person");

        final ColumnMetaData idColumn = new ColumnMetaData(personTableSpi, "id", false, Types.BIGINT);

        final Table accountTableSpi = new Table(null, "public", "account");
        final Column accountOwnerCol = new Column(accountTableSpi, "owner_id");
        idColumn.addForeignReference(new org.litebridge.db.spi.ForeignKeyConstraint(
                "fk_account_person",
                accountOwnerCol
        ));

        when(personTable.columns()).thenReturn(List.of(idColumn));

        final Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
        final TableMetaData accountTable = mock(TableMetaData.class);
        when(accountTable.name()).thenReturn("account");
        when(accountTable.qualifiedName()).thenReturn("public.account");
        tableMetaDataMap.put("public.account", accountTable);

        final ColumnMetaData accountIdCol = new ColumnMetaData(accountTableSpi, "id", false, Types.BIGINT);
        final ColumnMetaData accountOwnerColMeta = new ColumnMetaData(accountTableSpi, "owner_id", false, Types.BIGINT);
        when(accountTable.columns()).thenReturn(List.of(accountIdCol, accountOwnerColMeta));

        final Map<String, GeneratedEntity> entities = new HashMap<>();

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(personTable, tableMetaDataMap, new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), entities);

        // Then
        assertNotNull(result);
        final String code = result.entity().toString();
        assertTrue(code.contains("@OneToMany"));
        assertTrue(code.contains("List<Account> accounts"));
    }

    @Test
    void createEntityClassForTable_OneToMany_JoinOnFieldNotFound() throws MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final EntityGenerator generator = new EntityGenerator(null, null, output, log);

        final Table personTableSpi = new Table(null, "public", "person");
        final TableMetaData personTable = mock(TableMetaData.class);
        when(personTable.name()).thenReturn("person");
        when(personTable.qualifiedName()).thenReturn("public.person");

        final ColumnMetaData idColumn = new ColumnMetaData(personTableSpi, "id", false, Types.BIGINT);

        final Table accountTableSpi = new Table(null, "public", "account");
        final Column accountOwnerCol = new Column(accountTableSpi, "owner_id");
        idColumn.addForeignReference(new org.litebridge.db.spi.ForeignKeyConstraint(
                "fk_account_person",
                accountOwnerCol
        ));

        when(personTable.columns()).thenReturn(List.of(idColumn));

        final Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
        final TableMetaData accountTable = mock(TableMetaData.class);
        when(accountTable.name()).thenReturn("account");
        when(accountTable.qualifiedName()).thenReturn("public.account");
        tableMetaDataMap.put("public.account", accountTable);

        // Return an empty list for account columns so 'owner_id' won't be in the field map
        when(accountTable.columns()).thenReturn(Collections.emptyList());

        final Map<String, GeneratedEntity> entities = new HashMap<>();

        // When / Then
        assertThrows(MojoExecutionException.class, () -> generator.createEntityClassForTable(personTable, tableMetaDataMap, new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), entities));
    }

    @Test
    void createEntityClassForTable_columnMapping_sequenceAndGenerator() throws MojoExecutionException {
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final TableMappingConfig tableConfig = new TableMappingConfig();
        tableConfig.setTable("public.person");
        final ColumnMappingConfig colConfig = new ColumnMappingConfig();
        colConfig.setColumn("id");
        colConfig.setGenerateUsingSequence("person_seq");
        colConfig.setGeneratorClass("com.example.MyGenerator");
        tableConfig.setColumnMappings(List.of(colConfig));

        final EntityGenerator generator = new EntityGenerator(null, List.of(tableConfig), output, log);

        final TableMetaData table = mock(TableMetaData.class);
        when(table.name()).thenReturn("person");
        when(table.qualifiedName()).thenReturn("public.person");

        final Table personTableSpi = new Table(null, "public", "person");
        final ColumnMetaData column = new ColumnMetaData(personTableSpi, "id", false, Types.BIGINT);
        when(table.columns()).thenReturn(List.of(column));

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(table, Collections.emptyMap(), new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), new HashMap<>());

        // Then
        assertNotNull(result);
        final String code = result.entity().toString();
        assertTrue(code.contains("@Column"));
        assertTrue(code.contains("generateUsingSequence = \"person_seq\""));
        assertTrue(code.contains("generator = com.example.MyGenerator"));
    }

    @Test
    void createEntityClassForTable_tableMapping_customName_allowInterface() throws MojoExecutionException {
        // Given
        final Log log = new DebugMojoLog(EntityGenerator.class);
        final RevEngOutputConfig output = new RevEngOutputConfig();
        output.setOutputPackage("com.example.generated");

        final TableMappingConfig tableConfig = new TableMappingConfig();
        tableConfig.setTable("public.person");
        tableConfig.setEntityName("PersonDTO");
        tableConfig.setAllowInterface("com.example.PersonInterface");

        final EntityGenerator generator = new EntityGenerator(null, List.of(tableConfig), output, log);

        final TableMetaData table = mock(TableMetaData.class);
        when(table.name()).thenReturn("person");
        when(table.qualifiedName()).thenReturn("public.person");

        final Table personTableSpi = new Table(null, "public", "person");
        final ColumnMetaData column = new ColumnMetaData(personTableSpi, "id", false, Types.BIGINT);
        when(table.columns()).thenReturn(List.of(column));

        // When
        final GeneratedEntity result = generator.createEntityClassForTable(table, Collections.emptyMap(), new ManyToManyMappingResult(Collections.emptyList(), Collections.emptySet()), new HashMap<>());

        // Then
        assertNotNull(result);
        assertEquals("PersonDTO", result.className());
        final String code = result.entity().toString();
        assertTrue(code.contains("@AllowInterface(com.example.PersonInterface.class)"));
    }
}
