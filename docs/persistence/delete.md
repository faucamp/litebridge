# Deleting data

← [Persistence](index.md)

## Contents

<!-- TOC -->
* [Overview](#overview)
* [Usage](#usage)
  * [Deleting DTOs directly](#deleting-dtos-directly)
  * [Deleting DTOs via a query](#deleting-dtos-via-a-query)
  * [SQL-level deletes](#sql-level-deletes)
<!-- TOC -->

## Overview

Litebridge provides a fluent API for deleting data, which allows for:

* DTO-level deletes
  * direct deletion of DTOs 
  * deletion via queries
* SQL-level deletes

## Usage

### Deleting DTOs directly

Litebridge provides a simple mechanism for deleting objects, which functions similar to `save()`.
To delete an object, simply call `delete()` and pass the object as a parameter:

```java
Person person = new Person();
person.setId(1);

litebridge.delete(person);
```

### Deleting DTOs via a query

Litebridge also provides a fluent API for deleting registered object types via a query.
This is useful for deleting multiple objects that match certain criteria.
To delete objects via a query, use the `delete()` method with a query object:

```java
litebridge.delete(Person .class)
        .where("age").gt(20)
        .execute();
```

The `execute()` call at the end signals the end of the chain, allowing Litebridge to delete the objects that match the
built-up criteria.

### SQL-level deletes

Litebridge's fluent API allows for arbitrary SQL-level deletes:

```java
litebridge.delete().from("LB.PERSON")
        .where("AGE").gt(20)
        .execute();
```

For SQL-level deletes, no DTO registration is required.