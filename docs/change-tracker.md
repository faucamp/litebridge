# Change Tracker

← [Home](index.md)

The `ChangeTracker` API prvoides lightweight change tracking for arbitrary DTOs. 
While the Litebridge ORM uses this internally for SQL optimisation, it can be used independently for other state-tracking needs.

## Usage

### Tracking all changes to a DTO

To track changes to a DTO, it needs to be registered with a `ChangeTracker` instance.
This is done via the `ChangeTracker.trackDto()` method:

```java
ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
tracker.trackDto(dto);
```

When calling `track()` on a DTO, a snapshot of the DTO state is taken which is
recorded as part of a `TrackedDto` instance referring to the original DTO.

### Tracking specific fields of a DTO

To track changes to specific fields of a DTO, specify the names of the fieldsto track when
calling `trackDto()`:

```java
ChangeTracker changeTracker = new ChangeTracker(MethodHandles.lookup());
tracker.trackDto(dto, Set.of("field1", "field2"));
```

This will ensure that only `field1` and `field2` are tracked, whilst other fields are ignored.

> Note: tracked fields may be inherited from superclasses; they do not have to be declared by the DTO class itself.

### Retrieving changed fields

To see what fields of a DTO have changed since the last snapshot, retrieve
the `TrackedDto` instance and invoke its `getChangedFields()` method:

```java
TrackedDto<TestDto> trackedDto = changeTracker.getTrackedDto(dto);
ChangedFields changedFields = trackedDto.changedFields();
```

The `ChangedFields` instance contains all fields that have changed since the last snapshot.
It provides methods to iterate over the changed fields or to access a specific field:

```java
// Stream all changed field and collect their names
List<String> fieldNames = changedFields.stream()
    .map(ChangedField::name)
    .toList();

// Changed fields can be iterated over using a for-each loop
changedFields.forEach(field -> {
    System.out.println(field.name());
});

// Access a specific field; in this case getOrNull is used to short-circuit defaulting the Optional returned by get()
ChangedField field = changedFields.getOrNull("field1");
String fieldName = field.name();
Object currentValue = field.value();

// If the field is a Map or Collection, the ChangedField instance is a specific subtype
// which allows deep inspection of the field's contents to track changes in the collection itself
if (changedField instanceof ChangedMapField changedMapField) {
    // Get a snapshot of the map's previous contents
    // Format: original map key -> hash value of original value
    Map<?, Integer> mapSnaphot = changedMapField.mapSnapshot();
}

if (changedField instanceof ChangedCollectionField changedCollectionField) {
    // Get a snapshot of the collection's previous contents
    List<Integer> prevListSnapshot = changedCollectionField.prevListSnapshot();
    List<Integer> currentListSnapshot = changedCollectionField.listSnapshot();
}
```

### Snapshot behaviour

When `TrackedDto.changedFields()` is called for the first time, a new snapshot of the DTO is taken. The return
value of `changedFields()` is a snapshot of the DTO's state at the time of this `changedFields()` call.
This value is cached until `changedFields()` is called again with a `refresh` parameter set to `true`:

```java
// Refresh the cached snapshot
ChangedFields updatedChangedFields = trackedDto.changedFields(true);
```

or when forcing a snapshot to be taken via the `TrackDto.snapshot()` call:

```java
// Force a snapshot to be taken
trackedDto.snapshot(true);
```

Internally, snapshots are stored as hashes of the tracked field values at the time of the snapshot.

### Tracking scope

The `ChangeTracker` instance (and `TrackedDto` instances themselves) maintain weak references to the 
target DTO. 

If the DTO itself is garbage collected, the corresponding `TrackedDto` is removed from the `ChangeTracker` instance
automatically as well. 

Additionally, calling `TrackedDto.dto()` in a scenario where a reference to a `TrackedDto` still exists, but 
the underlying DTO has since been garbage collected, will result in an `IllegalStateException` with
the reason "DTO object has been garbage collected".