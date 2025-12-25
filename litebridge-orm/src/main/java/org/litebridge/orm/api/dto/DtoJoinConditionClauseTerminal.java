//package org.litebridge.orm.api.dto;
//
//import org.litebridge.orm.api.select.JoinClause;
//import org.litebridge.orm.api.select.JoinConditionClause;
//import org.litebridge.orm.api.select.JoinConditionClauseTerminal;
//import org.litebridge.orm.api.select.impl.AbstractSelector;
//import org.litebridge.orm.api.select.impl.JoinConditionClauseTerminalImpl;
//import org.litebridge.orm.api.select.model.JoinSpec;
//
//public class DtoJoinConditionClauseTerminal<DTO,
//        JC extends JoinClause<DTO, JCC, DtoJoinConditionClauseTerminal<DTO, JC, DtoJoinConditionClauseTerminal>>,
//        JCC extends JoinConditionClause<DTO, JCC, DtoJoinConditionClauseTerminal>>
//
//        extends JoinConditionClauseTerminalImpl<DTO, JC, JCC, DtoJoinConditionClauseTerminal> {
//
//    public DtoJoinConditionClauseTerminal(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
//        super(joinSpec, delegate);
//    }
//}
