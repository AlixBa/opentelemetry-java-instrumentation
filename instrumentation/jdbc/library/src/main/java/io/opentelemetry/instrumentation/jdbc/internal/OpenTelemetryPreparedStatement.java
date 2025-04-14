/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2017-2021 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.opentelemetry.instrumentation.jdbc.internal;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.jdbc.internal.dbinfo.DbInfo;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("OverloadMethodsDeclarationOrder")
class OpenTelemetryPreparedStatement<S extends PreparedStatement> extends OpenTelemetryStatement<S>
    implements PreparedStatement {
  private final Map<Integer, Object> parameters;

  public OpenTelemetryPreparedStatement(
      S delegate,
      OpenTelemetryConnection connection,
      DbInfo dbInfo,
      String query,
      Instrumenter<DbRequest, Void> instrumenter) {
    super(delegate, connection, dbInfo, query, instrumenter);
    this.parameters = new HashMap<>();
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    return new OpenTelemetryResultSet(wrapCall(query, delegate::executeQuery), this);
  }

  @Override
  public int executeUpdate() throws SQLException {
    return wrapCall(query, delegate::executeUpdate);
  }

  @Override
  public boolean execute() throws SQLException {
    return wrapCall(query, delegate::execute);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    delegate.setNull(parameterIndex, sqlType);
    parameters.put(parameterIndex, null);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    delegate.setNull(parameterIndex, sqlType, typeName);
    parameters.put(parameterIndex, null);
  }

  @Override
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    delegate.setBoolean(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setByte(int parameterIndex, byte x) throws SQLException {
    delegate.setByte(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setShort(int parameterIndex, short x) throws SQLException {
    delegate.setShort(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setInt(int parameterIndex, int x) throws SQLException {
    delegate.setInt(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setLong(int parameterIndex, long x) throws SQLException {
    delegate.setLong(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setFloat(int parameterIndex, float x) throws SQLException {
    delegate.setFloat(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setDouble(int parameterIndex, double x) throws SQLException {
    delegate.setDouble(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    delegate.setBigDecimal(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setString(int parameterIndex, String x) throws SQLException {
    delegate.setString(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    delegate.setBytes(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setDate(int parameterIndex, Date x) throws SQLException {
    delegate.setDate(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    delegate.setDate(parameterIndex, x, cal);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setTime(int parameterIndex, Time x) throws SQLException {
    delegate.setTime(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    delegate.setTime(parameterIndex, x, cal);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    delegate.setTimestamp(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    delegate.setTimestamp(parameterIndex, x, cal);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    delegate.setAsciiStream(parameterIndex, x, length);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    delegate.setAsciiStream(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    delegate.setAsciiStream(parameterIndex, x, length);
    parameters.put(parameterIndex, x);
  }

  @Override
  @Deprecated
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    delegate.setUnicodeStream(parameterIndex, x, length);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    delegate.setBinaryStream(parameterIndex, x, length);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    delegate.setBinaryStream(parameterIndex, x, length);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    delegate.setBinaryStream(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    delegate.setObject(parameterIndex, x, targetSqlType);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setObject(int parameterIndex, Object x) throws SQLException {
    delegate.setObject(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
      throws SQLException {
    delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void addBatch() throws SQLException {
    delegate.addBatch();
    batchSize++;
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, int length)
      throws SQLException {
    delegate.setCharacterStream(parameterIndex, reader, length);
    parameters.put(parameterIndex, reader);
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    delegate.setCharacterStream(parameterIndex, reader);
    parameters.put(parameterIndex, reader);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, long length)
      throws SQLException {
    delegate.setCharacterStream(parameterIndex, reader, length);
    parameters.put(parameterIndex, reader);
  }

  @Override
  public void setRef(int parameterIndex, Ref x) throws SQLException {
    delegate.setRef(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    delegate.setBlob(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    delegate.setBlob(parameterIndex, inputStream);
    parameters.put(parameterIndex, inputStream);
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream, long length)
      throws SQLException {
    delegate.setBlob(parameterIndex, inputStream, length);
    parameters.put(parameterIndex, inputStream);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setClob(int parameterIndex, Clob x) throws SQLException {
    delegate.setClob(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    delegate.setClob(parameterIndex, reader);
    parameters.put(parameterIndex, reader);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    delegate.setClob(parameterIndex, reader, length);
    parameters.put(parameterIndex, reader);
  }

  @Override
  public void setArray(int parameterIndex, Array x) throws SQLException {
    delegate.setArray(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return delegate.getMetaData();
  }

  @Override
  public void setURL(int parameterIndex, URL x) throws SQLException {
    delegate.setURL(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return delegate.getParameterMetaData();
  }

  @Override
  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    delegate.setRowId(parameterIndex, x);
    parameters.put(parameterIndex, x);
  }

  @Override
  public void setNString(int parameterIndex, String value) throws SQLException {
    delegate.setNString(parameterIndex, value);
    parameters.put(parameterIndex, value);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setNCharacterStream(int parameterIndex, Reader value, long length)
      throws SQLException {
    delegate.setNCharacterStream(parameterIndex, value, length);
    parameters.put(parameterIndex, value);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    delegate.setNCharacterStream(parameterIndex, value);
    parameters.put(parameterIndex, value);
  }

  @Override
  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    delegate.setNClob(parameterIndex, value);
    parameters.put(parameterIndex, value);
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    delegate.setNClob(parameterIndex, reader, length);
    parameters.put(parameterIndex, reader);
  }

  @SuppressWarnings("UngroupedOverloads")
  @Override
  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    delegate.setNClob(parameterIndex, reader);
    parameters.put(parameterIndex, reader);
  }

  @Override
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    delegate.setSQLXML(parameterIndex, xmlObject);
    parameters.put(parameterIndex, xmlObject);
  }

  @Override
  public void clearParameters() throws SQLException {
    delegate.clearParameters();
    parameters.clear();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    return wrapBatchCall(delegate::executeBatch);
  }

  @Override
  protected <T, E extends Exception> T wrapCall(String sql, ThrowingSupplier<T, E> callable)
      throws E {
    DbRequest request = DbRequest.create(dbInfo, sql, null, parameters);
    return wrapCall(request, callable);
  }

  private <T, E extends Exception> T wrapBatchCall(ThrowingSupplier<T, E> callable) throws E {
    DbRequest request = DbRequest.create(dbInfo, query, batchSize, parameters);
    return wrapCall(request, callable);
  }

  // JDBC 4.2

  @Override
  public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength)
      throws SQLException {
    parameters.put(parameterIndex, x);
    delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
  }

  @Override
  public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
    parameters.put(parameterIndex, x);
    delegate.setObject(parameterIndex, x, targetSqlType);
  }

  @Override
  public long executeLargeUpdate() throws SQLException {
    return wrapCall(query, delegate::executeLargeUpdate);
  }
}
