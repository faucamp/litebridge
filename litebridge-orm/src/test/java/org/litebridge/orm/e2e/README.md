# End-to-End Tests

This directory contains end-to-end tests for Litebridge, which validate the functionality of the ORM 
by interacting with specific or all supported databases.

They are bound to Maven's integration test phase; to run them, use `mvn verify`.

## Database environment

To specify a specific database environment, set the `lb.e2e.env` system property to the desired environment:

- `all` - Run against all supported databases (default)
- `h2` - Run against H2
- `oracle` - Run against Oracle
- `postgres` - Run against PostgreSQL
- `sqlite` - Run against SQLite
- `none` - Disable E2E integration tests

By default, the tests will run against all supported databases (`all`).

Example:

```sql
mvn clean verify -Dlb.e2e.env=h2
```

## IntelliJ note

To run a specific end-to-end test in IntelliJ, it may be necessary to add the following VM option to the run configuration:

```
--add-opens litebridge.orm/org.litebridge.orm.e2e=ALL-UNNAMED --add-opens litebridge.orm/org.litebridge.orm.e2e.setup=ALL-UNNAMED
```

JUnit runs in the unnamed module; the above allows the test-specific `e2e` package to access the `litebridge.orm` module.