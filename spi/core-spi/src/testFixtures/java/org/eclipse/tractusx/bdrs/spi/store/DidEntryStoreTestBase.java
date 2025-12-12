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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.spi.EdcException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class DidEntryStoreTestBase {
    private static final String BPN = "BPN12345";
    private static final String BPN2 = "BPN67890";
    private static final String DID = "did:web:localhost:member";
    private static final String DID2 = "did:web:localhost:member2";
    protected final ObjectMapper mapper = new ObjectMapper();

    @Test
    void save_whenNotExists() {
        getStore().save(new DidEntry(BPN, DID));

        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries).containsEntry(BPN, DID);

        //verify data must be present in cache
        assertThat(deserialize(getStore().getCache().get())).containsEntry(BPN, DID);
    }

    @Test
    void save_whenExists_shouldUpdate() {
        getStore().save(new DidEntry(BPN2, DID2));

        // already exists, should overwrite
        getStore().save(new DidEntry(BPN2, "did:web:newdid"));
        assertThat(deserialize(getStore().entries())).hasSize(1)
                .containsEntry(BPN2, "did:web:newdid");
    }

    @Test
    void saveStream_whenNoneExist() {
        getStore().save(Stream.of(new DidEntry(BPN, DID)));
        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID);
    }

    @Test
    void saveStream_whenSomeExist() {
        getStore().save(new DidEntry(BPN, DID));
        assertThatThrownBy(() ->
                getStore().save(Stream.of(new DidEntry(BPN, "did:web:newdid"), new DidEntry(BPN2, DID2)))
        ).isInstanceOf(EdcException.class);
    }

    @Test
    void update_whenExists() {
        getStore().save(new DidEntry(BPN, DID));
        getStore().update(new DidEntry(BPN, DID2));

        var bytes = getStore().entries();
        var entries = deserialize(bytes);

        assertThat(entries).hasSize(1);

        assertThat(deserialize(getStore().getCache().get())).containsEntry(BPN, DID2);

    }

    @Test
    void update_whenNotExists_shouldCreate() {
        getStore().update(new DidEntry(BPN, DID2));

        var bytes = getStore().entries();
        var entries = deserialize(bytes);

        assertThat(entries).hasSize(1);
        assertThat(deserialize(getStore().getCache().get())).containsEntry(BPN, DID2);

    }

    @Test
    void remove_whenExists() {
        getStore().save(new DidEntry(BPN, DID));
        getStore().delete(BPN);

        var bytes = getStore().entries();
        var entries = deserialize(bytes);

        assertThat(entries).isEmpty();
    }

    @Test
    void remove_whenNotExists() {
        assertThatNoException().isThrownBy(() -> getStore().delete(BPN));

        var bytes = getStore().entries();
        var entries = deserialize(bytes);

        assertThat(entries).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = { 1000, 10_000, 30_000, 50_000, 100_000 })
    void saveAndGetManyEntries(int size) {
        var allEntries = IntStream.range(0, size)
                .mapToObj(i -> new DidEntry("bpn" + i, "did:web:participant" + i))
                .toList();

        getStore().save(allEntries.stream());

        var loadedEntries = deserialize(getStore().entries());
        assertThat(loadedEntries).hasSize(size);
    }

    protected abstract DidEntryStore getStore();

    private Map<String, String> deserialize(byte[] bytes) {
        try {
            GZIPInputStream stream = null;
            stream = new GZIPInputStream(new ByteArrayInputStream(bytes));
            var decompressed = stream.readAllBytes();
            //noinspection unchecked
            return mapper.readValue(decompressed, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
