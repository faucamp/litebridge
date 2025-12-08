package org.litebridge.db.api.convert;

public interface DatabaseValueConverter {

    Object convert(Object value, int dbDataType);
}
