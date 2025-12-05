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

package org.eclipse.tractusx.bdrs.api.management;

import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagementApiControllerTest extends RestControllerTestBase {
    private static final String BPN = "BPN123";
    private static final String DID = "did:web:localhost:member";

    private DidEntryStore store;

    private Monitor monitor;

    @Test
    void verifyGetEntries() throws IOException {
        var entries = Map.of(BPN, new DidEntry(BPN, DID));
        var serialized = objectMapper.writeValueAsString(entries);
        var serializedStream = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(serializedStream)) {
            gzip.write(serialized.getBytes());
        }

        when(store.entries()).thenReturn(serializedStream.toByteArray());

        var expectedJson = new JsonPath(serialized);
        baseRequest().get("")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("", equalTo(expectedJson.getMap("")));
    }

    @Test
    void verifySave() throws IOException {


        String bpn = "BPNL" + RandomStringUtils.secure().nextAlphabetic(12).toUpperCase();
        String did = "did:web:localhost:" + bpn;
        var serialized = objectMapper.writeValueAsString(new DidEntry(bpn, did));

        var captor = ArgumentCaptor.forClass(DidEntry.class);

        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .post("")
                .then()
                .statusCode(204);

        verify(store).save(captor.capture());

        assertThat(captor.getValue().bpn()).isEqualTo(bpn);
        assertThat(captor.getValue().did()).isEqualTo(did);
    }

    @Test
    void verifyUpdate() throws IOException {

        String bpn = "BPNL" + RandomStringUtils.secure().nextAlphabetic(12).toUpperCase();
        String did = "did:web:localhost:" + bpn;
        var serialized = objectMapper.writeValueAsString(new DidEntry(bpn, did));
        when(store.exists(bpn)).thenReturn(false);
        var captor = ArgumentCaptor.forClass(DidEntry.class);

        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .put("")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        //add data
        serialized = objectMapper.writeValueAsString(new DidEntry(bpn, did));
        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .post("")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
        captor = ArgumentCaptor.forClass(DidEntry.class);

        //update data without change in did
        serialized = objectMapper.writeValueAsString(new DidEntry(bpn, did));
        when(store.exists(bpn)).thenReturn(true);
        when(store.getByBpn(bpn)).thenReturn(Optional.of(new DidEntry(bpn, did)));
        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .put("")
                .then()
                .statusCode(204);
        verify(store, Mockito.never()).update(captor.capture());

        //update data
        String updatedDid = "did:web:someotherhost:" + bpn;
        serialized = objectMapper.writeValueAsString(new DidEntry(bpn, updatedDid));
        when(store.exists(bpn)).thenReturn(true);
        when(store.getByBpn(bpn)).thenReturn(Optional.of(new DidEntry(bpn, did)));
        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .put("")
                .then()
                .statusCode(204);

        verify(store).update(captor.capture());

        assertThat(captor.getValue().bpn()).isEqualTo(bpn);
        assertThat(captor.getValue().did()).isEqualTo(updatedDid);
    }

    @Test
    void verifyDelete() {
        var captor = ArgumentCaptor.forClass(String.class);

        baseRequest()
                .contentType(JSON)
                .delete("/" + BPN)
                .then()
                .statusCode(204);

        verify(store).delete(captor.capture());

        assertThat(captor.getValue()).isEqualTo(BPN);
    }

    @Test
    void verifyDuplicateDid() throws IOException {
        String bpn = "BPNL" + RandomStringUtils.secure().nextAlphabetic(12).toUpperCase();
        String did = "did:web:localhost:" + bpn;
        var serialized = objectMapper.writeValueAsString(new DidEntry(bpn, did));

        when(store.existsByDid(did)).thenReturn(true);

        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .post("")
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    void verifyDuplicateDidUpdate() throws IOException {
        String bpn = "BPNL" + RandomStringUtils.secure().nextAlphabetic(12).toUpperCase();
        String did = "did:web:localhost:" + bpn;
        String otherBpn = "BPNL" + RandomStringUtils.secure().nextAlphabetic(12).toUpperCase();

        when(store.exists(bpn)).thenReturn(true);
        when(store.getByBpn(bpn)).thenReturn(Optional.of(new DidEntry(bpn, "old-did")));
        when(store.getByDid(did)).thenReturn(Optional.of(new DidEntry(otherBpn, did)));

        var serialized = objectMapper.writeValueAsString(new DidEntry(bpn, did));
        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .put("")
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Override
    protected Object controller() {
        store = mock(DidEntryStore.class);
        monitor = mock(Monitor.class);

        return new ManagementApiController(store, monitor);
    }

    private RequestSpecification baseRequest() {
        return given().baseUri("http://localhost:" + port).basePath("/bpn-directory").when();
    }
}
