import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Spring integration for Spring Framework 6 and Spring Boot 3
 */
@NullMarked
module litebridge.spring {
    requires org.jspecify;
    requires spring.data.commons;

    requires transitive litebridge.spring.common;
    requires litebridge.orm;

    exports org.litebridge.spring.impl;

    opens org.litebridge.spring.impl to spring.core, spring.beans, spring.context, litebridge.orm;
}
