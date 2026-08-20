package org.litebridge.orm.e2e.sql;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.MergeUpdateStep;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.expression.Fn;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MultiDbTestExtension.class)
public class SqlMergeE2eTest extends AbstractE2eTest {

    @TestTemplate
    public void merge(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final String accountTable = tableMapper.qualifyName("ACCOUNT");
        final String personTable = tableMapper.qualifyName("PERSON");
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        for (int j = 0; j < 10; j++) {
            final int id = j + 1;
            litebridge.insert(personTable, i -> i
                    .into("FIRST_NAME", "SURNAME", "AGE")
                    .values("Name" + id, "Surname" + id, id));
        }

        for (int j = 0; j < 9; j++) {
            final int id = j + 1;
            litebridge.insert(accountTable, i -> i
                    .into("ACCOUNT_ID", "ACCOUNT_NAME", "BALANCE", "PERSON_ID")
                    .values(id, "Account" + id, BigInteger.valueOf(id), id));
        }

        final UpdateResult updateResult = litebridge.mergeInto(accountTable, m -> m
                .using(personTable)
                .on(Fn.c(accountTable, "ACCOUNT_ID")).eq(Fn.c(personTable, "PERSON_ID"))
                .whenMatched(q -> q.and("ACCOUNT_ID").lt(5),
                        u -> u.update(account ->
                                account.set("BALANCE").to(500)))
                .whenMatched(MergeUpdateStep::delete)
                .whenNotMatched(i ->
                        i.insert("ACCOUNT_ID", "ACCOUNT_NAME", "BALANCE", "PERSON_ID")
                                .values(123L, "Default Account", 0, 1L)));

        assertEquals(10, updateResult.rowsAffected());

        final int count = litebridge.select(Fn.convert(Fn.count(), int.class)).from(Account.class).oneOrThrow();
        assertEquals(5, count);

        final String accountIdCol = tableMapper.transformColumnName("ACCOUNT_ID");
        final String balanceCol = tableMapper.transformColumnName("BALANCE");
        final List<Row> accountRows = litebridge.select(
                        Fn.convert(Fn.c(accountIdCol), int.class),
                        Fn.convert(Fn.c(balanceCol), int.class))
                .from(accountTable)
                .list();
        assertEquals(5, accountRows.size());

        for (int i = 1; i <= 4; i++) {
            final int id = i;
            assertTrue(accountRows.stream().anyMatch(row -> {
                final Row.RowColumn accountId = row.column(accountIdCol).orElseThrow();
                final Row.RowColumn balance = row.column(balanceCol).orElseThrow();
                return accountId.value().equals(id) && balance.value().equals(500);
            }));
        }

        assertTrue(accountRows.stream().anyMatch(row -> {
            final Row.RowColumn accountId = row.column(accountIdCol).orElseThrow();
            final Row.RowColumn balance = row.column(balanceCol).orElseThrow();
            return accountId.value().equals(123) && balance.value().equals(0);
        }));
    }
}
