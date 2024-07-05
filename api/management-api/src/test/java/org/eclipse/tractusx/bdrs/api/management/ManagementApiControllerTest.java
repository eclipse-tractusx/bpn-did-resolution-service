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
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
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
        var serialized = objectMapper.writeValueAsString(new DidEntry(BPN, DID));

        var captor = ArgumentCaptor.forClass(DidEntry.class);

        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .post("")
                .then()
                .statusCode(204);

        verify(store).save(captor.capture());

        assertThat(captor.getValue().bpn()).isEqualTo(BPN);
        assertThat(captor.getValue().did()).isEqualTo(DID);
    }

    @Test
    void verifyUpdate() throws IOException {
        var serialized = objectMapper.writeValueAsString(new DidEntry(BPN, DID));

        var captor = ArgumentCaptor.forClass(DidEntry.class);

        baseRequest()
                .contentType(JSON)
                .body(serialized)
                .put("")
                .then()
                .statusCode(204);

        verify(store).update(captor.capture());

        assertThat(captor.getValue().bpn()).isEqualTo(BPN);
        assertThat(captor.getValue().did()).isEqualTo(DID);
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

    @Override
    protected Object controller() {
        store = mock(DidEntryStore.class);
        return new ManagementApiController(store);
    }

    private RequestSpecification baseRequest() {
        return given().baseUri("http://localhost:" + port).basePath("/bpn-directory").when();
    }
}
