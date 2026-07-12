import org.jspecify.annotations.NullMarked;

/**
 * Supporting utilities for Litebridge ORM.
 * <p>
 * This module contains classpath-scanning utilities that support the Litebridge ORM framework
 * by allowing automatic registration of Litebridge entities or DTO-table mappings found on the classpath.
 * <p>
 * The utilities in this package are designed to simplify the process of integrating ORM functionality into applications,
 * reducing boilerplate code and enhancing maintainability.
 * <p>
 * However, they rely on external libraries that could conflict with existing dependencies in a
 * client application; thus they are not included as part of the core ORM module.
 */
@NullMarked
module litebridge.orm.support {
    requires io.github.classgraph;
    requires litebridge.annotations;
    requires litebridge.commons;
    requires litebridge.orm;
    requires org.jspecify;

    exports org.litebridge.orm.support;
}