import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Commons
 * <p>
 * Various utilities, custom types, and stream collectors.
 */
@NullMarked
module litebridge.commons {
    requires org.jspecify;

    exports org.litebridgedb.commons;
    exports org.litebridgedb.commons.collector;
    exports org.litebridgedb.commons.type;
}