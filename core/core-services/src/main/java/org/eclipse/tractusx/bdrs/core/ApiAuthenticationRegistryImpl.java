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

import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ApiAuthenticationRegistryImpl implements ApiAuthenticationRegistry {

    private static final AuthenticationService ALL_PASS = headers -> true;
    private final Map<String, AuthenticationService> services = new HashMap<>();

    public ApiAuthenticationRegistryImpl() {
    }

    @Override
    public void register(String context, AuthenticationService service) {
        services.put(context, service);
    }

    @Override
    public @NotNull AuthenticationService resolve(String context) {
        return services.getOrDefault(context, ALL_PASS);
    }

    @Override
    public boolean hasService(String context) {
        return services.containsKey(context);
    }
}
