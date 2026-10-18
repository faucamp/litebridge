package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.orm.engine.ast.QueryNode;

public record ManualUpsert(UpdateBuilder updateBuilder, InsertBuilder insertBuilder) implements StatementBuilder {

    @Override
    public Object dto() {
        return updateBuilder.dto();
    }

    @Override
    public QueryNode node() {
        return updateBuilder.node();
    }

    @Override
    public int nodeHash() {
        return updateBuilder.nodeHash();
    }

    @Override
    public StatementChain statementChain() {
        return updateBuilder().statementChain();
    }

    @Override
    public UpdateMetaData createUpdateMetaData(final PreparedOperation preparedOperation) {
        return updateBuilder.createUpdateMetaData(preparedOperation);
    }

    @Override
    public void setField(final String fieldName, @Nullable final Object value) {
        insertBuilder.setField(fieldName, value);
    }

    @Override
    public PreparedOperation build() {
        return updateBuilder.build();
    }
}
