/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
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
