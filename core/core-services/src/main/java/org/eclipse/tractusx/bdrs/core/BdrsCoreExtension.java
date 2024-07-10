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

package org.eclipse.tractusx.bdrs.core;

import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.json.JacksonTypeManager;
import org.eclipse.edc.runtime.metamodel.annotation.BaseExtension;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckService;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.tractusx.bdrs.core.health.HealthCheckServiceImpl;
import org.eclipse.tractusx.bdrs.core.store.InMemoryDidEntryStore;
import org.eclipse.tractusx.bdrs.core.vault.InMemoryVault;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import static org.eclipse.tractusx.bdrs.core.BdrsCoreExtension.NAME;

/**
 * Loads BDRS core services
 */
@BaseExtension
@Extension(NAME)
public class BdrsCoreExtension implements ServiceExtension {
    public static final String NAME = "BDRS Core";

    private HealthCheckServiceImpl healthCheckService;
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        healthCheckService = new HealthCheckServiceImpl();
    }

    @Provider
    public TypeManager typeManager() {
        if (typeManager == null) {
            typeManager = new JacksonTypeManager();
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

    @Provider
    public ApiAuthenticationRegistry apiAuthenticationRegistry() {
        return new ApiAuthenticationRegistryImpl();
    }
}
