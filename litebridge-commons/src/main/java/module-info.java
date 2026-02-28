import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Commons
 * <p>
 * Various utilities, custom types, and stream collectors.
 */
@NullMarked
module litebridge.commons {
    requires java.desktop;
    requires org.jspecify;

    exports org.litebridge.commons;
    exports org.litebridge.commons.collector;
    exports org.litebridge.commons.type;
}