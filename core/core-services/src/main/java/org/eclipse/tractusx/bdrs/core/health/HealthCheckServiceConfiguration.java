/*
 * Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.tractusx.bdrs.core.health;

import org.eclipse.edc.spi.system.health.LivenessProvider;
import org.eclipse.edc.spi.system.health.ReadinessProvider;
import org.eclipse.edc.spi.system.health.StartupStatusProvider;

import java.time.Duration;

/**
 * Temporarily copied from the EDC to avoid dragging in Connector dependencies. This will be removed in the future.
 */
public class HealthCheckServiceConfiguration {
    public static final long DEFAULT_PERIOD_SECONDS = 60;
    public static final int DEFAULT_THREADPOOL_SIZE = 3;
    private int threadPoolSize = DEFAULT_THREADPOOL_SIZE;
    private Duration readinessPeriod = Duration.ofSeconds(DEFAULT_PERIOD_SECONDS);
    private Duration livenessPeriod = Duration.ofSeconds(DEFAULT_PERIOD_SECONDS);
    private Duration startupStatusPeriod = Duration.ofSeconds(DEFAULT_PERIOD_SECONDS);

    /**
     * how many threads should be used by the health check service for periodic polling
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * Time delay between before {@link ReadinessProvider}s are checked again.
     * Defaults to 10 seconds.
     */
    public Duration getReadinessPeriod() {
        return readinessPeriod;
    }

    /**
     * Time delay between before {@link LivenessProvider}s are checked again.
     * Defaults to 10 seconds.
     */
    public Duration getLivenessPeriod() {
        return livenessPeriod;
    }

    /**
     * Time delay between before {@link StartupStatusProvider}s are checked again.
     * Defaults to 10 seconds.
     */
    public Duration getStartupStatusPeriod() {
        return startupStatusPeriod;
    }

    public static final class Builder {
        private final HealthCheckServiceConfiguration config;

        private Builder() {
            config = new HealthCheckServiceConfiguration();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder readinessPeriod(Duration readinessPeriod) {
            config.readinessPeriod = readinessPeriod;
            return this;
        }

        public Builder livenessPeriod(Duration livenessPeriod) {
            config.livenessPeriod = livenessPeriod;
            return this;
        }

        public Builder startupStatusPeriod(Duration startupStatusPeriod) {
            config.startupStatusPeriod = startupStatusPeriod;
            return this;
        }

        public Builder threadPoolSize(int threadPoolSize) {
            config.threadPoolSize = threadPoolSize;
            return this;
        }

        public HealthCheckServiceConfiguration build() {
            return config;
        }
    }
}
