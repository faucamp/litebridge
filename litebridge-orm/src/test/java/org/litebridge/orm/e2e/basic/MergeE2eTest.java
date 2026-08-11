package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.orm.api.merge.MergeUpdateStep;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.expression.Fn;

import java.math.BigInteger;

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

        final Account[] accounts = new Account[10];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account();
            accounts[i].setName("Account" + i);
            accounts[i].setBalance(BigInteger.valueOf(i+1));
            accounts[i].setOwner(persons[i]);
        }

        litebridge.saveAll(persons);
        litebridge.saveAll(accounts);

        litebridge.mergeInto("LB.ACCOUNT", m -> m
                .using("LB.PERSON")
                .on("ACCOUNT_ID").eq(Fn.c("PERSON_ID"))
                .whenMatchedAnd(q -> q.and("ACCOUNT_ID").eq(Fn.c("PERSON_ID")),
                        u -> u.update(account ->
                                account.set("PERSON_ID").to(1)))
                .whenMatched(MergeUpdateStep::delete)
                .whenNotMatched(i ->
                        i.insert("ACCOUNT_ID", "ACCOUNT_NAME", "BALANCE")
                                .values(123L, "Default Account", 0)));


    }
}
