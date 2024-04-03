/*
 *
 *   Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
 *
 *   See the NOTICE file(s) distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Apache License, Version 2.0 which is available at
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 *
 */

package org.eclipse.tractusx.bdrs.core;

import org.eclipse.edc.runtime.metamodel.annotation.BaseExtension;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ExecutorInstrumentation;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckService;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.tractusx.bdrs.core.health.HealthCheckServiceConfiguration;
import org.eclipse.tractusx.bdrs.core.health.HealthCheckServiceImpl;
import org.eclipse.tractusx.bdrs.core.store.InMemoryDidEntryStore;
import org.eclipse.tractusx.bdrs.core.vault.InMemoryVault;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import java.time.Duration;

import static org.eclipse.tractusx.bdrs.core.BdrsCoreExtension.NAME;

/**
 * Loads BDRS core services
 */
@BaseExtension
@Extension(NAME)
public class BdrsCoreExtension implements ServiceExtension {
    public static final String NAME = "BDRS Core";

    @Setting
    public static final String LIVENESS_PERIOD_SECONDS_SETTING = "edc.core.system.health.check.liveness-period";

    @Setting
    public static final String STARTUP_PERIOD_SECONDS_SETTING = "edc.core.system.health.check.startup-period";

    @Setting
    public static final String READINESS_PERIOD_SECONDS_SETTING = "edc.core.system.health.check.readiness-period";

    @Setting
    public static final String THREADPOOL_SIZE_SETTING = "edc.core.system.health.check.threadpool-size";

    private static final long DEFAULT_DURATION = 60;
    private static final int DEFAULT_TP_SIZE = 3;

    private HealthCheckServiceImpl healthCheckService;
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var config = getHealthCheckConfig(context);
        var instrumentation = new ExecutorInstrumentation() {
        };
        healthCheckService = new HealthCheckServiceImpl(config, instrumentation);
    }

    @Override
    public void start() {
        healthCheckService.start();
    }

    @Override
    public void shutdown() {
        healthCheckService.stop();
        ServiceExtension.super.shutdown();
    }

    @Provider
    public TypeManager typeManager() {
        if (typeManager == null) {
            typeManager = new TypeManager();
        }
        return typeManager;
    }

    @Provider
    public HealthCheckService healthCheckService() {
        return healthCheckService;
    }

    @Provider(isDefault = true)
    public DidEntryStore defaultDidEntryStore() {
        return new InMemoryDidEntryStore(typeManager().getMapper());
    }

    @Provider(isDefault = true)
    public Vault defaultVault() {
        return new InMemoryVault();
    }

    private HealthCheckServiceConfiguration getHealthCheckConfig(ServiceExtensionContext context) {
        return HealthCheckServiceConfiguration.Builder.newInstance()
                .livenessPeriod(Duration.ofSeconds(context.getSetting(LIVENESS_PERIOD_SECONDS_SETTING, DEFAULT_DURATION)))
                .startupStatusPeriod(Duration.ofSeconds(context.getSetting(STARTUP_PERIOD_SECONDS_SETTING, DEFAULT_DURATION)))
                .readinessPeriod(Duration.ofSeconds(context.getSetting(READINESS_PERIOD_SECONDS_SETTING, DEFAULT_DURATION)))
                .readinessPeriod(Duration.ofSeconds(context.getSetting(READINESS_PERIOD_SECONDS_SETTING, DEFAULT_DURATION)))
                .threadPoolSize(context.getSetting(THREADPOOL_SIZE_SETTING, DEFAULT_TP_SIZE))
                .build();
    }
}
