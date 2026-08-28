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
        final String accountId = tableMapper.transformColumnName("ACCOUNT_ID");
        final String accountName = tableMapper.transformColumnName("ACCOUNT_NAME");
        final String balance = tableMapper.transformColumnName("BALANCE");
        final String personId = tableMapper.transformColumnName("PERSON_ID");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        for (int j = 0; j < 10; j++) {
            final int id = j + 1;
            litebridge.insert(personTable, i -> i
                    .into(firstName, surname, age)
                    .values("Name" + id, "Surname" + id, id));
        }

        for (int j = 0; j < 9; j++) {
            final int id = j + 1;
            litebridge.insert(accountTable, i -> i
                    .into(accountId, accountName, balance, personId)
                    .values(id, "Account" + id, BigInteger.valueOf(id), id));
        }

        final UpdateResult updateResult = litebridge.mergeInto(accountTable, m -> m
                .using(personTable)
                .on(Fn.c(accountTable, accountId)).eq(Fn.c(personTable, personId))
                .whenMatched(q -> q.and(accountId).lt(5),
                        u -> u.update(account ->
                                account.set(balance).to(500)))
                .whenMatched(MergeUpdateStep::delete)
                .whenNotMatched(i ->
                        i.insert(accountId, accountName, balance, personId)
                                .values(123L, "Default Account", 0, 1L)));

        assertEquals(10, updateResult.rowsAffected());

        final int count = litebridge.select(Fn.convert(Fn.count(), int.class)).from(Account.class).oneOrThrow();
        assertEquals(5, count);

        final List<Row> accountRows = litebridge.select(
                        Fn.convert(Fn.c(accountId), int.class),
                        Fn.convert(Fn.c(balance), int.class))
                .from(accountTable)
                .list();
        assertEquals(5, accountRows.size());

        for (int i = 1; i <= 4; i++) {
            final int id = i;
            assertTrue(accountRows.stream().anyMatch(row -> {
                final Row.RowColumn accountIdCol = row.column(accountId).orElseThrow();
                final Row.RowColumn balanceCol = row.column(balance).orElseThrow();
                return accountIdCol.value().equals(id) && balanceCol.value().equals(500);
            }));
        }

        assertTrue(accountRows.stream().anyMatch(row -> {
            final Row.RowColumn accountIdCol = row.column(accountId).orElseThrow();
            final Row.RowColumn balanceCol = row.column(balance).orElseThrow();
            return accountIdCol.value().equals(123) && balanceCol.value().equals(0);
        }));
    }
}
