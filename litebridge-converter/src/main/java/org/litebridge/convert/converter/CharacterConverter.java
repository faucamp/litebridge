package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

public class CharacterConverter extends AbstractStringParsingConverter<Character> implements Converter<Character> {

    @Override
    protected Character convertString(final String value) {
        return value.charAt(0);
    }

    @Override
    public Class<?> type() {
        return Character.class;
    }

    @Override
    public @Nullable Class<?> primitiveType() {
        return char.class;
    }
}
