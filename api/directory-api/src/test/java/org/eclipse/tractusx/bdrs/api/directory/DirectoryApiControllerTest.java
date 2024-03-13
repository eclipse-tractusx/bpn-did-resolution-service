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

package org.eclipse.tractusx.bdrs.api.directory;

import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.web.jersey.testfixtures.RestControllerTestBase;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DirectoryApiControllerTest extends RestControllerTestBase {
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

    @Override
    protected Object controller() {
        store = mock(DidEntryStore.class);
        return new DirectoryApiController(store);
    }

    private RequestSpecification baseRequest() {
        return given().baseUri("http://localhost:" + port).basePath("/bpn-directory").when();
    }

}

