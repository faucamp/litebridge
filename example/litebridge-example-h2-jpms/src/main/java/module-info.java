import org.jspecify.annotations.NullMarked;

@NullMarked
module litebridge.example.h2.jpms {
    requires flyway.core;
    requires java.sql;
    requires org.jspecify;
    requires org.slf4j;

    requires litebridge.db.h2;
    requires litebridge.orm;
    requires litebridge.example.common;

    exports org.litebridgedb.example.h2.jpms;

    // Flyway classpath scanning
    opens db.migration;
}