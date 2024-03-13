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
import org.eclipse.edc.spi.EdcException;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A non-persistent, concurrent store implementation.
 */
public class InMemoryDidEntryStore implements DidEntryStore {
    private static final int LOCK_TIMEOUT = 1000;

    private final AtomicReference<byte[]> cache = new AtomicReference<>(new byte[0]);
    private final Map<String, String> backingStore = new HashMap<>();

    private final ObjectMapper mapper;
    private final ReadWriteLock lock;

    public InMemoryDidEntryStore(ObjectMapper mapper) {
        this.mapper = mapper;
        lock = new ReentrantReadWriteLock(true);
        updateCache();

    }

    @Override
    public byte[] entries() {
        return cache.get();
    }

    @Override
    public void save(DidEntry entry) {
        requireNonNull(entry);
        writeLock(() -> {
            backingStore.put(entry.bpn(), entry.did());
            updateCache();
        });
    }

    @Override
    public void save(Stream<DidEntry> entries) {
        writeLock(() -> {
            entries.forEach(entry -> backingStore.put(entry.bpn(), entry.did()));
            updateCache();
        });
    }

    @Override
    public void delete(String bpn) {
        requireNonNull(bpn);
        writeLock(() -> {
            backingStore.remove(bpn);
            updateCache();
        });
        updateCache();
    }

    private void updateCache() {
        var bas = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(bas)) {
            gzip.write(mapper.writeValueAsBytes(backingStore));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cache.set(bas.toByteArray());
    }

    /**
     * Attempts to obtain a write lock.
     */
    private void writeLock(Runnable work) {
        try {
            if (!lock.writeLock().tryLock(LOCK_TIMEOUT, MILLISECONDS)) {
                throw new EdcException("Timeout acquiring write lock");
            }
            try {
                work.run();
            } finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            //noinspection ResultOfMethodCallIgnored
            Thread.interrupted();
            throw new EdcException(e);
        }
    }

}
