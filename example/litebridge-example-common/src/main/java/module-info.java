import org.jspecify.annotations.NullMarked;

@NullMarked
module litebridge.example.common {
    requires org.slf4j;
    requires org.jspecify;

    requires litebridge.annotations;
    requires litebridge.db.spi;
    requires litebridge.orm;

    exports org.litebridgedb.example.common;
    exports org.litebridgedb.example.common.dto;
    exports org.litebridgedb.example.common.entity;
    exports org.litebridgedb.example.common.mapping;

    opens org.litebridgedb.example.common.dto to litebridge.orm, litebridge.example.h2.jpms;
    opens org.litebridgedb.example.common.entity to litebridge.orm;
    opens org.litebridgedb.example.common.mapping to litebridge.orm;
}