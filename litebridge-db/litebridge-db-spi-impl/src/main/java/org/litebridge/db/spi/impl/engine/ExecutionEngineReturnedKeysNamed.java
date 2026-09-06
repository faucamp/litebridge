package org.litebridge.db.spi.impl.engine;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ExecutionEngineReturnedKeysNamed extends AbstractExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionEngineReturnedKeysNamed.class);

    public ExecutionEngineReturnedKeysNamed(final TypeConverter typeConverter, final AliasTransformer aliasTransformer) {
        super(typeConverter, aliasTransformer);
    }

    @Override
    protected PreparedStatement prepareJdbcStatementReturnGeneratedKeys(final UpdateMetaData updateMetaData,
                                                                        final PreparedSql preparedSql,
                                                                        final ManagedConnection connection) throws SQLException {
        final String[] generatedKeyNames = updateMetaData.generatedKeys().stream()
                .map(ColumnMetaData::name)
                .toArray(String[]::new);

        return connection.prepareStatement(preparedSql.sql(), generatedKeyNames);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
