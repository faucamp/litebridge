//package org.litebridge.orm.api.select.model;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.query.Limit;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class LimitSpecTest {
//
//    @Test
//    void setGetLimit() {
//        // Given
//        final LimitSpec limitSpec = new LimitSpec();
//        limitSpec.setLimit(10);
//
//        // When
//        final Optional<Integer> result = limitSpec.getLimit();
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals(10, result.get().intValue());
//    }
//
//    @Test
//    void getLimit_null() {
//        // Given
//        final LimitSpec limitSpec = new LimitSpec();
//
//        // When
//        final Optional<Integer> result = limitSpec.getLimit();
//
//        // Then
//        assertFalse(result.isPresent());
//    }
//
//    @Test
//    void setGetOffset() {
//        // Given
//        final LimitSpec limitSpec = new LimitSpec();
//        limitSpec.setOffset(10);
//
//        // When
//        final Optional<Integer> result = limitSpec.getOffset();
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals(10, result.get().intValue());
//    }
//
//    @Test
//    void getOffset_null() {
//        // Given
//        final LimitSpec limitSpec = new LimitSpec();
//
//        // When
//        final Optional<Integer> result = limitSpec.getOffset();
//
//        // Then
//        assertFalse(result.isPresent());
//    }
//
//    @Test
//    void toLimit() {
//        // Given
//        final LimitSpec limitSpec = new LimitSpec();
//        limitSpec.setLimit(10);
//        limitSpec.setOffset(20);
//
//        // When
//        final Optional<Limit> result = limitSpec.toLimit();
//
//        // Then
//        assertTrue(result.isPresent());
//        assertTrue(result.get().limit().isPresent());
//        assertEquals(10, result.get().limit().get());
//        assertTrue(result.get().offset().isPresent());
//        assertEquals(20, result.get().offset().get());
//    }
//}