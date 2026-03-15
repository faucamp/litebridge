# End-to-End Tests

This directory contains end-to-end tests for LiteBridge ORM, which validate the functionality of the ORM 
by interacting with an in-memory H2 database. 

## IntelliJ note

To run a specific end-to-end test in IntelliJ, you may need to add the following VM option to the run configuration:

```
--add-opens litebridge.orm/org.litebridge.orm.e2e=ALL-UNNAMED
```

JUnit runs in the unnamed module; the above allows the test-specific `e2e` package to access the `litebridge.orm` module.