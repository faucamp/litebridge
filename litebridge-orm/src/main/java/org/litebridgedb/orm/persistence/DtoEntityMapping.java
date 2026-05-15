package org.litebridgedb.orm.persistence;

import org.litebridgedb.orm.api.spec.FieldSpec;

import java.util.Map;

public record DtoEntityMapping(Class<?> entityClass, Map<FieldSpec, FieldSpec> dtoEntityFieldMap) {
}
