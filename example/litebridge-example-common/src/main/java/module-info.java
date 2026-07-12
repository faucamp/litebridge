import org.jspecify.annotations.NullMarked;

@NullMarked
module litebridge.example.common {
    requires org.slf4j;
    requires org.jspecify;

    requires litebridge.annotations;
    requires litebridge.db.spi;
    requires litebridge.orm;

    exports org.litebridge.example.common;
    exports org.litebridge.example.common.dto;
    exports org.litebridge.example.common.entity;
    exports org.litebridge.example.common.mapping;

    opens org.litebridge.example.common.dto to litebridge.orm, litebridge.example.h2.jpms;
    opens org.litebridge.example.common.entity to litebridge.orm;
    opens org.litebridge.example.common.mapping to litebridge.orm;
}