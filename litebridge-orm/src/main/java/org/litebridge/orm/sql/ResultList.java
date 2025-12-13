package org.litebridge.orm.sql;

import java.util.List;
import java.util.Map;

public class ResultList {

    private final List<Map<String, Object>> resultList;

    public ResultList(final List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }
}
