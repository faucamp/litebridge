import org.jspecify.annotations.NullMarked;

/**
 * Litebridge ORM
 */
@NullMarked
module litebridge.orm {
    requires java.sql;
    requires org.jspecify;
    requires org.slf4j;
    requires litebridge.commons;
    requires litebridge.db.spi;
    requires litebridge.tracking;

    exports org.litebridge.orm;

    // Unit test support
    opens org.litebridge.orm to litebridge.tracking;
    opens org.litebridge.orm.api.dto to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.api.select to litebridge.tracking;
    opens org.litebridge.orm.api.select.impl to litebridge.commons;
    opens org.litebridge.orm.persistence to litebridge.commons, litebridge.tracking;
}