package org.eclipse.tractusx.bdrs.spi.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.junit.annotations.PostgresqlIntegrationTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@PostgresqlIntegrationTest
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

        assertThat(entries.get(BPN)).isEqualTo(DID);
    }

    @Test
    void save_whenExists_shouldUpdate() {
        getStore().save(Stream.of(new DidEntry(BPN, DID), new DidEntry(BPN2, DID2)));

        // already exists, should overwrite
        getStore().save(new DidEntry(BPN2, "did:web:newdid"));
        assertThat(deserialize(getStore().entries())).hasSize(2)
                .containsEntry(BPN, DID)
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
        getStore().save(Stream.of(new DidEntry(BPN, "did:web:newdid"), new DidEntry(BPN2, DID2)));
        var bytes = getStore().entries();

        var entries = deserialize(bytes);

        assertThat(entries.get(BPN)).isEqualTo("did:web:newdid");
    }

    @Test
    void update_whenExists() {
        getStore().save(new DidEntry(BPN, DID));
        getStore().update(new DidEntry(BPN, DID2));

        var bytes = getStore().entries();
        var entries = deserialize(bytes);

        assertThat(entries).hasSize(1).containsEntry(BPN, DID2);
    }

    @Test
    void update_whenNotExists_shouldCreate() {
        getStore().update(new DidEntry(BPN, DID2));

        var bytes = getStore().entries();
        var entries = deserialize(bytes);

        assertThat(entries).hasSize(1).containsEntry(BPN, DID2);
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
