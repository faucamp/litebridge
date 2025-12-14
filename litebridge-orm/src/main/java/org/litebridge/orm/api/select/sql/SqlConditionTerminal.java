package org.litebridge.orm.api.select.sql;

import org.litebridge.orm.api.select.Condition;
import org.litebridge.orm.api.select.ConditionTerminal;
import org.litebridge.orm.api.select.SelectorTerminal;

import java.util.Map;

public interface SqlConditionTerminal extends ConditionTerminal<Map<String, Object>, SqlConditionTerminal> {

    Condition<Map<String, Object>, SqlConditionTerminal> and(final String column);

    <DTO> SelectorTerminal<DTO> mapToDto(Class<DTO> dtoClass);
}
