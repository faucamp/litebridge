package org.litebridgedb.orm.api.select.model;

import java.util.function.Consumer;

@FunctionalInterface
public interface ConditionGroupSpecBuilder extends Consumer<ConditionGroupSpec> {
}
