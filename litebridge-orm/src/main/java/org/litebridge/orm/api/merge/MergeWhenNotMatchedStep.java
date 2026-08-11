package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.select.ast.QueryNode;

import java.util.function.Function;

public class MergeWhenNotMatchedStep<DTO> extends MergeTerminal {

    public MergeWhenNotMatchedStep(final QueryNode node) {
        super(node);
    }

    public MergeTerminal whenNotMatched(final Function<MergeInsertStep, MergeTerminal> insert) {
        return this;
    }
}
