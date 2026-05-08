# End-to-End Tests

This directory contains end-to-end tests for LiteBridge ORM, which validate the functionality of the ORM 
by interacting with specifc or all supported databases.

## Database environment

To specify a specific database environment, set the `db.env` system property to the desired environment:

- `all` - Run against all supported databases (default)
- `h2` - Run against H2
- `oracle` - Run against Oracle
- `none` - Disable E2E integration tests

By default, the tests will run against all supported databases (`all`).

## IntelliJ note

To run a specific end-to-end test in IntelliJ, you may need to add the following VM option to the run configuration:

```
--add-opens litebridge.orm/org.litebridge.orm.e2e=ALL-UNNAMED --add-opens litebridge.orm/org.litebridge.orm.e2e.setup=ALL-UNNAMED --add-opens litebridge.orm/org.litebridge.orm.e2e=ALL-UNNAMED --add-opens litebridge.orm/org.litebridge.orm.e2e.compositepk=ALL-UNNAMED
```

JUnit runs in the unnamed module; the above allows the test-specific `e2e` package to access the `litebridge.orm` module.