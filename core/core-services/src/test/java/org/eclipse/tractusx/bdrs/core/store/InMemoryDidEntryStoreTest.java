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

package org.eclipse.tractusx.bdrs.core.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDidEntryStoreTest {
    private static final String BPN = "BPN12345";
    private static final String DID = "did:web:localhost:member";
    private static final String DID2 = "did:web:localhost:member2";

    private ObjectMapper mapper;
    private InMemoryDidEntryStore store;

    @Test
    void verifySave() throws IOException {
        store.save(new DidEntry(BPN, DID));

        var bytes = store.entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID);
    }

    @Test
    void verifyStreamSave() throws IOException {
        store.save(Stream.of(new DidEntry(BPN, DID)));
        var bytes = store.entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID);
    }

    @Test
    void verifyUpdate() throws IOException {
        store.save(new DidEntry(BPN, DID));
        store.update(new DidEntry(BPN, DID2));

        var bytes = store.entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID2);
    }

    @Test
    void verifyRemove() throws IOException {
        store.save(new DidEntry(BPN, DID));
        store.delete(BPN);

        var bytes = store.entries();

        var entries = deserialize(bytes);

        assertThat(entries).isEmpty();
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        store = new InMemoryDidEntryStore(mapper);
    }

    private Map<String, String> deserialize(byte[] bytes) throws IOException {
        var stream = new GZIPInputStream(new ByteArrayInputStream(bytes));
        var decompressed = stream.readAllBytes();
        //noinspection unchecked
        return mapper.readValue(decompressed, Map.class);
    }

}
