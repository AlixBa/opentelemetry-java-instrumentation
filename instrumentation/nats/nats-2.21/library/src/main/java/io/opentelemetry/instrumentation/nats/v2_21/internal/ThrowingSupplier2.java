/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.nats.v2_21.internal;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@FunctionalInterface
public interface ThrowingSupplier2<T, E extends Throwable, E2 extends Throwable> {

  T call() throws E, E2;
}
