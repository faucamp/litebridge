package org.litebridge.db.spi.impl.engine;

import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecutionEngineReturnedKeysAuto extends AbstractExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionEngineReturnedKeysAuto.class);

    public ExecutionEngineReturnedKeysAuto(final TypeConverter typeConverter, final AliasTransformer aliasTransformer) {
        super(typeConverter, aliasTransformer);
    }

    @Override
    protected PreparedStatement prepareJdbcStatementReturnGeneratedKeys(final UpdateMetaData updateMetaData,
                                                                        final PreparedSql preparedSql,
                                                                        final ManagedConnection connection) throws SQLException {
        return connection.prepareStatement(preparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
