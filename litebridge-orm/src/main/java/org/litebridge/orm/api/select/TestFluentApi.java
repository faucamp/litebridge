package org.litebridge.orm.api.select;

import java.util.Map;

public class TestFluentApi {

    public void test() {
        SelectClause<Map<String, Object>> selectClause = new SelectClause<>();
        selectClause.select("COL1")
                .from("TABLE1")
                .join("TABLE2").on("COL2").eq(123)
                .and("COL3").eq(234)
                .join("TABLE3").on("COL3").eq(345)
                .where("COL1").eq(987)
                .and("COL2").eq(654)
                .and("COL3").eq(999)
                .orderBy("COL1").asc()
                .then("COL2").desc()
                .stream()
                .forEach(System.out::println);


        selectClause.select("COL1")
                .from("TABLE1")
                .join("TABLE2").on("COL2").eq(123)
                .and("COL3").eq(234)
                .join("TABLE3").on("COL3").eq(345)
                .where("COL1").eq(987)
                .and("COL2").eq(654)
                .and("COL3").eq(999)
                .stream()
                .forEach(System.out::println);

        selectClause.select("COL1")
                .from("TABLE1")
                .join("TABLE2").on("COL2").eq(123)
                .and("COL3").eq(234)
                .stream()
                .forEach(System.out::println);


        selectClause.select("COL1")
                .from("TABLE1")
                .where("COL1").eq(987)
                .orderBy("COL1").asc()
                .then("COL2").desc()
                .stream()
                .forEach(System.out::println);
    }
}
