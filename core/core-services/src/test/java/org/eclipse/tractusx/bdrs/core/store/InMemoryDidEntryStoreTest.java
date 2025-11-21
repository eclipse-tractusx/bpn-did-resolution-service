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

package org.eclipse.tractusx.bdrs.core.store;

import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStoreTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
class InMemoryDidEntryStoreTest extends DidEntryStoreTestBase {
    private final InMemoryDidEntryStore store = new InMemoryDidEntryStore(mapper);

    @Override
    protected DidEntryStore getStore() {
        return store;
    }



    @Test
    void verifyExists() {
        String bpn = "BPNL0000000000045";
        String did = "did:web:localhost:" + bpn;
        store.save(new DidEntry(bpn, did));
        Assertions.assertTrue(store.exists(bpn));
        Assertions.assertFalse(store.exists("BPNL0000000000011"));
    }

    @Test
    void testAudit() {
        String bpn = "BPNL0000000000045";
        String did = "did:web:localhost:" + bpn;
        store.save(new DidEntry(bpn, did));

        //update entry
        String updatedDid = "did:web:updatedHost:" + bpn;
        store.update(new DidEntry(bpn, updatedDid));

        //delete entry
        store.delete(bpn);
    }

    @Test
    void verifyExistsByDid() {
        String bpn = "BPNL0000000000045";
        String did = "did:web:localhost:" + bpn;
        store.save(new DidEntry(bpn, did));
        Assertions.assertTrue(store.existsByDid(did));
        Assertions.assertFalse(store.existsByDid("did:web:localhost_some_random_host:BPNL0000000000011"));
    }

    @Test
    void verifyGetByDid() {
        String bpn = "BPNL0000000000045";
        String did = "did:web:localhost:" + bpn;
        DidEntry entry = new DidEntry(bpn, did);
        store.save(entry);
        Assertions.assertTrue(store.getByDid(did).isPresent());
        Assertions.assertEquals(entry, store.getByDid(did).get());
        Assertions.assertTrue(store.getByDid("did:web:localhost_some_random_host:BPNL0000000000011").isEmpty());
    }
}
