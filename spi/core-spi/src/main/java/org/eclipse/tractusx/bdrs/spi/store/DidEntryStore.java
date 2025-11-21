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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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



    /**
     * Checks if a BPN-DID mapping exists in the store.
     *
     * @param bpn The Business Process Number (BPN) for which the existence is to be checked.
     *            This is a unique identifier for a business process.
     * @return {@code true} if a BPN-DID mapping exists for the given BPN, {@code false} otherwise.
     */
    boolean exists(String bpn);


    /**
     * Checks if a DID mapping exists in the store.
     *
     * @param did The Decentralized Identifier (DID) for which the existence is to be checked.
     *            This is a unique identifier for digital identity.
     * @return {@code true} if a DID mapping exists for the given DID, {@code false} otherwise.
     */
    boolean existsByDid(String did);


    /**
     * Retrieves a {@link DidEntry} from the store based on the provided Business Process Number (BPN).
     *
     * @param bpn The Business Process Number (BPN) for which the {@link DidEntry} is to be retrieved.
     *            This is a unique identifier for a business process.
     * @return The {@link DidEntry} associated with the given BPN if it exists in the store.
     */
    Optional<DidEntry> getByBpn(String bpn);

    /**
     * Retrieves the cache for storing serialized JSON entries.
     *
     * @return The cache for storing serialized JSON entries.
     */
    AtomicReference<byte[]> getCache();


    /**
     * Retrieves a {@link DidEntry} from the store based on the provided Decentralized Identifier (DID).
     *
     * @param did The Decentralized Identifier (DID) for which the {@link DidEntry} is to be retrieved.
     *            This is a unique identifier for digital identity.
     * @return The {@link DidEntry} associated with the given DID if it exists in the store.
     */
    Optional<DidEntry> getByDid(String did);
}
