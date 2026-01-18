package org.litebridge.orm.persistence;

import org.litebridge.commons.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public final class CompositeUpdateResult {

    private final List<DtoUpdateResult> dtoUpdateResults = new ArrayList<>();

    public CompositeUpdateResult add(final DtoUpdateResult dtoUpdateResult) {
        dtoUpdateResults.add(dtoUpdateResult);
        return this;
    }

    public CompositeUpdateResult merge(final CompositeUpdateResult compositeUpdateResult) {
        dtoUpdateResults.addAll(compositeUpdateResult.dtoUpdateResults);
        return this;
    }

    public List<DtoUpdateResult> results() {
        return dtoUpdateResults;
    }

    public DtoUpdateResult primary() {
        return CollectionUtils.requireNonEmpty(dtoUpdateResults, "Update results not set").getFirst();
    }
}
