package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.MergeUpdateStep;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.meta.AccountMeta;
import org.litebridge.orm.e2e.basic.meta.PersonMeta;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.expression.Fn;

import java.math.BigInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MultiDbTestExtension.class)
public class MergeE2eTest extends AbstractE2eTest {

    @TestTemplate
    public void merge(final DbEnvDtoTableMapper tableMapper) throws Exception {
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

        // Merge with: "USING <dto>", "WHEN MATCHED AND <update>", "WHEN MATHED <delete>", "WHEN NOT MATCHED <insert values directly>"
        {
            final UpdateResult result = litebridge.mergeInto(Account.class, m -> m
                    .using(Person.class)
                    .on(AccountMeta.id).eq(PersonMeta.id)
                    .whenMatched(u -> u
                            .update(account -> account
                                    .set(AccountMeta.balance).to(500)
                                    .where(AccountMeta.id).lt(5)))
                    .whenMatched(MergeUpdateStep::delete)
                    .whenNotMatched(i -> i
                            .insert(AccountMeta.id, AccountMeta.name, AccountMeta.balance, AccountMeta.owner)
                            .values(123L, "Default Account", 0, 1L)));

            assertEquals(10, result.rowsAffected());
            final int count = litebridge.select(Fn.convert(Fn.count(), int.class)).from(Account.class).oneOrThrow();
            assertEquals(5, count);
        }

        // Merge with: "USING <dto>", "WHEN NOT MATCHED <insert dto>"
        {
            assertEquals(5, litebridge.select(Fn.count()).from(Account.class).oneOrThrow());

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

            assertEquals(6, result.rowsAffected());
            assertEquals(11, litebridge.select(Fn.count()).from(Account.class).oneOrThrow());
        }
    }
}
