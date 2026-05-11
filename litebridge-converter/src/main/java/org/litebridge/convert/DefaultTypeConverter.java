package org.litebridge.convert;

import org.litebridge.convert.converter.Converter;

import java.util.ServiceLoader;

public class DefaultTypeConverter extends ConfigurableTypeConverter {

    public DefaultTypeConverter() {
        ServiceLoader.load(Converter.class).forEach(this::register);
    }
}
