package org.litebridgedb.orm.nativesql;

import java.util.List;

public record ParsedSql(String sql, List<String> bindParameterNames) {
}
