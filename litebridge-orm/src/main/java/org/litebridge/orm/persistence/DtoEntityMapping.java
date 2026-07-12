package org.litebridge.orm.persistence;

import org.litebridge.orm.api.spec.FieldSpec;

import java.util.Map;

public record DtoEntityMapping(Class<?> entityClass, Map<FieldSpec, FieldSpec> dtoEntityFieldMap) {
}
