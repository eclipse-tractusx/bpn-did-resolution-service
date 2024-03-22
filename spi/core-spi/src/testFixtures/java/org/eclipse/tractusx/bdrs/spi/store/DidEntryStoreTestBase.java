package org.eclipse.tractusx.bdrs.spi.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class DidEntryStoreTestBase {
    private static final String BPN = "BPN12345";
    private static final String DID = "did:web:localhost:member";
    private static final String DID2 = "did:web:localhost:member2";
    protected final ObjectMapper mapper = new ObjectMapper();

    @Test
    void verifySave() throws IOException {
        getStore().save(new DidEntry(BPN, DID));

        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID);
    }



    @Test
    void verifyStreamSave() throws IOException {
        getStore().save(Stream.of(new DidEntry(BPN, DID)));
        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID);
    }

    @Test
    void verifyUpdate() throws IOException {
        getStore().save(new DidEntry(BPN, DID));
        getStore().update(new DidEntry(BPN, DID2));

        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo(DID2);
    }

    @Test
    void verifyRemove() throws IOException {
        getStore().save(new DidEntry(BPN, DID));
        getStore().delete(BPN);

        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries).isEmpty();
    }

    @BeforeEach
    void setUp() {

    }

    protected abstract DidEntryStore getStore();
    private Map<String, String> deserialize(byte[] bytes) throws IOException {
        var stream = new GZIPInputStream(new ByteArrayInputStream(bytes));
        var decompressed = stream.readAllBytes();
        //noinspection unchecked
        return mapper.readValue(decompressed, Map.class);
    }
}
