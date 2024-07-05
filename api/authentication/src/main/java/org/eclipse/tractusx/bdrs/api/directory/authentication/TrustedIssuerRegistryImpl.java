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

package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TrustedIssuerRegistryImpl implements TrustedIssuerRegistry {
    private final Map<String, Issuer> store = new HashMap<>();

    @Override
    public void addIssuer(Issuer issuer) {
        store.put(issuer.id(), issuer);
    }

    @Override
    public Issuer getById(String id) {
        return store.get(id);
    }

    @Override
    public Collection<Issuer> getTrustedIssuers() {
        return store.values();
    }
}
