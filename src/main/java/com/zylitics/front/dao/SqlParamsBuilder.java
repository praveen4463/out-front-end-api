package com.zylitics.front.dao;

import com.zylitics.front.util.DateTimeUtil;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.JDBCType;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

class SqlParamsBuilder {
  
  private final Map<String, SqlParameterValue> params = new HashMap<>();
  
  SqlParamsBuilder(int userId) {
    withInteger("zluser_id", userId);
  }
  
  SqlParamsBuilder(int projectId, int userId) {
    withInteger("bt_project_id", projectId);
    withInteger("zluser_id", userId);
  }
  
  SqlParamsBuilder() {}
  
  SqlParamsBuilder withProject(int projectId) {
    params.put("bt_project_id", new SqlParameterValue(Types.INTEGER, projectId));
    return this;
  }
  
  SqlParamsBuilder withCreateDate() {
    params.put("create_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE,
        DateTimeUtil.getCurrentUTC()));
    return this;
  }
  
  SqlParamsBuilder withInteger(String name, int value) {
    params.put(name, new SqlParameterValue(Types.INTEGER, value));
    return this;
  }
  
  SqlParamsBuilder withInteger(String name, Integer value) {
    params.put(name, new SqlParameterValue(Types.INTEGER, value));
    return this;
  }
  
  SqlParamsBuilder withOther(String name, Object value) {
    params.put(name, new SqlParameterValue(Types.OTHER, value));
    return this;
  }
  
  SqlParamsBuilder withVarchar(String name, String value) {
    params.put(name, new SqlParameterValue(Types.VARCHAR, value));
    return this;
  }
  
  SqlParamsBuilder withBoolean(String name, boolean value) {
    params.put(name, new SqlParameterValue(Types.BOOLEAN, value));
    return this;
  }
  
  SqlParamsBuilder withTimestampTimezone(String name, OffsetDateTime value) {
    params.put(name, new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE, value));
    return this;
  }
  
  SqlParamsBuilder withBigint(String name, long value) {
    params.put(name, new SqlParameterValue(Types.BIGINT, value));
    return this;
  }
  
  SqlParamsBuilder withArray(String name, Object[] value,
                             @SuppressWarnings("SameParameterValue") JDBCType elementType) {
    params.put(name, new SqlParameterValue(Types.ARRAY, elementType.getName()
        , new ArraySqlTypeValue(value)));
    return this;
  }
  
  SqlParameterSource build() {
    return new MapSqlParameterSource(params);
  }
}
