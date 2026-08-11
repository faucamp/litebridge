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
    requires java.management;
    requires java.xml.crypto;

    exports org.litebridge.orm;
    exports org.litebridge.orm.api.condition;
    exports org.litebridge.orm.api.delete;
    exports org.litebridge.orm.api.delete.model;
    exports org.litebridge.orm.api.dto;
    exports org.litebridge.orm.api.dto.delete;
    exports org.litebridge.orm.api.dto.update;
    exports org.litebridge.orm.api.insert.model;
    exports org.litebridge.orm.api.register;
    exports org.litebridge.orm.api.select;
    exports org.litebridge.orm.api.select.ast;
    exports org.litebridge.orm.api.select.model;
    exports org.litebridge.orm.api.spec;
    exports org.litebridge.orm.api.sql;
    exports org.litebridge.orm.api.sql.delete;
    exports org.litebridge.orm.api.sql.update;
    exports org.litebridge.orm.api.tx;
    exports org.litebridge.orm.api.update;
    exports org.litebridge.orm.api.update.model;
    exports org.litebridge.orm.config;
    exports org.litebridge.orm.engine;
    exports org.litebridge.orm.exception;
    exports org.litebridge.orm.expression;
    exports org.litebridge.orm.expression.function.aggregate;
    exports org.litebridge.orm.expression.function.date;
    exports org.litebridge.orm.expression.function.scalar;
    exports org.litebridge.orm.expression.intent;
    exports org.litebridge.orm.expression.select;
    exports org.litebridge.orm.meta;
    exports org.litebridge.orm.nativesql;
    exports org.litebridge.orm.persistence;
    exports org.litebridge.orm.persistence.alias;
    exports org.litebridge.orm.tx;

    opens org.litebridge.orm to litebridge.tracking;
    opens org.litebridge.orm.api.dto to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.persistence to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.persistence.alias to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.api.select to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.api.select.ast to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.api.select.model to litebridge.commons, litebridge.tracking;
    opens org.litebridge.orm.engine to litebridge.commons, litebridge.tracking;
    exports org.litebridge.orm.api.dto.condition;
    opens org.litebridge.orm.api.dto.condition to litebridge.commons, litebridge.tracking;
}