package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;

public class Subselect extends SubselectExpression {

    private SelectSqlGenerator selectSqlGenerator;

    public Subselect(final Select subselect, final SelectSqlGenerator selectSqlGenerator) {
        super(subselect);
        this.selectSqlGenerator = selectSqlGenerator;
    }

    @Override
    public PreparedSql toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return selectSqlGenerator.prepareSql(subselect, connectionProvider);
    }
}
