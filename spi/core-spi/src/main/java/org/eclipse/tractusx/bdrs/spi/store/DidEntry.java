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

package org.eclipse.tractusx.bdrs.spi.store;

import static java.util.Objects.requireNonNull;

/**
 * A BPN to DID mapping.
 */
public record DidEntry(String bpn, String did) {
    public DidEntry {
        requireNonNull(bpn, "bpn");
        requireNonNull(did, "did");
    }
}
