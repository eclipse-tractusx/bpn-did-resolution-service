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

package org.eclipse.tractusx.bdrs.spi.store;

import java.util.stream.Stream;

/**
 * Stores {@link DidEntry}s.
 */
public interface DidEntryStore {

    /**
     * Returns all serialized JSON entries as a compressed (GZIP) byte array.
     */
    byte[] entries();

    /**
     * Adds a new BPN-DID mapping.
     */
    void save(DidEntry entry);

    /**
     * Bulk adds a new BPN-DID mapping.
     */
    void save(Stream<DidEntry> entries);

    /**
     * Updates a BPN-DID mapping.
     */
    default void update(DidEntry entry) {
        this.save(entry);
    }

    /**
     * Removes a BPN-DID mapping.
     */
    void delete(String bpn);

}
