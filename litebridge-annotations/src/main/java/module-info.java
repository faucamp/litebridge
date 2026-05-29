import org.jspecify.annotations.NullMarked;

/**
 * Litebridge ORM entity annotations
 * <p>
 * Optional annotations used to provide metadata to specify entity-table mappings.
 */
@NullMarked
module litebridge.annotations {
    requires org.jspecify;
    requires litebridge.db.spi;

    exports org.litebridgedb.orm.annotation;
}