# End-to-End Tests

The build contains end-to-end/integration tests for Litebridge which validate the functionality of the ORM 
by interacting with specific or all supported databases.
They also serve as a reference for developers to understand how the ORM works.

The tests are found in: [`litebridge-orm/src/test/java/org/litebridge/orm/e2e/`](../../litebridge-orm/src/test/java/org/litebridge/orm/e2e/)

## Database environment

To specify a specific database environment, set the `lb.e2e.env` system property to the desired environment:

- `all` - Run against all supported databases (default)
- `h2` - Run against H2
- `oracle` - Run against Oracle
- `none` - Disable E2E integration tests

By default, the tests will run against all supported databases (`all`).

### Examples:

To run H2 integration tests only:

```bash
mvn clean test -Dlb.e2e.env=h2
```

To disable integration tests (but still run unit tests):

```bash
mvn clean test -Dlb.e2e.env=none
```

## IntelliJ note

To run a specific end-to-end test in IntelliJ, you may need to add the following VM option to the run configuration:

```
--add-opens litebridge.orm/org.litebridge.orm.e2e=ALL-UNNAMED --add-opens litebridge.orm/org.litebridge.orm.e2e.setup=ALL-UNNAMED
```

JUnit runs in the unnamed module; the above allows the test-specific `e2e` package to access the `litebridge.orm` module.