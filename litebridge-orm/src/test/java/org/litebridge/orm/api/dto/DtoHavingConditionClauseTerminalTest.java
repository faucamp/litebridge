//package org.litebridge.orm.api.dto;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.TableMetaData;
//import org.litebridge.orm.api.dto.condition.CbDtoConditionClauseTerminal;
//import org.litebridge.orm.api.select.ast.ConditionGroupNode;
//import org.litebridge.orm.api.select.ast.HavingNode;
//import org.litebridge.orm.api.select.impl.DelegatingSelectorInspector;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.persistence.DtoConstructor;
//import org.litebridge.orm.persistence.OrmTable;
//import org.litebridge.orm.persistence.TableRegistry;
//import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
//import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
//import org.litebridge.tracking.ClassFieldAccessorCache;
//
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class DtoHavingConditionClauseTerminalTest {
//
//    @Test
//    void and_query() {
//        // Given
//        final OrmTable ormTable = mock(OrmTable.class);
//        final Table spiTable = new Table("TEST");
//        final TableMetaData metaData = mock(TableMetaData.class);
//        when(ormTable.getMetaData()).thenReturn(metaData);
//        when(metaData.toTable()).thenReturn(spiTable);
//        when(ormTable.dtoClass()).thenReturn((Class) String.class);
//
//        final DtoSelector<String> selector = new DtoSelector<>(
//                String.class,
//                ormTable,
//                mock(TableRegistry.class),
//                mock(ClassFieldAccessorCache.class),
//                mock(DtoConstructor.class),
//                mock(TransactionalDatabaseProvider.class),
//                new NoOpAliasGenerator(),
//                mock(LitebridgeContext.class),
//                null
//        );
//        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);
//
//        // When
//        final DtoHavingConditionClauseTerminal<String> result = terminal.and(q -> {
//            return new CbDtoConditionClauseTerminal<>(ormTable, mock(org.litebridge.orm.engine.FromClauseEngine.class), null);
//        });
//
//        // Then
//        assertNotNull(result);
//        assertInstanceOf(HavingNode.class, DelegatingSelectorInspector.getDelegate(result).node());
//        assertInstanceOf(ConditionGroupNode.class, ((HavingNode) DelegatingSelectorInspector.getDelegate(result).node()).condition());
//    }
//
//    @Test
//    void or_query() {
//        // Given
//        final OrmTable ormTable = mock(OrmTable.class);
//        final Table spiTable = new Table("TEST");
//        final TableMetaData metaData = mock(TableMetaData.class);
//        when(ormTable.getMetaData()).thenReturn(metaData);
//        when(metaData.toTable()).thenReturn(spiTable);
//        when(ormTable.dtoClass()).thenReturn((Class) String.class);
//
//        final DtoSelector<String> selector = new DtoSelector<>(
//                String.class,
//                ormTable,
//                mock(TableRegistry.class),
//                mock(ClassFieldAccessorCache.class),
//                mock(DtoConstructor.class),
//                mock(TransactionalDatabaseProvider.class),
//                new NoOpAliasGenerator(),
//                mock(LitebridgeContext.class),
//                null
//        );
//        final DtoHavingConditionClauseTerminal<String> terminal = new DtoHavingConditionClauseTerminal<>(selector);
//
//        // When
//        final DtoHavingConditionClauseTerminal<String> result = terminal.or(q -> {
//            return new CbDtoConditionClauseTerminal<>(ormTable, mock(org.litebridge.orm.engine.FromClauseEngine.class), null);
//        });
//
//        // Then
//        assertNotNull(result);
//        assertInstanceOf(HavingNode.class, DelegatingSelectorInspector.getDelegate(result).node());
//        assertInstanceOf(ConditionGroupNode.class, ((HavingNode) DelegatingSelectorInspector.getDelegate(result).node()).condition());
//    }
//}
