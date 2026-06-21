import org.jspecify.annotations.NullMarked;

/**
 * Litebridge ORM
 */
@NullMarked
module litebridge.orm {
    requires java.sql;
    requires org.jspecify;
    requires org.slf4j;
    requires litebridge.annotations;
    requires litebridge.commons;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.tracking;

    exports org.litebridgedb.orm;
    exports org.litebridgedb.orm.api.delete;
    exports org.litebridgedb.orm.api.dto;
    exports org.litebridgedb.orm.api.dto.delete;
    exports org.litebridgedb.orm.api.dto.update;
    exports org.litebridgedb.orm.api.register;
    exports org.litebridgedb.orm.api.select;
    exports org.litebridgedb.orm.api.select.model;
    exports org.litebridgedb.orm.api.spec;
    exports org.litebridgedb.orm.api.sql;
    exports org.litebridgedb.orm.api.sql.delete;
    exports org.litebridgedb.orm.api.sql.update;
    exports org.litebridgedb.orm.api.tx;
    exports org.litebridgedb.orm.api.update;
    exports org.litebridgedb.orm.api.update.model;
    exports org.litebridgedb.orm.config;
    exports org.litebridgedb.orm.engine;
    exports org.litebridgedb.orm.expression;
    exports org.litebridgedb.orm.expression.function.aggregate;
    exports org.litebridgedb.orm.expression.function.date;
    exports org.litebridgedb.orm.expression.function.scalar;
    exports org.litebridgedb.orm.expression.select;
    exports org.litebridgedb.orm.persistence;
    exports org.litebridgedb.orm.tx;

    opens org.litebridgedb.orm to litebridge.tracking;
    opens org.litebridgedb.orm.api.dto to litebridge.commons, litebridge.tracking;
    opens org.litebridgedb.orm.persistence to litebridge.commons, litebridge.tracking;
    opens org.litebridgedb.orm.persistence.alias to litebridge.commons, litebridge.tracking;
    opens org.litebridgedb.orm.api.select to litebridge.commons, litebridge.tracking;

    opens org.litebridgedb.orm.engine to litebridge.commons, litebridge.tracking;
}