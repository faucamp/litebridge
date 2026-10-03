package org.litebridge.orm.e2e.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.RowColumn;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeInspector;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.Fn;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(MultiDbTestExtension.class)
public class SqlMergeE2eTest extends AbstractE2eTest {

    @TestTemplate
    @DisplayName("SQL merge using table")
    public void merge_usingTable(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;

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
                .whenMatched(u -> u
                        .update(account -> account
                                .set(balance).to(500)
                                .where(accountId).lt(5)))
                .whenMatched(account -> account
                        .delete(d -> d
                                .where(accountId).gte(5)))
                .whenNotMatched(i -> i
                        .insert(accountId, accountName, balance, personId)
                        .values(123L, "Default Account", 0, 1L)));

        final boolean isOracle = "Oracle".equals(dbEnv.getName());
        assertEquals(isOracle ? 5 : 10, updateResult.rowsAffected());

        final int count = litebridge.select(Fn.convert(Fn.count(), int.class)).from(Account.class).oneOrThrow();
        assertEquals(isOracle ? 10 : 5, count);

        final List<Row> accountRows = litebridge.select(
                        Fn.convert(Fn.c(accountId), int.class),
                        Fn.convert(Fn.c(balance), int.class))
                .from(accountTable)
                .list();
        assertEquals(isOracle ? 10 : 5, accountRows.size());

        for (int i = 1; i <= 4; i++) {
            final int id = i;
            assertTrue(accountRows.stream().anyMatch(row -> {
                final RowColumn accountIdCol = row.column(accountId);
                final RowColumn balanceCol = row.column(balance);
                return accountIdCol.value().equals(id) && balanceCol.value().equals(500);
            }));
        }

