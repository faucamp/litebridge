package org.litebridgedb.orm.config;

import java.util.Objects;

public class LitebridgeConfig {

    private RelatedDtoStrategy relatedDtoStrategy = RelatedDtoStrategy.NULL_IF_NO_JOIN;

    public LitebridgeConfig() {
    }

    public LitebridgeConfig(final LitebridgeConfig other) {
        this.relatedDtoStrategy = other.relatedDtoStrategy;
    }

    public RelatedDtoStrategy getRelatedDtoStrategy() {
        return relatedDtoStrategy;
    }

    public void setRelatedDtoStrategy(final RelatedDtoStrategy relatedDtoStrategy) {
        this.relatedDtoStrategy = Objects.requireNonNull(relatedDtoStrategy, "relatedDtoStrategy must not be null");
    }
}
