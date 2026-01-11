package org.litebridge.orm.persistence;

import org.litebridge.tracking.FieldAccessorChain;

import java.util.ArrayList;
import java.util.List;

public class FieldAccessorChainLink {

    private final List<FieldAccessorChain> fieldAccessorChains;

    public FieldAccessorChainLink() {
        this.fieldAccessorChains = new ArrayList<>();
    }

    public FieldAccessorChainLink add(final FieldAccessorChain fieldAccessorChain) {
        fieldAccessorChains.add(fieldAccessorChain);
        return this;
    }

    public List<FieldAccessorChain> fieldAccessorChains() {
        return fieldAccessorChains;
    }
}
