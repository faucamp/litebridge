package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.BatchUpdateResult;
import org.litebridge.db.spi.update.Result;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.orm.tx.DefaultTransactionManager;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.litebridge.orm.util.DatabaseProviderTestUtil.mockDatabaseProviderWithMetaData;
import static org.mockito.Mockito.mock;

class LitebridgeBuilderTest {

    @Test
    void buildThrowsWhenNeitherTransactionManagerNorDataSourceIsProvided() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final LitebridgeBuilder<Litebridge> builder = new LitebridgeBuilder<>(databaseProvider);

        // When / Then
        final IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("Either a transaction manager or data source must be provided", exception.getMessage());
    }

    @Test
    void buildDefaultLitebridgeWithDataSourceAndDefaultOptions() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final DataSource dataSource = mock(DataSource.class);
        final LitebridgeBuilder<Litebridge> builder = new LitebridgeBuilder<>(databaseProvider);

        // When
        final LitebridgeBuilder<Litebridge> chained = builder.withDataSource(dataSource);
        final Litebridge litebridge = builder.build();

        // Then
        assertSame(builder, chained);
        assertNotNull(litebridge);
        final TransactionalDatabaseProvider txDbProvider = ObjectUtils.getFieldValue(litebridge, "databaseProvider", TransactionalDatabaseProvider.class);
        assertNotNull(txDbProvider);
        assertTrue(txDbProvider.transactionManager() instanceof DefaultTransactionManager);
        final LitebridgeConfig config = ObjectUtils.getFieldValue(litebridge, "litebridgeConfig", LitebridgeConfig.class);
        assertNotNull(config);
    }

    @Test
    void buildDefaultLitebridgeWithExplicitDependencies() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeConfig config = new LitebridgeConfig();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final LitebridgeBuilder<Litebridge> builder = new LitebridgeBuilder<>(databaseProvider);

        // When
        final LitebridgeBuilder<Litebridge> chainedTx = builder.withTransactionManager(transactionManager);
        final LitebridgeBuilder<Litebridge> chainedConfig = builder.withConfig(config);
        final LitebridgeBuilder<Litebridge> chainedLookup = builder.withLookup(lookup);
        final Litebridge litebridge = builder.build();

        // Then
        assertSame(builder, chainedTx);
        assertSame(builder, chainedConfig);
        assertSame(builder, chainedLookup);
        assertNotNull(litebridge);
        final TransactionalDatabaseProvider txDbProvider = ObjectUtils.getFieldValue(litebridge, "databaseProvider", TransactionalDatabaseProvider.class);
        assertNotNull(txDbProvider);
        assertSame(transactionManager, txDbProvider.transactionManager());
        final LitebridgeConfig actualConfig = ObjectUtils.getFieldValue(litebridge, "litebridgeConfig", LitebridgeConfig.class);
        assertSame(config, actualConfig);
    }

    @Test
    void buildWhenBothTransactionManagerAndDataSourceProvidedUsesTransactionManager() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final DataSource dataSource = mock(DataSource.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeBuilder<Litebridge> builder = new LitebridgeBuilder<>(databaseProvider);

        // When
        builder.withDataSource(dataSource);
        builder.withTransactionManager(transactionManager);
        final Litebridge litebridge = builder.build();

        // Then
        assertNotNull(litebridge);
        final TransactionalDatabaseProvider txDbProvider = ObjectUtils.getFieldValue(litebridge, "databaseProvider", TransactionalDatabaseProvider.class);
        assertNotNull(txDbProvider);
        assertSame(transactionManager, txDbProvider.transactionManager());
    }

    @Test
    void buildLitebridgeCoreWithOverrideDatabaseProvider() {
        // Given
        final TestLitebridgeCoreOverrideDatabaseProvider overrideProvider = new TestLitebridgeCoreOverrideDatabaseProvider();
        final DataSource dataSource = mock(DataSource.class);
        final LitebridgeBuilder<LitebridgeCore> builder = new LitebridgeBuilder<>(overrideProvider);

        // When
        builder.withDataSource(dataSource);
        final LitebridgeCore litebridgeCore = builder.build();

        // Then
        assertNotNull(litebridgeCore);
        assertEquals(LitebridgeCore.class, litebridgeCore.getClass());
        final TransactionalDatabaseProvider txDbProvider = ObjectUtils.getFieldValue(litebridgeCore, "databaseProvider", TransactionalDatabaseProvider.class);
        assertNotNull(txDbProvider);
        assertTrue(txDbProvider.transactionManager() instanceof DefaultTransactionManager);
    }

    @Test
    void buildCustomLitebridgeWithOverrideDatabaseProvider() {
        // Given
        final TestCustomLitebridgeOverrideDatabaseProvider overrideProvider = new TestCustomLitebridgeOverrideDatabaseProvider();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeConfig config = new LitebridgeConfig();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final LitebridgeBuilder<TestCustomLitebridge> builder = new LitebridgeBuilder<>(overrideProvider);

        // When
        builder.withTransactionManager(transactionManager)
                .withConfig(config)
                .withLookup(lookup);
        final TestCustomLitebridge customLitebridge = builder.build();

        // Then
        assertNotNull(customLitebridge);
        assertNotNull(overrideProvider.capturedConstructorArgs);
        assertSame(overrideProvider, overrideProvider.capturedConstructorArgs.databaseProvider());
        assertSame(transactionManager, overrideProvider.capturedConstructorArgs.transactionManager());
        assertSame(config, overrideProvider.capturedConstructorArgs.litebridgeConfig());
        assertSame(lookup, overrideProvider.capturedConstructorArgs.lookup());
    }

    @Test
    void constructorArgsRecordContract() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeConfig config = new LitebridgeConfig();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final LitebridgeBuilder.ConstructorArgs args1 = new LitebridgeBuilder.ConstructorArgs(
                databaseProvider,
                transactionManager,
                config,
                lookup);
        final LitebridgeBuilder.ConstructorArgs args2 = new LitebridgeBuilder.ConstructorArgs(
                databaseProvider,
                transactionManager,
                config,
                lookup);
        final LitebridgeBuilder.ConstructorArgs args3 = new LitebridgeBuilder.ConstructorArgs(
                mockDatabaseProviderWithMetaData(),
                transactionManager,
                config,
                lookup);

        // When / Then
        assertSame(databaseProvider, args1.databaseProvider());
        assertSame(transactionManager, args1.transactionManager());
        assertSame(config, args1.litebridgeConfig());
        assertSame(lookup, args1.lookup());
        assertEquals(args1, args2);
        assertEquals(args1.hashCode(), args2.hashCode());
        assertNotEquals(args1, args3);
        assertNotNull(args1.toString());
    }

    @Test
    void createBuilderViaLitebridgeCoreStaticFactoryMethods() {
        // Given
        final DatabaseProvider databaseProvider = mockDatabaseProviderWithMetaData();
        final TestLitebridgeCoreOverrideDatabaseProvider overrideProvider = new TestLitebridgeCoreOverrideDatabaseProvider();
        final DataSource dataSource = mock(DataSource.class);

        // When
        final LitebridgeBuilder<Litebridge> defaultBuilder = LitebridgeCore.withDatabase(databaseProvider);
        final LitebridgeBuilder<LitebridgeCore> overrideBuilder = LitebridgeCore.withDatabase(overrideProvider);

        // Then
        assertNotNull(defaultBuilder);
        assertNotNull(overrideBuilder);

        defaultBuilder.withDataSource(dataSource);
        overrideBuilder.withDataSource(dataSource);

        final Litebridge defaultLitebridge = defaultBuilder.build();
        final LitebridgeCore overrideLitebridge = overrideBuilder.build();

        assertNotNull(defaultLitebridge);
        assertNotNull(overrideLitebridge);
    }

    private static class TestLitebridgeCoreOverrideDatabaseProvider implements LitebridgeOverrideDatabaseProvider<LitebridgeCore> {

        private final DatabaseProvider delegate = mockDatabaseProviderWithMetaData();

        @Override
        public Class<LitebridgeCore> litebridgeClass() {
            return LitebridgeCore.class;
        }

        @Override
        public DatabaseProviderMetaData metaData() {
            return delegate.metaData();
        }

        @Override
        public DatabaseMetaData databaseMetaData(final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.databaseMetaData(connectionProvider);
        }

        @Override
        public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.tableMetaData(table, connectionProvider);
        }

        @Override
        public <T extends Result> T executeUpdate(final PreparedSql preparedSql, final Class<T> resultType, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeUpdate(preparedSql, resultType, connectionProvider);
        }

        @Override
        public BatchUpdateResult executeBatch(final List<PreparedSql> preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeBatch(preparedSql, connectionProvider);
        }

        @Override
        public List<Row> executeQuery(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeQuery(preparedSql, connectionProvider);
        }

        @Override
        public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
            return delegate.toSql(operation, connectionProvider);
        }

        @Override
        public TypeConverter typeConverter() {
            return delegate.typeConverter();
        }

        @Override
        public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
            return delegate.sequenceColumnValueGenerator(sequence);
        }

        @Override
        public SqlFunctionRegistry sqlFunctionRegistry() {
            return delegate.sqlFunctionRegistry();
        }
    }

    private static class TestCustomLitebridge extends LitebridgeCore {

        TestCustomLitebridge(final DatabaseProvider databaseProvider,
                             final TransactionManager transactionManager,
                             final LitebridgeConfig litebridgeConfig,
                             final MethodHandles.Lookup lookup) {
            super(databaseProvider, transactionManager, litebridgeConfig, lookup);
        }
    }

    private static class TestCustomLitebridgeOverrideDatabaseProvider implements LitebridgeOverrideDatabaseProvider<TestCustomLitebridge> {

        private final DatabaseProvider delegate = mockDatabaseProviderWithMetaData();
        private LitebridgeBuilder.ConstructorArgs capturedConstructorArgs;

        @Override
        public Class<TestCustomLitebridge> litebridgeClass() {
            return TestCustomLitebridge.class;
        }

        @Override
        public TestCustomLitebridge createLitebridge(final LitebridgeBuilder.ConstructorArgs constructorArgs) {
            this.capturedConstructorArgs = constructorArgs;
            return new TestCustomLitebridge(
                    constructorArgs.databaseProvider(),
                    constructorArgs.transactionManager(),
                    constructorArgs.litebridgeConfig(),
                    constructorArgs.lookup());
        }

        @Override
        public DatabaseProviderMetaData metaData() {
            return delegate.metaData();
        }

        @Override
        public DatabaseMetaData databaseMetaData(final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.databaseMetaData(connectionProvider);
        }

        @Override
        public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.tableMetaData(table, connectionProvider);
        }

        @Override
        public <T extends Result> T executeUpdate(final PreparedSql preparedSql, final Class<T> resultType, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeUpdate(preparedSql, resultType, connectionProvider);
        }

        @Override
        public BatchUpdateResult executeBatch(final List<PreparedSql> preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeBatch(preparedSql, connectionProvider);
        }

        @Override
        public List<Row> executeQuery(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeQuery(preparedSql, connectionProvider);
        }

        @Override
        public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
            return delegate.toSql(operation, connectionProvider);
        }

        @Override
        public TypeConverter typeConverter() {
            return delegate.typeConverter();
        }

        @Override
        public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
            return delegate.sequenceColumnValueGenerator(sequence);
        }

        @Override
        public SqlFunctionRegistry sqlFunctionRegistry() {
            return delegate.sqlFunctionRegistry();
        }
    }
}
