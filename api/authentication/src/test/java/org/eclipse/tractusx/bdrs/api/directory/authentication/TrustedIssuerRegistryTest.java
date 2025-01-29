/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
 *
 */

package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TrustedIssuerRegistryTest {

    private final TrustedIssuerRegistryImpl registry = new TrustedIssuerRegistryImpl();

    @Test
    void trustedIssuer() {
        var issuer = new Issuer("test-id", Map.of());

        registry.register(issuer, "test-type1");
        registry.register(issuer, "test-type2");

        assertThat(registry.getSupportedTypes(issuer)).containsExactly("test-type1", "test-type2");
    }

    @Test
    void invalidIssuer() {
        var issuer = new Issuer("test-id", Map.of());

        assertThat(registry.getSupportedTypes(issuer)).isEmpty();
    }

}