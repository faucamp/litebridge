package org.litebridge.orm.api.select;

import org.litebridge.orm.api.sql.SqlSelector;

import java.util.Map;

public class TestFluentApi {

    public void test() {
        SqlSelector selector = new SqlSelector(null, null);

        selector.select("COL1", "COL2")
                .from("TABLE")
                .join("TABLE2")
                .on("COL2").eq(123)
                .and("COL3")
                .eq(234)
                .join("TABLE3")
                .on("COL3")
                .eq(345)
                .where("COL1")
                .eq(987)
                .and("COL2")
                .eq(654)
                .and("COL3")
                .eq(999)
                .orderBy("COL1")
                .asc()
                .then("COL2")
                .desc();
    }
}
