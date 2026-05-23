package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;

public final class RegistrationColumnContext extends RegistrationTableContextImpl {

    private final FieldSpec fieldSpec;
    private final String column;
    private final RegistrationTableContextImpl registrationTableContext;

    RegistrationColumnContext(final FieldSpec fieldSpec, final String column, final RegistrationTableContextImpl registrationTableContext) {
        super(registrationTableContext);
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.registrationTableContext = registrationTableContext;
    }

    public RegistrationTableContext generateUsingSequence(final String sequence) {
        return generate(databaseProvider().getSequenceColumnValueGenerator(sequence));
    }

    public RegistrationTableContext generate(ColumnValueGenerator generator) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, generator));
        return registrationTableContext;
    }

    public RegistrationJoinStep joinOn(final String column) {
        return new RegistrationJoinStep(fieldSpec, this.column, column, registrationTableContext);
    }

    public RegistrationJoinStep joinUsing() {
        return joinOn(this.column);
    }

    @Override
    public RegistrationFieldContext mapField(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column));
        return registrationTableContext.mapField(fieldName);
    }

    @Override
    public RegistrationFieldContext mapProperty(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column));
        return registrationTableContext.mapProperty(fieldName);
    }

    @Override
    public DtoTableSpec buildDtoTableSpec(final Class<?> dtoClass) {
        addFieldColumnMapping(fieldSpec, new ColumnSpec(column));
        return super.buildDtoTableSpec(dtoClass);
    }
}
