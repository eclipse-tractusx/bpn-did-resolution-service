package org.eclipse.tractusx.bdrs.sql.store;

import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import java.util.stream.Stream;

public class SqlDidEntryStore implements DidEntryStore {
    @Override
    public byte[] entries() {
        return new byte[0];
    }

    @Override
    public void save(DidEntry entry) {

    }

    @Override
    public void save(Stream<DidEntry> entries) {

    }

    @Override
    public void update(DidEntry entry) {
        DidEntryStore.super.update(entry);
    }

    @Override
    public void delete(String bpn) {

    }
}
