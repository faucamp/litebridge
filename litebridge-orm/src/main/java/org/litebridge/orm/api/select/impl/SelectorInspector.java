package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;

public final class SelectorInspector {

    private SelectorInspector() {
    }

    public static SelectSpec getSelectSpec(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof DelegatingSelector<?, ?> terminal) {
            return terminal.delegate.selectSpec();
        } else {
            throw new IllegalArgumentException("Unsupported terminal class: " + selectTerminal.getClass());
        }
    }
}
