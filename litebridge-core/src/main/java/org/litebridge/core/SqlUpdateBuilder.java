//package org.litebridge.core;
//
//import jakarta.annotation.Nullable;
//import org.litebridge.commons.CollectionUtils;
//import org.litebridge.commons.StringUtils;
//import org.litebridge.dto.ChangeTrackingDto;
//import org.litebridge.dto.ChangedField;
//import org.litebridge.dto.DbTableChangeTrackingDto;
//
//import java.math.BigDecimal;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.time.ZonedDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiFunction;
//
//public class SqlUpdateBuilder {
//
//    private final StringBuilder sb;
//    private List<Object> parameters = new ArrayList<>();
//    private final String whereClause;
//    private List<Object> whereParameters;
//    private final BiFunction<String, Object, Object> parameterPreProcessor;
//
//    public SqlUpdateBuilder(final String tableName, final String whereClause, final List<Object> whereParameters, @Nullable BiFunction<String, Object, Object> parameterPreProcessor) {
//        this.sb = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
//
//        if (!StringUtils.isBlank(whereClause)) {
//            this.whereClause = whereClause;
//            this.whereParameters = whereParameters;
//        } else {
//            this.whereClause = null;
//            this.whereParameters = null;
//        }
//
//        this.parameterPreProcessor = parameterPreProcessor;
//    }
//
//    public SqlUpdateBuilder(final String tableName, final String whereClause, final List<Object> whereParameters) {
//        this(tableName, whereClause, whereParameters, null);
//    }
//
//    public SqlUpdateBuilder(final DbTableChangeTrackingDto dbTableChangeTrackingDto, final String whereClause, final List<Object> whereParameters) {
//        this(dbTableChangeTrackingDto.getDbTableName(), whereClause, whereParameters, null);
//        mergeChanges(dbTableChangeTrackingDto);
//    }
//
//    public SqlUpdateBuilder(final DbTableChangeTrackingDto dbTableChangeTrackingDto, final String whereClause, final List<Object> whereParameters, BiFunction<String, Object, Object> parameterPreProcessor) {
//        this(dbTableChangeTrackingDto.getDbTableName(), whereClause, whereParameters, parameterPreProcessor);
//        mergeChanges(dbTableChangeTrackingDto);
//    }
//
//    public SqlUpdateBuilder withColumn(final String columnName, final Object value) {
//        addCommaSeparator();
//        sb.append(columnName).append(" = ?");
//        parameters.add(value);
//        return this;
//    }
//
//    /**
//     * Merges the changes from another ChangeTrackingDto explicity, specifying a map of field name->db column name
//     *
//     * @param changeTrackingDto The DTO to merge
//     * @param fieldColumnMap    Field name -> database column mapping
//     */
//    public SqlUpdateBuilder mergeChanges(final ChangeTrackingDto changeTrackingDto, final Map<String, String> fieldColumnMap) {
//        if (changeTrackingDto == null) {
//            return this;
//        }
//
//        if (CollectionUtils.isEmpty(fieldColumnMap)) {
//            throw new IllegalArgumentException("No field-column map provided");
//        }
//
//        for (ChangedField changedField : changeTrackingDto.getChangedFields().values()) {
//            final String dbColumnName = fieldColumnMap.get(changedField.fieldName());
//
//            if (dbColumnName != null) {
//                addCommaSeparator();
//                sb.append(dbColumnName).append(" = ?");
//
//                if (parameterPreProcessor != null) {
//                    parameters.add(parameterPreProcessor.apply(changedField.fieldName(), changedField.value()));
//                } else {
//                    parameters.add(changedField.value());
//                }
//            }
//        }
//
//        return this;
//    }
//
//    private void mergeChanges(final ChangeTrackingDto changeTrackingDto) {
//        if (changeTrackingDto == null || CollectionUtils.isEmpty(changeTrackingDto.getChangedFields())) {
//            return;
//        }
//
//        for (ChangedField changedField : changeTrackingDto.getChangedFields().values()) {
//            if (!StringUtils.isBlank(changedField.trackedField().dbColumnName())) {
//                addCommaSeparator();
//                sb.append(changedField.trackedField().dbColumnName()).append(" = ?");
//
//                if (parameterPreProcessor != null) {
//                    parameters.add(parameterPreProcessor.apply(changedField.fieldName(), changedField.value()));
//                } else {
//                    parameters.add(changedField.value());
//                }
//            } else {
//                // Nested DTO - if not specifying its own table, it inherits the table from the parent DTO
//                if (changedField.value() != null
//                        && ChangeTrackingDto.class.isAssignableFrom(changedField.value().getClass()) &&
//                        !(DbTableChangeTrackingDto.class.isAssignableFrom(changedField.value().getClass()))) {
//                    // Nested DTO that does not have its own table - merge it into the current record
//                    mergeChanges((ChangeTrackingDto) changedField.value());
//                }
//            }
//        }
//    }
//
//    public boolean isEmpty() {
//        return parameters.isEmpty();
//    }
//
//    public PreparedStatementCreator getPreparedStatementCreator() {
//        if (parameters.size() == 0) {
//            throw new IllegalStateException("No parameters have been added to the SQL statement");
//        }
//
//        if (!StringUtils.isEmpty(whereClause)) {
//            sb.append(" WHERE ").append(whereClause);
//        }
//
//        final String sql = sb.toString();
//
//        return connection -> {
//            PreparedStatement ps = connection.prepareStatement(sql);
//            int index = 1;
//
//            // SET x = ?
//            for (Object parameter : parameters) {
//                index = setSqlParameter(ps, index, parameter);
//            }
//
//            // WHERE x = ?
//            if (!CollectionUtils.isEmpty(whereParameters)) {
//                for (Object parameter : whereParameters) {
//                    index = setSqlParameter(ps, index, parameter);
//                }
//            }
//
//            return ps;
//        };
//    }
//
//    private void addCommaSeparator() {
//        if (parameters.size() > 0) {
//            sb.append(", ");
//        }
//    }
//
//    private static int setSqlParameter(final PreparedStatement ps, final int index, final Object parameter) throws SQLException {
//        if (parameter == null) {
//            ps.setNull(index, java.sql.Types.NULL);
//        } else if (parameter instanceof BigDecimal) {
//            ps.setBigDecimal(index, (BigDecimal) parameter);
//        } else if (parameter instanceof Long) {
//            ps.setLong(index, (Long) parameter);
//        } else if (parameter instanceof Integer) {
//            ps.setInt(index, (Integer) parameter);
//        } else if (parameter instanceof Double) {
//            ps.setDouble(index, (Double) parameter);
//        } else if (parameter instanceof String) {
//            ps.setString(index, (String) parameter);
//        } else if (parameter instanceof Boolean) {
//            ps.setBoolean(index, (Boolean) parameter);
//        } else if (parameter instanceof ZonedDateTime) {
//            final ZonedDateTime zonedDateTime = (ZonedDateTime) parameter;
//            ps.setTimestamp(index, java.sql.Timestamp.valueOf(TimeMapper.toLocalDateTime(zonedDateTime)));
//        } else if (parameter.getClass().isEnum()) {
//            ps.setString(index, parameter.toString());
//        } else {
//            throw new IllegalArgumentException("Unsupported parameter type: " + parameter.getClass().getName());
//        }
//
//        return index + 1;
//    }
//}