        assertTrue(accountRows.stream().anyMatch(row -> {
            final RowColumn accountIdCol = row.column(accountId);
            final RowColumn balanceCol = row.column(balance);
            return accountIdCol.value().equals(123) && balanceCol.value().equals(0);
        }));
    }

    @TestTemplate
    @DisplayName("SQL merge using table, complex ON conditions")
    public void merge_usingTable_complexOnConditions(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;

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

        // Merge on condition with chained nested subconditions
//        {
//            final UpdateResult updateResult = litebridge.mergeInto(accountTable, m -> m
//                    .using(personTable)
//                    .on(Fn.c(accountTable, accountId)).eq(Fn.c(personTable, personId))
//                    .and(q -> q
//                            .where(accountId).lte(99)
//                            .or(accountId).gte(101))
//                    .whenMatched(u -> u
//                            .update(account -> account
//                                    .set(balance).to(500)
//                                    .where(accountId).lt(5)))
//                    .whenNotMatched(i -> i
//                            .insert(accountId, accountName, balance, personId)
//                            .values(123L, "Default Account", 0, 1L)));
//
//            assertEquals(5, updateResult.rowsAffected());
//
//            final int count = litebridge.select(Fn.convert(Fn.count(), int.class)).from(Account.class).oneOrThrow();
//            assertEquals(10, count);
//        }

        // Merge on nested subconditions
        {
            final UpdateResult updateResult = litebridge.mergeInto(accountTable, m -> m
                    .using(personTable)
                    .on(q -> q
                            .where(Fn.c(accountTable, accountId)).eq(Fn.c(personTable, personId))
                            .or(q2 -> q2
                                    .where(Fn.c(accountTable, accountId)).eq(Fn.c(personTable, personId))
                                    .and(q3 -> q3
                                            .where(accountId).lte(99)
                                            .or(accountId).gte(101))))
                    .whenMatched(u -> u
                            .update(account -> account
                                    .set(balance).multiply(2)
                                    .where(accountId).lt(5))));

            assertEquals(4, updateResult.rowsAffected());
        }
    }

    @TestTemplate
    @DisplayName("SQL merge using query")
    public void merge_usingQuery(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;

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

        for (int j = 0; j < 5; j++) {
            final int id = j + 1;
            litebridge.insert(accountTable, i -> i
                    .into(accountId, accountName, balance, personId)
                    .values(id, "Account" + id, BigInteger.valueOf(id), id));
        }

        litebridge.select().from("LB.PERSON").list();

        final UpdateResult updateResult = litebridge.mergeInto(personTable, m -> m
                .using(q -> q
                        .select(accountId)
                        .from(accountTable))
                .on(accountId).eq(Fn.c(personTable, personId))
                .and(accountId).gte(1)
                .whenMatched(u -> u
                        .update(person -> person
                                .set(firstName).to("Updated Name")
                                .set(surname).to("Updated Surname")
                                .set(age).to(100))));

        assertEquals(5, updateResult.rowsAffected());
    }

    @TestTemplate
    @DisplayName("SQL merge: upsert using SELECT")
    public void merge_upsert_subquery(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;

        final String personTable = tableMapper.qualifyName("PERSON");
        final String personId = tableMapper.transformColumnName("PERSON_ID");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);
        final QueryPlanCache queryPlanCache = LitebridgeInspector.getQueryPlanCache(litebridge);
        int prevCacheSize = queryPlanCache.size();

        // Insert a row that does not exist by selecting literals in the USING clause
        {
            final UpdateResult insertResult = litebridge.mergeInto(personTable, m -> m
                    .using(Fn.alias(q -> q
                                    .select(Fn.literal(123).as(personId)),
                            "X"))
                    .on(personId).eq(Fn.aliasRef("X", personId))
                    .whenMatched(u -> u
                            .update(person -> person
                                    .set(firstName).to("Updated Name")
                                    .set(surname).to("Updated Surname")))
                    .whenNotMatched(i -> i
                            .insert(personId, firstName, surname, age)
                            .values(123, "Inserted Name", "Inserted Surname", 30)));

            assertEquals(1, insertResult.rowsAffected());
            assertEquals(prevCacheSize + 1, queryPlanCache.size());
            final Row row = litebridge.select().from(personTable).where(personId).eq(123).oneOrThrow();
            assertEquals("Inserted Name", row.value(firstName));
            assertEquals("Inserted Surname", row.value(surname));
            assertEquals(30, ((Number) row.value(age)).intValue());
            prevCacheSize = queryPlanCache.size();
        }

        // Update an existing row with the same merge statement
        {
            final UpdateResult updateResult = litebridge.mergeInto(personTable, m -> m
                    .using(Fn.alias(q -> q
                                    .select(Fn.literal(123).as(personId)),
                            "X"))
                    .on(personId).eq(Fn.aliasRef("X", personId))
                    .whenMatched(u -> u
                            .update(person -> person
                                    .set(firstName).to("Updated Name")
                                    .set(surname).to("Updated Surname")))
                    .whenNotMatched(i -> i
                            .insert(personId, firstName, surname, age)
                            .values(123, "Inserted Name", "Inserted Surname", 30)));

            assertEquals(1, updateResult.rowsAffected());
            assertEquals(prevCacheSize, queryPlanCache.size());
            final Row row = litebridge.select().from(personTable).where(personId).eq(123).oneOrThrow();
            assertEquals("Updated Name", row.value(firstName));
            assertEquals("Updated Surname", row.value(surname));
            assertEquals(30, ((Number) row.value(age)).intValue());
        }
    }

    @TestTemplate
    @DisplayName("SQL merge: upsert using VALUES")
    public void merge_upsert_values(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;

        final String personTable = tableMapper.qualifyName("PERSON");
        final String personId = tableMapper.transformColumnName("PERSON_ID");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);
        final QueryPlanCache queryPlanCache = LitebridgeInspector.getQueryPlanCache(litebridge);
        int prevCacheSize = queryPlanCache.size();

        // Insert a row that does not exist using direct values (or selecting literals if the database provider doesn't support the VALUES clause)
        {
            final UpdateResult insertResult = litebridge.mergeInto(personTable, m -> m
                    .using(Fn.values("newPerson", "id", 123))
                    .on(personId).eq(Fn.aliasRef("newPerson", "id"))
                    .whenMatched(u -> u
                            .update(person -> person
                                    .set(firstName).to("Updated Name")
                                    .set(surname).to("Updated Surname")))
                    .whenNotMatched(i -> i
                            .insert(personId, firstName, surname, age)
                            .values(123, "Inserted Name", "Inserted Surname", 30)));

            assertEquals(1, insertResult.rowsAffected());
            assertEquals(prevCacheSize + 1, queryPlanCache.size());
            final Row row = litebridge.select().from(personTable).where(personId).eq(123).oneOrThrow();
            assertEquals("Inserted Name", row.value(firstName));
            assertEquals("Inserted Surname", row.value(surname));
            assertEquals(30, ((Number) row.value(age)).intValue());
            prevCacheSize = queryPlanCache.size();
        }

        // Update an existing row with the same merge statement
        {
            final UpdateResult updateResult = litebridge.mergeInto(personTable, m -> m
                    .using(Fn.values("newPerson", "id", 123))
                    .on(personId).eq(Fn.aliasRef("newPerson", "id"))
                    .whenMatched(u -> u
                            .update(person -> person
                                    .set(firstName).to("Updated Name")
                                    .set(surname).to("Updated Surname")))
                    .whenNotMatched(i -> i
                            .insert(personId, firstName, surname, age)
                            .values(123, "Inserted Name", "Inserted Surname", 30)));

            assertEquals(1, updateResult.rowsAffected());
            assertEquals(prevCacheSize, queryPlanCache.size());
            final Row row = litebridge.select().from(personTable).where(personId).eq(123).oneOrThrow();
            assertEquals("Updated Name", row.value(firstName));
            assertEquals("Updated Surname", row.value(surname));
            assertEquals(30, ((Number) row.value(age)).intValue());
        }
    }
}
