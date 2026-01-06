package org.litebridge.db.spi.update;

import java.util.List;

public record StatementGroup(UpdateStatement statement, List<UpdateStatement> dependentStatemnts)
        implements UpdateStatement {

}
