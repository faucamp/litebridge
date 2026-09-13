package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.api.select.FromClauseStart;
import org.litebridge.orm.api.select.FromClauseStartTypeOverride;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.intent.ConvertIntent;
import org.litebridge.orm.persistence.DtoConstructor;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SelectEngineTest {

    @Test
    void selectByDtoClass() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final DtoFromClauseTerminal<UserDto> terminal = engine.select(UserDto.class, context);

        // Then
        assertNotNull(terminal);
    }

    @Test
    void selectByDtoClassWithContextDtoClass() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final DtoFromClauseTerminal<UserDto> terminal = engine.select(UserDto.class, ContextDto.class, context);

        // Then
        assertNotNull(terminal);
    }

    @Test
    void selectByFieldsOrColumns() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = mode -> mock(LitebridgeContext.class);

        // When
        final FromClauseStart start = engine.select(new String[]{"id", "name"}, contextCreator);

        // Then
        assertNotNull(start);
    }

    @Test
    void selectByExpressions() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = mode -> mock(LitebridgeContext.class);

        // When
        final FromClauseStart start = engine.select(new ExpressionSpec[0], contextCreator);

        // Then
        assertNotNull(start);
    }

    @Test
    void selectByTypeOverrideExpressionSpec() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = mode -> mock(LitebridgeContext.class);
        final CountSpec countSpec = new CountSpec();

        // When
        final FromClauseStartTypeOverride<Long> start = engine.select(countSpec, contextCreator);

        // Then
        assertNotNull(start);
    }

    @Test
    void selectByConvertIntent() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = mode -> mock(LitebridgeContext.class);
        final ConvertIntent<String> convertIntent = new ConvertIntent<>(new ExpressionSpec[0], String.class);

        // When
        final FromClauseStartTypeOverride<String> start = engine.select(convertIntent, contextCreator);

        // Then
        assertNotNull(start);
    }

    @Test
    void selectAll() {
        // Given
        final DtoConstructor dtoConstructor = mock(DtoConstructor.class);
        final SelectEngine engine = new SelectEngine(dtoConstructor);
        final Function<LitebridgeContext.Mode, LitebridgeContext> contextCreator = mode -> mock(LitebridgeContext.class);

        // When
        final FromClauseStart start = engine.select(contextCreator);

        // Then
        assertNotNull(start);
    }

    static class UserDto {
        private Long id;
    }

    static class ContextDto {
        private Long tenantId;
    }
}
