package org.litebridge.orm.e2e.oracle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridge.db.oracle.api.LitebridgeOracle;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.orm.LitebridgeInspector;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.meta.AccountMeta;
import org.litebridge.orm.e2e.basic.meta.PersonMeta;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.engine.QueryPlanCache;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests Oracle-specific features.
 * <p>
 * These tests will only run against the Oracle database environment.
 */
public class OracleE2eTest extends AbstractE2eTest {

    @TestTemplate
    @DisplayName("Oracle: INSERT ALL (SQL mode)")
    void insertAll_sql(final DbEnvDtoTableMapper tableMapper) throws Exception {
        assumeTrue(litebridge instanceof LitebridgeOracle);
        final LitebridgeOracle litebridge = (LitebridgeOracle) this.litebridge;

        final InsertResult result = litebridge.insertAll(ia -> ia
                .intoTable("LB.PERSON", i -> i
                        .into("PERSON_ID", "FIRST_NAME", "SURNAME")
                        .values(1, "Alice", "Smith")
                        .values(2, "Bob", "Jones")
                        .values(3, "Charlie", "Brown"))
                .intoTable("LB.ACCOUNT", i -> i
                        .into("ACCOUNT_ID", "ACCOUNT_NAME", "BALANCE", "PERSON_ID")
                        .values(1, "Alice's Account", BigInteger.ZERO, 1)
                        .values(2, "Bob's Account", BigInteger.ONE, 2)
                        .values(3, "Charlie's Account", BigInteger.TEN, 3)));

        assertNotNull(result);
        assertEquals(6, result.rowsAffected());
    }

    @TestTemplate
    @DisplayName("Oracle: INSERT ALL (DTO mode)")
    void insertAll_dto(final DbEnvDtoTableMapper tableMapper) throws Exception {
        assumeTrue(litebridge instanceof LitebridgeOracle);
        final LitebridgeOracle litebridge = (LitebridgeOracle) this.litebridge;
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);
        final QueryPlanCache queryPlanCache = LitebridgeInspector.getQueryPlanCache(litebridge);
        assertEquals(0, queryPlanCache.size());

        // Compile and execute the INSERT ALL statement
        {
            final InsertResult result = litebridge.insertAll(ia -> ia
                    .intoTable(Person.class, i -> i
                            .into(PersonMeta.id, PersonMeta.name, PersonMeta.surname)
                            .values(1, "Alice", "Smith")
                            .values(2, "Bob", "Jones")
                            .values(3, "Charlie", "Brown"))
                    .intoTable(Account.class, i -> i
                            .into(AccountMeta.id, AccountMeta.name, AccountMeta.balance, AccountMeta.owner)
                            .values(1, "Alice's Account", BigInteger.ZERO, 1)
                            .values(2, "Bob's Account", BigInteger.ONE, 2)
                            .values(3, "Charlie's Account", BigInteger.TEN, 3)));

            assertNotNull(result);
            assertEquals(6, result.rowsAffected());
            assertEquals(1, queryPlanCache.size());
        }

        // Ensure that the cache is used when running a similar insert
        {
            final InsertResult result = litebridge.insertAll(ia -> ia
                    .intoTable(Person.class, i -> i
                            .into(PersonMeta.id, PersonMeta.name, PersonMeta.surname)
                            .values(4, "John", "Doe")
                            .values(5, "Jane", "Doe")
                            .values(6, "Bruce", "Wayne"))
                    .intoTable(Account.class, i -> i
                            .into(AccountMeta.id, AccountMeta.name, AccountMeta.balance, AccountMeta.owner)
                            .values(4, "John's Account", BigInteger.ZERO, 4)
                            .values(5, "Jane's Account", BigInteger.ZERO, 5)
                            .values(6, "Bruce's Account", BigInteger.valueOf(1000000), 6)));

            assertNotNull(result);
            assertEquals(6, result.rowsAffected());
            assertEquals(1, queryPlanCache.size());
        }
    }
}
