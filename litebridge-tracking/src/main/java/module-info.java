import org.jspecify.annotations.NullMarked;

/**
 * Litebridge Change Tracker
 * <p>
 * Provides field change tracking for plain Java objects.
 */
@NullMarked
module litebridge.tracking {
    requires java.desktop;
    requires org.jspecify;
    requires org.slf4j;
    requires litebridge.commons;

    exports org.litebridge.tracking;

    opens org.litebridge.tracking to litebridge.commons;
}