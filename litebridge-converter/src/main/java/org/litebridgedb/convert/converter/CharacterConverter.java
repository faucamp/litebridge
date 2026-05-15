package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

/**
 * A converter for {@link Character} values.
 * <p>
 * Converts values by taking the first character of their string representation.
 */
public class CharacterConverter extends AbstractStringParsingConverter<Character> implements Converter<Character> {

    @Override
    protected Character convertString(final String value) {
        return value.charAt(0);
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Character}.class
     */
    @Override
    public Class<?> type() {
        return Character.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code char.class}
     */
    @Override
    public @Nullable Class<?> primitiveType() {
        return char.class;
    }
}
