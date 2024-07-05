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

package org.eclipse.tractusx.bdrs.core.vault;

import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.security.Vault;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent, non-persistent vault.
 */
public class InMemoryVault implements Vault {
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public @Nullable String resolveSecret(String key) {
        return cache.get(key);
    }

    @Override
    public Result<Void> storeSecret(String key, String value) {
        cache.put(key, value);
        return Result.success();
    }

    @Override
    public Result<Void> deleteSecret(String key) {
        cache.remove(key);
        return Result.success();
    }
}
