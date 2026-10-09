package org.litebridge.spring.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.DtoDeleteStart;
import org.litebridge.orm.api.select.FromClauseStartTypeOverride;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoOrderByClause;
import org.litebridge.orm.api.select.dto.DtoOrderByClauseChain;
import org.litebridge.orm.api.select.dto.DtoWhereConditionClauseTerminal;
import org.litebridge.orm.expression.TypeOverride;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LitebridgeRepositoryImplTest {

    @Mock
    private LitebridgeCore litebridge;

    @Mock
    private DtoFromClauseTerminal<TestEntity> dtoSelectTerminal;

    private LitebridgeRepositoryImpl<TestEntity, Long> repository;

    @BeforeEach
    void setUp() {
        repository = new LitebridgeRepositoryImpl<>(TestEntity.class, litebridge);
    }

    @Test
    void save_delegatesToLitebridge_andReturnsEntity() {
        // Given
        final TestEntity entity = new TestEntity(1L, "Alice", 30);

        // When
        final TestEntity result = repository.save(entity);

        // Then
        assertSame(entity, result);
        verify(litebridge).save(entity);
    }

    @Test
    void saveAll_withList_delegatesToLitebridge_andReturnsList() {
        // Given
        final List<TestEntity> entities = List.of(
                new TestEntity(1L, "Alice", 30),
                new TestEntity(2L, "Bob", 25)
        );

        // When
        final List<TestEntity> result = repository.saveAll(entities);

        // Then
        assertSame(entities, result);
        verify(litebridge).saveAll(entities);
    }

    @Test
    void saveAll_withNonListIterable_delegatesToLitebridge_andReturnsConvertedList() {
        // Given
        final TestEntity entity1 = new TestEntity(1L, "Alice", 30);
        final TestEntity entity2 = new TestEntity(2L, "Bob", 25);
        final Set<TestEntity> entities = Set.of(entity1, entity2);

        // When
        final List<TestEntity> result = repository.saveAll(entities);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(entity1));
        assertTrue(result.contains(entity2));
        verify(litebridge).saveAll(entities);
    }

    @Test
    void findById_whenEntityFound_returnsOptionalWithEntity() {
        // Given
        final TestEntity entity = new TestEntity(1L, "Alice", 30);
        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.withId(1L)).thenReturn(Optional.of(entity));

        // When
        final Optional<TestEntity> result = repository.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertSame(entity, result.get());
        verify(litebridge).select(TestEntity.class);
        verify(dtoSelectTerminal).withId(1L);
    }

    @Test
    void findById_whenEntityNotFound_returnsEmptyOptional() {
        // Given
        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.withId(99L)).thenReturn(Optional.empty());

        // When
        final Optional<TestEntity> result = repository.findById(99L);

        // Then
        assertFalse(result.isPresent());
        verify(litebridge).select(TestEntity.class);
        verify(dtoSelectTerminal).withId(99L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsById_whenCountGreaterThanZero_returnsTrue() {
        // Given
        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.withIdOrThrow(1L)).thenReturn(1L);

        // When
        final boolean exists = repository.existsById(1L);

        // Then
        assertTrue(exists);
        verify(countDtoTerminal).withIdOrThrow(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsById_whenCountIsZero_returnsFalse() {
        // Given
        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.withIdOrThrow(99L)).thenReturn(0L);

        // When
        final boolean exists = repository.existsById(99L);

        // Then
        assertFalse(exists);
        verify(countDtoTerminal).withIdOrThrow(99L);
    }

    @Test
    void findAll_returnsAllEntities() {
        // Given
        final List<TestEntity> entities = List.of(
                new TestEntity(1L, "Alice", 30),
                new TestEntity(2L, "Bob", 25)
        );
        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.list()).thenReturn(entities);

        // When
        final List<TestEntity> result = repository.findAll();

        // Then
        assertSame(entities, result);
        verify(dtoSelectTerminal).list();
    }

    @Test
    void findAllById_returnsMatchingEntities() {
        // Given
        final List<Long> ids = List.of(1L, 2L);
        final List<TestEntity> entities = List.of(
                new TestEntity(1L, "Alice", 30),
                new TestEntity(2L, "Bob", 25)
        );
        @SuppressWarnings("unchecked")
        final DtoWhereConditionClauseTerminal<TestEntity> whereTerminal = mock(DtoWhereConditionClauseTerminal.class);
        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.withIds(ids)).thenReturn(whereTerminal);
        when(whereTerminal.list()).thenReturn(entities);

        // When
        final List<TestEntity> result = repository.findAllById(ids);

        // Then
        assertSame(entities, result);
        verify(dtoSelectTerminal).withIds(ids);
        verify(whereTerminal).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void count_returnsTotalEntityCount() {
        // Given
        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.oneOrThrow()).thenReturn(42L);

        // When
        final long count = repository.count();

        // Then
        assertEquals(42L, count);
        verify(countDtoTerminal).oneOrThrow();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteById_delegatesToLitebridgeDelete() {
        // Given
        final DtoDeleteStart<TestEntity> deleteStart = mock(DtoDeleteStart.class);
        when(deleteStart.withId(1L)).thenReturn(deleteStart);
        final ArgumentCaptor<Function<DtoDeleteStart<TestEntity>, DeleteTerminal>> captor =
                ArgumentCaptor.forClass(Function.class);

        // When
        repository.deleteById(1L);

        // Then
        verify(litebridge).delete(eq(TestEntity.class), captor.capture());
        final Function<DtoDeleteStart<TestEntity>, DeleteTerminal> function = captor.getValue();
        final DeleteTerminal result = function.apply(deleteStart);
        assertSame(deleteStart, result);
        verify(deleteStart).withId(1L);
    }

    @Test
    void delete_delegatesToLitebridgeDeleteEntity() {
        // Given
        final TestEntity entity = new TestEntity(1L, "Alice", 30);

        // When
        repository.delete(entity);

        // Then
        verify(litebridge).delete(entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteAllById_delegatesToLitebridgeDelete() {
        // Given
        final List<Long> ids = List.of(1L, 2L);
        final DtoDeleteStart<TestEntity> deleteStart = mock(DtoDeleteStart.class);
        when(deleteStart.withIds(ids)).thenReturn(deleteStart);
        final ArgumentCaptor<Function<DtoDeleteStart<TestEntity>, DeleteTerminal>> captor =
                ArgumentCaptor.forClass(Function.class);

        // When
        repository.deleteAllById(ids);

        // Then
        verify(litebridge).delete(eq(TestEntity.class), captor.capture());
        final Function<DtoDeleteStart<TestEntity>, DeleteTerminal> function = captor.getValue();
        final DeleteTerminal result = function.apply(deleteStart);
        assertSame(deleteStart, result);
        verify(deleteStart).withIds(ids);
    }

    @Test
    void deleteAll_withEntitiesIterable_delegatesToLitebridgeForEachEntity() {
        // Given
        final TestEntity entity1 = new TestEntity(1L, "Alice", 30);
        final TestEntity entity2 = new TestEntity(2L, "Bob", 25);
        final List<TestEntity> entities = List.of(entity1, entity2);

        // When
        repository.deleteAll(entities);

        // Then
        verify(litebridge).delete(entity1);
        verify(litebridge).delete(entity2);
    }

    @Test
    void deleteAll_withNoArgs_delegatesToLitebridgeDeleteClass() {
        // Given / When
        repository.deleteAll();

        // Then
        verify(litebridge).delete(TestEntity.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withSortAscending_buildsAscOrderByQueryAndReturnsList() {
        // Given
        final Sort sort = Sort.by(Sort.Order.asc("name"));
        final DtoOrderByClause<TestEntity> orderByClause = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> orderByChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("name")).thenReturn(orderByClause);
        when(orderByClause.asc()).thenReturn(orderByChain);
        when(orderByChain.list()).thenReturn(entities);

        // When
        final List<TestEntity> result = repository.findAll(sort);

        // Then
        assertSame(entities, result);
        verify(dtoSelectTerminal).orderBy("name");
        verify(orderByClause).asc();
        verify(orderByChain).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withSortDescending_buildsDescOrderByQueryAndReturnsList() {
        // Given
        final Sort sort = Sort.by(Sort.Order.desc("age"));
        final DtoOrderByClause<TestEntity> orderByClause = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> orderByChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("age")).thenReturn(orderByClause);
        when(orderByClause.desc()).thenReturn(orderByChain);
        when(orderByChain.list()).thenReturn(entities);

        // When
        final List<TestEntity> result = repository.findAll(sort);

        // Then
        assertSame(entities, result);
        verify(dtoSelectTerminal).orderBy("age");
        verify(orderByClause).desc();
        verify(orderByChain).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withMultipleSortOrders_chainsOrderByClausesAndReturnsList() {
        // Given
        final Sort sort = Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("age"),
                Sort.Order.asc("city")
        );
        final DtoOrderByClause<TestEntity> firstOrderBy = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> firstChain = mock(DtoOrderByClauseChain.class);
        final DtoOrderByClause<TestEntity> secondOrderBy = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> secondChain = mock(DtoOrderByClauseChain.class);
        final DtoOrderByClause<TestEntity> thirdOrderBy = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> thirdChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("name")).thenReturn(firstOrderBy);
        when(firstOrderBy.asc()).thenReturn(firstChain);
        when(firstChain.then("age")).thenReturn(secondOrderBy);
        when(secondOrderBy.desc()).thenReturn(secondChain);
        when(secondChain.then("city")).thenReturn(thirdOrderBy);
        when(thirdOrderBy.asc()).thenReturn(thirdChain);
        when(thirdChain.list()).thenReturn(entities);

        // When
        final List<TestEntity> result = repository.findAll(sort);

        // Then
        assertSame(entities, result);
        verify(dtoSelectTerminal).orderBy("name");
        verify(firstOrderBy).asc();
        verify(firstChain).then("age");
        verify(secondOrderBy).desc();
        verify(secondChain).then("city");
        verify(thirdOrderBy).asc();
        verify(thirdChain).list();
    }

    @Test
    void findAll_withUnsortedSort_delegatesToFindAll() {
        // Given
        final Sort sort = Sort.unsorted();
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));
        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.list()).thenReturn(entities);

        // When
        final List<TestEntity> result = repository.findAll(sort);

        // Then
        assertSame(entities, result);
        verify(dtoSelectTerminal).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withPageableUnsorted_returnsPagedResultWithCount() {
        // Given
        final Pageable pageable = PageRequest.of(1, 5);
        final List<TestEntity> entities = List.of(new TestEntity(2L, "Bob", 25));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.limit(5)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.offset(5)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.list()).thenReturn(entities);

        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.oneOrThrow()).thenReturn(15L);

        // When
        final Page<TestEntity> result = repository.findAll(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertSame(entities.get(0), result.getContent().get(0));
        assertEquals(15L, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
        verify(dtoSelectTerminal).limit(5);
        verify(dtoSelectTerminal).offset(5);
        verify(dtoSelectTerminal).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withPageableAndAscendingSort_appliesSortLimitOffsetAndReturnsPage() {
        // Given
        final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name")));
        final DtoOrderByClause<TestEntity> orderByClause = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> orderByChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("name")).thenReturn(orderByClause);
        when(orderByClause.asc()).thenReturn(orderByChain);
        when(orderByChain.limit(10)).thenReturn(orderByChain);
        when(orderByChain.offset(0)).thenReturn(orderByChain);
        when(orderByChain.list()).thenReturn(entities);

        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.oneOrThrow()).thenReturn(1L);

        // When
        final Page<TestEntity> result = repository.findAll(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertSame(entities.get(0), result.getContent().get(0));
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        verify(dtoSelectTerminal).orderBy("name");
        verify(orderByClause).asc();
        verify(orderByChain).limit(10);
        verify(orderByChain).offset(0);
        verify(orderByChain).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withPageableAndDescendingSort_appliesSortLimitOffsetAndReturnsPage() {
        // Given
        final Pageable pageable = PageRequest.of(2, 5, Sort.by(Sort.Order.desc("age")));
        final DtoOrderByClause<TestEntity> orderByClause = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> orderByChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("age")).thenReturn(orderByClause);
        when(orderByClause.desc()).thenReturn(orderByChain);
        when(orderByChain.limit(5)).thenReturn(orderByChain);
        when(orderByChain.offset(10)).thenReturn(orderByChain);
        when(orderByChain.list()).thenReturn(entities);

        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.oneOrThrow()).thenReturn(20L);

        // When
        final Page<TestEntity> result = repository.findAll(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(20L, result.getTotalElements());
        assertEquals(2, result.getNumber());
        assertEquals(5, result.getSize());
        verify(dtoSelectTerminal).orderBy("age");
        verify(orderByClause).desc();
        verify(orderByChain).limit(5);
        verify(orderByChain).offset(10);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withPageableAndMultipleSortOrders_chainsSortOrdersWithLimitOffsetAndReturnsPage() {
        // Given
        final Pageable pageable = PageRequest.of(1, 10, Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("age")
        ));
        final DtoOrderByClause<TestEntity> firstOrderBy = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> firstChain = mock(DtoOrderByClauseChain.class);
        final DtoOrderByClause<TestEntity> secondOrderBy = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> secondChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("name")).thenReturn(firstOrderBy);
        when(firstOrderBy.asc()).thenReturn(firstChain);
        when(firstChain.then("age")).thenReturn(secondOrderBy);
        when(secondOrderBy.desc()).thenReturn(secondChain);
        when(secondChain.limit(10)).thenReturn(secondChain);
        when(secondChain.offset(10)).thenReturn(secondChain);
        when(secondChain.list()).thenReturn(entities);

        final FromClauseStartTypeOverride<Long> typeOverride = mock(FromClauseStartTypeOverride.class);
        final DtoFromClauseTerminal<Long> countDtoTerminal = mock(DtoFromClauseTerminal.class);
        when(litebridge.select(any(TypeOverride.class))).thenReturn(typeOverride);
        when(typeOverride.from(TestEntity.class)).thenReturn(countDtoTerminal);
        when(countDtoTerminal.oneOrThrow()).thenReturn(25L);

        // When
        final Page<TestEntity> result = repository.findAll(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(25L, result.getTotalElements());
        verify(dtoSelectTerminal).orderBy("name");
        verify(firstOrderBy).asc();
        verify(firstChain).then("age");
        verify(secondOrderBy).desc();
        verify(secondChain).limit(10);
        verify(secondChain).offset(10);
    }

    @Test
    void findAll_withUnpagedPageable_returnsPageWithAllRecords() {
        // Given
        final Pageable pageable = Pageable.unpaged();
        final List<TestEntity> entities = List.of(
                new TestEntity(1L, "Alice", 30),
                new TestEntity(2L, "Bob", 25)
        );
        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.list()).thenReturn(entities);

        // When
        final Page<TestEntity> result = repository.findAll(pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        verify(dtoSelectTerminal).list();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withUnpagedPageableAndSort_returnsPageWithSortedRecords() {
        // Given
        final Sort sort = Sort.by(Sort.Order.asc("name"));
        final Pageable pageable = Pageable.unpaged(sort);
        final DtoOrderByClause<TestEntity> orderByClause = mock(DtoOrderByClause.class);
        final DtoOrderByClauseChain<TestEntity> orderByChain = mock(DtoOrderByClauseChain.class);
        final List<TestEntity> entities = List.of(new TestEntity(1L, "Alice", 30));

        when(litebridge.select(TestEntity.class)).thenReturn(dtoSelectTerminal);
        when(dtoSelectTerminal.orderBy("name")).thenReturn(orderByClause);
        when(orderByClause.asc()).thenReturn(orderByChain);
        when(orderByChain.list()).thenReturn(entities);

        // When
        final Page<TestEntity> result = repository.findAll(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(dtoSelectTerminal).orderBy("name");
        verify(orderByClause).asc();
        verify(orderByChain).list();
    }

    static class TestEntity {
        private Long id;
        private String name;
        private int age;

        TestEntity(final Long id, final String name, final int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
