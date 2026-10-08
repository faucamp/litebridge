package org.litebridge.spring.boot.autoconfigure;

import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
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
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.spring.LitebridgeTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LitebridgeFactoryBeanTest {

    @Test
    void getObjectType_standardDatabaseProvider_returnsLitebridgeClass() {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final Class<?> objectType = factoryBean.getObjectType();

        // Then
        assertThat(objectType).isEqualTo(Litebridge.class);
    }

    @Test
    void getObjectType_overrideDatabaseProvider_returnsCustomLitebridgeClass() {
        // Given
        final LitebridgeOverrideDatabaseProvider<LitebridgeCore> databaseProvider = new TestOverrideDatabaseProvider();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final Class<?> objectType = factoryBean.getObjectType();

        // Then
        assertThat(objectType).isEqualTo(LitebridgeCore.class);
    }

    @Test
    void getObjectType_customSubtypeOverrideDatabaseProvider_returnsCustomSubtypeClass() {
        // Given
        final LitebridgeOverrideDatabaseProvider<TestCustomLitebridge> databaseProvider = new TestCustomLitebridgeDatabaseProvider();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final Class<?> objectType = factoryBean.getObjectType();

        // Then
        assertThat(objectType).isEqualTo(TestCustomLitebridge.class);
    }

    @Test
    void isSingleton_returnsTrue() {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final boolean singleton = factoryBean.isSingleton();

        // Then
        assertThat(singleton).isTrue();
    }

    @Test
    void getObject_standardDatabaseProvider_createsLitebridgeInstance() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Litebridge.class);
    }

    @Test
    void getObject_overrideDatabaseProvider_createsLitebridgeCoreInstance() throws Exception {
        // Given
        final LitebridgeOverrideDatabaseProvider<LitebridgeCore> databaseProvider = new TestOverrideDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(LitebridgeCore.class);
        assertThat(result.getClass()).isEqualTo(LitebridgeCore.class);
    }

    @Test
    void getObject_customSubtypeOverrideDatabaseProvider_createsCustomLitebridgeInstance() throws Exception {
        // Given
        final LitebridgeOverrideDatabaseProvider<TestCustomLitebridge> databaseProvider = new TestCustomLitebridgeDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TestCustomLitebridge.class);
    }

    @Test
    void getObject_withScanBasePackages_registersEntities() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        properties.setScanBasePackage(new String[]{"org.litebridge.spring.boot.autoconfigure.test.entity"});
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void getObject_withScanBasePackagesEmpty_doesNotRegisterEntities() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        properties.setScanBasePackage(new String[]{"com.example.nonexistent"});
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void getObject_withNullScanBasePackages_skipsScanning() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        properties.setScanBasePackage(null);
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void getObject_withConfigurer_appliesConfigurer() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        final AtomicBoolean configured = new AtomicBoolean(false);
        final LitebridgeConfigurer configurer = litebridge -> configured.set(true);
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, configurer);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
        assertThat(configured).isTrue();
    }

    @Test
    void getObject_withoutConfigurer_succeeds() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void getObject_withRelatedDtoStrategy_passesStrategyToConfig() throws Exception {
        // Given
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();
        final TransactionManager transactionManager = createTransactionManager();
        final LitebridgeProperties properties = new LitebridgeProperties();
        properties.setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
        final LitebridgeFactoryBean factoryBean = new LitebridgeFactoryBean(databaseProvider, transactionManager, properties, null);

        // When
        final LitebridgeCore result = factoryBean.getObject();

        // Then
        final Field configField = ClassUtils.getField(LitebridgeCore.class, "litebridgeConfig");
        configField.setAccessible(true);
        final LitebridgeConfig config = (LitebridgeConfig) configField.get(result);
        assertThat(config.relatedDtoStrategy()).isEqualTo(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
    }

    private static TransactionManager createTransactionManager() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:lbfactory;DB_CLOSE_DELAY=-1", "sa", "");
        final Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
        return new LitebridgeTransactionManager(dataSource);
    }

    public static class TestCustomLitebridge extends LitebridgeCore {
        public TestCustomLitebridge(final DatabaseProvider databaseProvider,
                                    final TransactionManager transactionManager,
                                    final LitebridgeConfig litebridgeConfig,
                                    final MethodHandles.Lookup lookup) {
            super(databaseProvider, transactionManager, litebridgeConfig, lookup);
        }
    }

    public static class TestDatabaseProvider implements DatabaseProvider {
        private final DatabaseProvider delegate = new org.litebridge.db.h2.H2DatabaseProvider();

        @Override
        public DatabaseProviderMetaData metaData() {
            return delegate.metaData();
        }

        @Override
        public DatabaseMetaData databaseMetaData(final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.databaseMetaData(connectionProvider);
        }

        @Override
        public TableMetaData tableMetaData(final @NonNull Table table, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.tableMetaData(table, connectionProvider);
        }

        @Override
        public <T extends Result> T executeUpdate(final @NonNull PreparedSql preparedSql, final @NonNull Class<T> resultType, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeUpdate(preparedSql, resultType, connectionProvider);
        }

        @Override
        public BatchUpdateResult executeBatch(final @NonNull List<PreparedSql> preparedSql, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeBatch(preparedSql, connectionProvider);
        }

        @Override
        public List<Row> executeQuery(final @NonNull PreparedSql preparedSql, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeQuery(preparedSql, connectionProvider);
        }

        @Override
        public String toSql(final @NonNull Operation operation, final @NonNull ConnectionProvider connectionProvider) {
            return delegate.toSql(operation, connectionProvider);
        }

        @Override
        public TypeConverter typeConverter() {
            return delegate.typeConverter();
        }

        @Override
        public SequenceColumnValueGenerator sequenceColumnValueGenerator(final @NonNull String sequence) throws UnsupportedOperationException {
            return delegate.sequenceColumnValueGenerator(sequence);
        }

        @Override
        public SqlFunctionRegistry sqlFunctionRegistry() {
            return delegate.sqlFunctionRegistry();
        }
    }

    public static class TestOverrideDatabaseProvider extends TestDatabaseProvider implements LitebridgeOverrideDatabaseProvider<LitebridgeCore> {
        public TestOverrideDatabaseProvider() {
        }

        @Override
        public Class<LitebridgeCore> litebridgeClass() {
            return LitebridgeCore.class;
        }
    }

    public static class TestCustomLitebridgeDatabaseProvider extends TestDatabaseProvider implements LitebridgeOverrideDatabaseProvider<TestCustomLitebridge> {
        public TestCustomLitebridgeDatabaseProvider() {
        }

        @Override
        public Class<TestCustomLitebridge> litebridgeClass() {
            return TestCustomLitebridge.class;
        }

        @Override
        public TestCustomLitebridge createLitebridge(final LitebridgeBuilder.ConstructorArgs constructorArgs) {
            return new TestCustomLitebridge(
                    constructorArgs.databaseProvider(),
                    constructorArgs.transactionManager(),
                    constructorArgs.litebridgeConfig(),
                    constructorArgs.lookup()
            );
        }
    }
}
