import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Change Tracker
 * <p>
 * Provides field change tracking for plain Java objects.
 */
@NullMarked
module litebridge.tracking {
    requires org.jspecify;
    requires org.slf4j;
    requires litebridge.commons;

    exports org.litebridgedb.tracking;

    opens org.litebridgedb.tracking to litebridge.commons;
}