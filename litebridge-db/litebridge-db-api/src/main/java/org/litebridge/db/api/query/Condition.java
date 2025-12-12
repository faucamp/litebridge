package org.litebridge.db.api.query;

public interface Condition {

    String getColumn();

    Operator getOperator();

    Object getValue();

}
