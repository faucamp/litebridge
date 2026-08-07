package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.model.SelectSpec;

public final class DelegatingSelectorInspector {

    private DelegatingSelectorInspector() {
    }

    public static <DTO, SSP extends SelectSpec> AbstractSelector<DTO, SSP> getDelegate(final DelegatingSelector<DTO, SSP> delegatingSelector) {
        return delegatingSelector.delegate();
    }
}
