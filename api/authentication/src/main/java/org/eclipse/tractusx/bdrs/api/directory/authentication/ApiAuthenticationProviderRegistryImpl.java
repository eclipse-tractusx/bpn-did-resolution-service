/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *  Copyright (c) 2025 Cofinity-X GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *       Cofinity-X GmbH
 *
 */
package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.api.auth.spi.ApiAuthenticationProvider;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationProviderRegistry;

import java.util.HashMap;
import java.util.Map;

public class ApiAuthenticationProviderRegistryImpl implements ApiAuthenticationProviderRegistry {
    private final Map<String, ApiAuthenticationProvider> providers = new HashMap<>();

    @Override
    public void register(String type, ApiAuthenticationProvider provider) {
        providers.put(type, provider);
    }

    @Override
    public ApiAuthenticationProvider resolve(String type) {
        return providers.get(type);
    }
}
