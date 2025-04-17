/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.jdbc.internal;

import static io.opentelemetry.instrumentation.api.internal.SemconvStability.emitStableDatabaseSemconv;
import static io.opentelemetry.instrumentation.jdbc.internal.JdbcInstrumenterFactory.createStatementInstrumenter;
import static io.opentelemetry.instrumentation.testing.junit.db.SemconvStabilityUtil.maybeStable;
import static io.opentelemetry.instrumentation.testing.junit.db.SemconvStabilityUtil.maybeStableDbSystemName;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.semconv.ServerAttributes.SERVER_ADDRESS;
import static io.opentelemetry.semconv.ServerAttributes.SERVER_PORT;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_CONNECTION_STRING;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_NAME;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_OPERATION;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_QUERY_PARAMETER;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_SQL_TABLE;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_STATEMENT;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_SYSTEM;
import static io.opentelemetry.semconv.incubating.DbIncubatingAttributes.DB_USER;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.jdbc.TestConnection;
import io.opentelemetry.instrumentation.jdbc.internal.dbinfo.DbInfo;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryConnectionTest {

  @RegisterExtension
  private static final InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @Test
  void testVerifyCreateStatement() throws SQLException {
    Instrumenter<DbRequest, Void> instrumenter =
        createStatementInstrumenter(testing.getOpenTelemetry());
    DbInfo dbInfo = getDbInfo();
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), dbInfo, instrumenter);
    String query = "SELECT * FROM users";
    Statement statement = connection.createStatement();

    testing.runWithSpan(
        "parent",
        () -> {
          assertThat(statement.execute(query)).isTrue();
        });

    jdbcTraceAssertion(dbInfo, query);

    statement.close();
    connection.close();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testVerifyCreateStatementReturnsOtelWrapper() throws Exception {
    OpenTelemetry ot = OpenTelemetry.propagating(ContextPropagators.noop());
    Instrumenter<DbRequest, Void> instrumenter = createStatementInstrumenter(ot);
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), DbInfo.DEFAULT, instrumenter);

    assertThat(connection.createStatement()).isInstanceOf(OpenTelemetryStatement.class);
    assertThat(connection.createStatement(0, 0)).isInstanceOf(OpenTelemetryStatement.class);
    assertThat(connection.createStatement(0, 0, 0)).isInstanceOf(OpenTelemetryStatement.class);
    assertThat(((OpenTelemetryStatement<Statement>) connection.createStatement()).instrumenter)
        .isEqualTo(instrumenter);

    connection.close();
  }

  @Test
  void testVerifyPrepareStatement() throws SQLException {
    Instrumenter<DbRequest, Void> instrumenter =
        createStatementInstrumenter(testing.getOpenTelemetry());
    DbInfo dbInfo = getDbInfo();
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), dbInfo, instrumenter);
    String query = "SELECT * FROM users";
    PreparedStatement statement = connection.prepareStatement(query);

    testing.runWithSpan(
        "parent",
        () -> {
          assertThat(statement.execute()).isTrue();
          ResultSet resultSet = statement.getResultSet();
          assertThat(resultSet).isInstanceOf(OpenTelemetryResultSet.class);
          assertThat(resultSet.getStatement()).isEqualTo(statement);
        });

    jdbcTraceAssertion(dbInfo, query);

    statement.close();
    connection.close();
  }

  @Test
  void testVerifyPrepareStatementQuery() throws SQLException {
    Instrumenter<DbRequest, Void> instrumenter =
        createStatementInstrumenter(testing.getOpenTelemetry());
    DbInfo dbInfo = getDbInfo();
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), dbInfo, instrumenter);
    String query = "SELECT * FROM users";
    PreparedStatement statement = connection.prepareStatement(query);

    testing.runWithSpan(
        "parent",
        () -> {
          ResultSet resultSet = statement.executeQuery();
          assertThat(resultSet).isInstanceOf(OpenTelemetryResultSet.class);
          assertThat(resultSet.getStatement()).isEqualTo(statement);
        });

    jdbcTraceAssertion(dbInfo, query);

    statement.close();
    connection.close();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testVerifyPrepareStatementReturnsOtelWrapper() throws Exception {
    OpenTelemetry ot = OpenTelemetry.propagating(ContextPropagators.noop());
    Instrumenter<DbRequest, Void> instrumenter = createStatementInstrumenter(ot);
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), DbInfo.DEFAULT, instrumenter);
    String query = "SELECT * FROM users";

    assertThat(connection.prepareStatement(query))
        .isInstanceOf(OpenTelemetryPreparedStatement.class);
    assertThat(connection.prepareStatement(query, new int[] {0}))
        .isInstanceOf(OpenTelemetryPreparedStatement.class);
    assertThat(connection.prepareStatement(query, new String[] {"id"}))
        .isInstanceOf(OpenTelemetryPreparedStatement.class);
    assertThat(connection.prepareStatement(query, 0))
        .isInstanceOf(OpenTelemetryPreparedStatement.class);
    assertThat(connection.prepareStatement(query, 0, 0))
        .isInstanceOf(OpenTelemetryPreparedStatement.class);
    assertThat(connection.prepareStatement(query, 0, 0, 0))
        .isInstanceOf(OpenTelemetryPreparedStatement.class);
    assertThat(
            ((OpenTelemetryStatement<Statement>) connection.prepareStatement(query)).instrumenter)
        .isEqualTo(instrumenter);

    connection.close();
  }

  @Test
  void testVerifyPrepareCall() throws SQLException {
    Instrumenter<DbRequest, Void> instrumenter =
        createStatementInstrumenter(testing.getOpenTelemetry());
    DbInfo dbInfo = getDbInfo();
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), dbInfo, instrumenter);
    String query = "SELECT * FROM users";
    PreparedStatement statement = connection.prepareCall(query);

    testing.runWithSpan(
        "parent",
        () -> {
          assertThat(statement.execute()).isTrue();
        });

    jdbcTraceAssertion(dbInfo, query);

    statement.close();
    connection.close();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testVerifyPrepareCallReturnsOtelWrapper() throws Exception {
    OpenTelemetry ot = OpenTelemetry.propagating(ContextPropagators.noop());
    Instrumenter<DbRequest, Void> instrumenter = createStatementInstrumenter(ot);
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), DbInfo.DEFAULT, instrumenter);
    String query = "SELECT * FROM users";

    assertThat(connection.prepareCall(query)).isInstanceOf(OpenTelemetryCallableStatement.class);

    assertThat(connection.prepareCall(query)).isInstanceOf(OpenTelemetryCallableStatement.class);
    assertThat(connection.prepareCall(query, 0, 0))
        .isInstanceOf(OpenTelemetryCallableStatement.class);
    assertThat(connection.prepareCall(query, 0, 0, 0))
        .isInstanceOf(OpenTelemetryCallableStatement.class);
    assertThat(((OpenTelemetryStatement<Statement>) connection.prepareCall(query)).instrumenter)
        .isEqualTo(instrumenter);

    connection.close();
  }

  // https://github.com/open-telemetry/semantic-conventions/pull/2093
  @SuppressWarnings("deprecation")
  @Test
  void testVerifyPrepareStatementParameters() throws SQLException {
    Instrumenter<DbRequest, Void> instrumenter =
        createStatementInstrumenter(testing.getOpenTelemetry());
    DbInfo dbInfo = getDbInfo();
    OpenTelemetryConnection connection =
        new OpenTelemetryConnection(new TestConnection(), dbInfo, instrumenter);
    String query = "SELECT * FROM users WHERE id=? AND name=$2 AND age=3";
    String sanitized = "SELECT * FROM users WHERE id=? AND name=$2 AND age=?";
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setInt(1, 1);
    statement.setString(2, "bob");

    testing.runWithSpan(
        "parent",
        () -> {
          ResultSet resultSet = statement.executeQuery();
          assertThat(resultSet).isInstanceOf(OpenTelemetryResultSet.class);
          assertThat(resultSet.getStatement()).isEqualTo(statement);
        });

    jdbcTraceAssertion(
        dbInfo,
        sanitized,
        equalTo(DB_QUERY_PARAMETER.getAttributeKey("0"), "1"),
        equalTo(DB_QUERY_PARAMETER.getAttributeKey("$2"), "'bob'"),
        equalTo(DB_QUERY_PARAMETER.getAttributeKey("1"), "3"));

    statement.close();
    connection.close();
  }

  private static DbInfo getDbInfo() {
    return DbInfo.builder()
        .system("my_system")
        .subtype("my_sub_type")
        .shortUrl("my_connection_string")
        .user("my_user")
        .name("my_name")
        .db("my_db")
        .host("my_host")
        .port(1234)
        .build();
  }

  @SuppressWarnings("deprecation") // old semconv
  private static void jdbcTraceAssertion(
      DbInfo dbInfo, String query, AttributeAssertion... assertions) {
    List<AttributeAssertion> baseAttributeAssertions =
        Arrays.asList(
            equalTo(maybeStable(DB_SYSTEM), maybeStableDbSystemName(dbInfo.getSystem())),
            equalTo(maybeStable(DB_NAME), dbInfo.getName()),
            equalTo(DB_USER, emitStableDatabaseSemconv() ? null : dbInfo.getUser()),
            equalTo(
                DB_CONNECTION_STRING, emitStableDatabaseSemconv() ? null : dbInfo.getShortUrl()),
            equalTo(maybeStable(DB_STATEMENT), query),
            equalTo(maybeStable(DB_OPERATION), "SELECT"),
            equalTo(maybeStable(DB_SQL_TABLE), "users"),
            equalTo(SERVER_ADDRESS, dbInfo.getHost()),
            equalTo(SERVER_PORT, dbInfo.getPort()));

    List<AttributeAssertion> additionAttributeAssertions = Arrays.asList(assertions);

    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span -> span.hasName("parent").hasKind(SpanKind.INTERNAL).hasNoParent(),
                span ->
                    span.hasName("SELECT my_name.users")
                        .hasKind(SpanKind.CLIENT)
                        .hasParent(trace.getSpan(0))
                        .hasAttributesSatisfyingExactly(
                            Stream.concat(
                                    baseAttributeAssertions.stream(),
                                    additionAttributeAssertions.stream())
                                .collect(Collectors.toList()))));
  }
}
