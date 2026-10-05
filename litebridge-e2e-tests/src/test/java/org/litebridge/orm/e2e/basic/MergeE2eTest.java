package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeInspector;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.meta.AccountMeta;
import org.litebridge.orm.e2e.basic.meta.PersonMeta;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.Fn;

import java.math.BigInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(MultiDbTestExtension.class)
public class MergeE2eTest extends AbstractE2eTest {

    @TestTemplate
    public void merge(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;

        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        final Person[] persons = new Person[10];

        for (int i = 0; i < persons.length; i++) {
            persons[i] = new Person();
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
            persons[i].setAge(i);
        }

        final Account[] accounts = new Account[9];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account();
            accounts[i].setName("Account" + i);
            accounts[i].setBalance(BigInteger.valueOf(i + 1));
            accounts[i].setOwner(persons[i]);
        }

        litebridge.saveAll(persons);
        litebridge.saveAll(accounts);
        litebridge.select().from("LB.ACCOUNT").list();
        litebridge.select().from("LB.PERSON").list();

        // Merge with: "USING <dto>", "WHEN MATCHED AND <update>", "WHEN MATHED AND <delete>", "WHEN NOT MATCHED <insert values directly>"
        {
            final UpdateResult result = litebridge.mergeInto(Account.class, m -> m
                    .using(Person.class)
                    .on(AccountMeta.id).eq(PersonMeta.id)
                    .whenMatched(u -> u
                            .update(account -> account
                                    .set(AccountMeta.balance).to(500)
                                    .where(AccountMeta.id).lt(5)))
                    .whenMatched(account -> account
                            .delete(d -> d
                                    .where(AccountMeta.id).gte(5)))
                    .whenNotMatched(i -> i
                            .insert(AccountMeta.id, AccountMeta.name, AccountMeta.balance, AccountMeta.owner)
                            .values(123L, "Default Account", 0, 1L)));

            final boolean isOracle = "Oracle".equals(dbEnv.getName());
            assertEquals(isOracle ? 5 : 10, result.rowsAffected());
            final int count = litebridge.select(Fn.convert(Fn.count(), int.class)).from(Account.class).oneOrThrow();
            assertEquals(isOracle ? 10 : 5, count);
        }

        // Similar query as above, but with multiple USING ON conditions and no DELETE/INSERT clause
        {
            final UpdateResult result = litebridge.mergeInto(Account.class, m -> m
                    .using(Person.class)
                    .on(AccountMeta.id).eq(PersonMeta.id)
                    .and(AccountMeta.id).gt(1000)
                    .or(AccountMeta.name).eq("Random Account Name")
                    .whenMatched(u -> u
                            .update(account -> account
                                    .set(AccountMeta.balance).to(500)
                                    .where(AccountMeta.id).lt(5))));

            assertEquals(0, result.rowsAffected());
        }

        // Merge with: "USING <dto>", "WHEN NOT MATCHED <insert dto>"
        {
            final boolean isOracle = "Oracle".equals(dbEnv.getName());
            assertEquals(isOracle ? 10 : 5, litebridge.select(Fn.count()).from(Account.class).oneOrThrow());

            final Supplier<Account> createAccount = () -> {
                final Account insertAccount = new Account();
                insertAccount.setName("Inserted Account");
                insertAccount.setBalance(BigInteger.ZERO);
                insertAccount.setOwner(persons[0]);
                return insertAccount;
            };

            final UpdateResult result = litebridge.mergeInto(Account.class, m -> m
                    .using(Person.class)
                    .on(AccountMeta.id).eq(PersonMeta.id)
                    .whenNotMatched(i -> i.insert(createAccount.get())));

            assertEquals(isOracle ? 1 : 6, result.rowsAffected());
            assertEquals(11, litebridge.select(Fn.count()).from(Account.class).oneOrThrow());
        }
    }

    @TestTemplate
    @DisplayName("Merge: upsert using VALUES")
    public void merge_upsert_values(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Don't run test for databases that do not support MERGE INTO
        assumeTrue(litebridge instanceof Litebridge);
        final Litebridge litebridge = (Litebridge) this.litebridge;
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);
        final QueryPlanCache queryPlanCache = LitebridgeInspector.getQueryPlanCache(litebridge);
        int prevCacheSize = queryPlanCache.size();

        // Insert a row that does not exist using direct values (or selecting literals if the database provider doesn't support the VALUES clause)
        {
            final UpdateResult insertResult = litebridge.mergeInto(Person.class, m -> m
                    .using(Fn.values("newPerson", "id", 123L))
                    .on(PersonMeta.id).eq(Fn.aliasRef("newPerson", "id"))
                    .whenMatched(u -> u
                            .update(person -> person
                                    .set(PersonMeta.name).to("Alice")
                                    .set(PersonMeta.surname).to("Smith")))
                    .whenNotMatched(i -> i
                            .insert(PersonMeta.id, PersonMeta.name, PersonMeta.surname)
                            .values(123L, "Alice", "Smith")));

            assertEquals(1, insertResult.rowsAffected());
            assertEquals(prevCacheSize + 1, queryPlanCache.size());
            final Person result = litebridge.select().from(Person.class).withIdOrThrow(123L);
            assertEquals("Alice", result.getName());
            assertEquals("Smith", result.getSurname());
            prevCacheSize = queryPlanCache.size();
        }

        // Update an existing row with the same merge statement
        {
            final UpdateResult updateResult = litebridge.mergeInto(Person.class, m -> m
                    .using(Fn.values("newPerson", "id", 123L))
                    .on(PersonMeta.id).eq(Fn.aliasRef("newPerson", "id"))
                    .whenMatched(u -> u
                            .update(person -> person
                                    .set(PersonMeta.name).to("Updated Name")
                                    .set(PersonMeta.surname).to("Updated Surname")))
                    .whenNotMatched(i -> i
                            .insert(PersonMeta.id, PersonMeta.name, PersonMeta.surname)
                            .values(123L, "Alice", "Smith")));

            assertEquals(1, updateResult.rowsAffected());
            assertEquals(prevCacheSize, queryPlanCache.size());
            final Person result = litebridge.select().from(Person.class).withIdOrThrow(123L);
            assertEquals("Updated Name", result.getName());
            assertEquals("Updated Surname", result.getSurname());
        }
    }
}
