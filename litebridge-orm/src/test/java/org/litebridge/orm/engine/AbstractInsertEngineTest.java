package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractInsertEngineTest {

    @Test
    void createUpdateMetaDataReturnsNullWhenOperationIsNotInsert() {
        // Given
        final Update nonInsertOp = mock(Update.class);
        final PreparedOperation preparedOp = new PreparedOperation(nonInsertOp, Collections.emptyList());
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final UpdateMetaData metaData = AbstractInsertEngine.createUpdateMetaData(preparedOp, () -> new Table("test"), context);

        // Then
        assertNull(metaData);
    }

    @Test
    void createUpdateMetaDataWhenReturnGeneratedKeysIsFalse() {
        // Given
        final Table table = new Table("test");
        final Insert insert = mock(Insert.class);
        when(insert.rows()).thenReturn(1);
        when(insert.columns()).thenReturn(List.of(new UpdateColumn("id"), new UpdateColumn("name")));
        when(insert.returnGeneratedKeys()).thenReturn(false);

        final PreparedOperation preparedOp = new PreparedOperation(insert, Collections.emptyList());
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final UpdateMetaData metaData = AbstractInsertEngine.createUpdateMetaData(preparedOp, () -> table, context);

        // Then
        assertFalse(metaData.returnGeneratedKeys());
        assertNull(metaData.generatedKeys());
        assertNull(metaData.generatedKeyNames());
        assertEquals(1, metaData.rows());
        assertEquals(2, metaData.bindValueColumns());
    }

    @Test
    void createUpdateMetaDataWhenReturnGeneratedKeysIsTrueWithoutGeneratedPks() {
        // Given
        final Table table = new Table("test");
        final Insert insert = mock(Insert.class);
        when(insert.rows()).thenReturn(2);
        when(insert.columns()).thenReturn(List.of(new UpdateColumn("id"), new UpdateColumn("name")));
        when(insert.returnGeneratedKeys()).thenReturn(true);

        final PreparedOperation preparedOp = new PreparedOperation(insert, Collections.emptyList());
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableMetaDataCache cache = mock(TableMetaDataCache.class);
        final TableMetaData tableMetaData = mock(TableMetaData.class);

        when(context.tableMetaDataCache()).thenReturn(cache);
        when(cache.ensureTableMetaData(table)).thenReturn(tableMetaData);

        // Primary key column is manual, not auto increment and no sequence generator
        final ColumnMetaData manualPk = new ColumnMetaData(table, "id", false, Types.BIGINT);
        when(tableMetaData.primaryKey()).thenReturn(List.of(manualPk));

        // When
        final UpdateMetaData metaData = AbstractInsertEngine.createUpdateMetaData(preparedOp, () -> table, context);

        // Then
        assertFalse(metaData.returnGeneratedKeys());
        assertNull(metaData.generatedKeys());
        assertNull(metaData.generatedKeyNames());
        assertEquals(2, metaData.rows());
        assertEquals(2, metaData.bindValueColumns());
    }

    @Test
    void createUpdateMetaDataWhenReturnGeneratedKeysIsTrueWithGeneratedPks() {
        // Given
        final Table table = new Table("test");
        final Insert insert = mock(Insert.class);
        when(insert.rows()).thenReturn(1);
        when(insert.columns()).thenReturn(List.of(
                new UpdateColumn("id"),
                new UpdateColumn("seq_id"),
                new UpdateColumn("other_id"),
                new UpdateColumn("name")));
        when(insert.returnGeneratedKeys()).thenReturn(true);

        final PreparedOperation preparedOp = new PreparedOperation(insert, Collections.emptyList());
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableMetaDataCache cache = mock(TableMetaDataCache.class);
        final TableMetaData tableMetaData = mock(TableMetaData.class);

        when(context.tableMetaDataCache()).thenReturn(cache);
        when(cache.ensureTableMetaData(table)).thenReturn(tableMetaData);

        // 1. Auto-increment PK
        final ColumnMetaData autoIncPk = new ColumnMetaData(table, "id", false, Types.BIGINT, 0, 0, true, null, null);

        // 2. Sequence generator PK
        final SequenceColumnValueGenerator seqGen = mock(SequenceColumnValueGenerator.class);
        final ColumnMetaData seqPk = new ColumnMetaData(table, "seq_id", false, Types.BIGINT, 0, 0, false, null, seqGen);

        // 3. Other generator PK (not sequence)
        final ColumnValueGenerator otherGen = mock(ColumnValueGenerator.class);
        final ColumnMetaData otherPk = new ColumnMetaData(table, "other_id", false, Types.BIGINT, 0, 0, false, null, otherGen);

        when(tableMetaData.primaryKey()).thenReturn(List.of(autoIncPk, seqPk, otherPk));

        // When
        final UpdateMetaData metaData = AbstractInsertEngine.createUpdateMetaData(preparedOp, () -> table, context);

        // Then
        assertTrue(metaData.returnGeneratedKeys());
        assertEquals(List.of(autoIncPk, seqPk), metaData.generatedKeys());
        assertArrayEquals(new String[]{"id", "seq_id"}, metaData.generatedKeyNames());
        assertEquals(1, metaData.rows());
        assertEquals(2, metaData.bindValueColumns()); // 4 columns - 2 generated PKs
    }

    @Test
    void getGeneratedPrimaryKeyColumnsMethod() {
        // Given
        final Table table = new Table("test");
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        final ColumnMetaData col1 = new ColumnMetaData(table, "id", false, Types.BIGINT, 0, 0, true, null, null);
        final ColumnMetaData col2 = new ColumnMetaData(table, "name", false, Types.VARCHAR, 0, 0, false, null, null);

        when(tableMetaData.primaryKey()).thenReturn(List.of(col1, col2));

        // When
        final List<ColumnMetaData> generated = AbstractInsertEngine.getGeneratedPrimaryKeyColumns(tableMetaData);

        // Then
        assertEquals(List.of(col1), generated);
    }
}
