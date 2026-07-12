package org.litebridge.maven.config.reverse;

import org.litebridge.maven.config.JSpecifyConfig;

import java.util.StringJoiner;

/**
 * Configuration for reverse engineering JSpecify annotation behaviour.
 */
public class RevEngJSpecifyConfig extends JSpecifyConfig {

    /**
     * Annotate fields as @{code @Nullable} according to the database column's {@code NULLABLE} attribute.
     * <p>
     * <ul>
     *     <li>
     *         If {@code true}, generated entity fields are annotated as @Nullable if:
     *         <ul>
     *             <li>the database column's {@code NULLABLE} attribute is {@code true}</li>
     *             <li>the mapped field type is not a primitive</li>
     *         </ul>
     *         This is useful if entities are only ever read fully-complete from the database.
     *         It may result in some fields being {@code null} at runtime (but not marked as {@code @Nullable})
     *         when retrieving partial entites or constructing new ones in Java.
     *     </li>
     *     <li>
     *         If {@code false}, all non-primitive fields are annotated as {@code @Nullable}.
     *         However, setter method input parameters still retain their database nullability:
     *         they are marked {@code @Nullable} only if the mapped database column's {@code NULLABLE}
     *         attribute is {@code true}.
     *     </li>
     * </ul>
     * <p>
     * Default: {@code false}
     * <p>
     * This setting is only used if {@code annotate} and {@code nullMarked} are both {@code true}.
     */
    private boolean databaseNullable;

    public boolean isDatabaseNullable() {
        return databaseNullable;
    }

    public void setDatabaseNullable(final boolean databaseNullable) {
        this.databaseNullable = databaseNullable;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RevEngJSpecifyConfig.class.getSimpleName() + "[", "]")
                .add("annotate=" + isAnnotate())
                .add("nullMarked=" + isNullMarked())
                .add("databaseNullable=" + databaseNullable)
                .toString();
    }
}
