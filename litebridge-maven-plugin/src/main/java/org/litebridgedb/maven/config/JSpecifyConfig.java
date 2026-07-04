package org.litebridgedb.maven.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.StringJoiner;

/**
 * Configuration for JSpecify annotation behaviour.
 */
public class JSpecifyConfig {

    /**
     * Toggle to annotate generated code for nullability using JSpecify annotations.
     * <p>
     * If {@code true}, generated code will be annotated with JSpecify annotations such as
     * {@code @NullMarked}, {@code @Nullable} and/or {@code @NullUnmarked} to enable nullability
     * checks.
     * <p>
     * If {@code false}, no JSpecify annotations will be added, regardless of other config settings.*
     * <p>
     * JSpecify must be available on the classpath for the generated entities to be used.
     * <p>
     * Default: {@code false}
     *
     * @see <a href="https://jspecify.dev/">JSpecify</a>
     */
    @Parameter(defaultValue = "false")
    private boolean annotate;

    /**
     * Annotate the entity classes/package with {@code @NullMarked}.
     * <p>
     * If {@code true}, generated entity classes will be annotated with {@code @NullMarked}
     * and nullable fields are annotated with {@code @Nullable}.
     * <p>
     * If {@code false}, generated entity classes will be annoted with {@code @NullUnmarked}
     * and no fields are annotated with {@code @Nullable}.
     * <p>
     * This setting is only used if {@code annotate} is {@code true}.
     * <p>
     * Default: {@code true}
     */
    private boolean nullMarked = true;

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

    public boolean isAnnotate() {
        return annotate;
    }

    public void setAnnotate(final boolean annotate) {
        this.annotate = annotate;
    }

    public boolean isNullMarked() {
        return nullMarked;
    }

    public void setNullMarked(final boolean nullMarked) {
        this.nullMarked = nullMarked;
    }

    public boolean isDatabaseNullable() {
        return databaseNullable;
    }

    public void setDatabaseNullable(final boolean databaseNullable) {
        this.databaseNullable = databaseNullable;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JSpecifyConfig.class.getSimpleName() + "[", "]")
                .add("annotate=" + annotate)
                .add("nullMarked=" + nullMarked)
                .add("databaseNullable=" + databaseNullable)
                .toString();
    }
}
