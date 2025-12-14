package org.litebridge.orm.sql;

import org.litebridge.orm.persistence.Condition;
import org.litebridge.orm.persistence.ConditionTerminal;
import org.litebridge.orm.persistence.SelectorTerminal;

import java.util.Map;

public interface SqlConditionTerminal extends ConditionTerminal<Map<String, Object>, SqlConditionTerminal> {

    Condition<Map<String, Object>, SqlConditionTerminal> and(final String column);

    <DTO> SelectorTerminal<DTO> mapToDto(Class<DTO> dtoClass);
}
