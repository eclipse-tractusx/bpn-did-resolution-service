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

package org.eclipse.tractusx.bdrs.test.directory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.DecoderConfig.ContentDecoder.DEFLATE;
import static io.restassured.config.DecoderConfig.decoderConfig;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFreePort;

/**
 * Performs end-to-end testing of the BPN Directory.
 */
public class E2EDirectoryTest {
    private static final URI apiEndpoint = URI.create("http://localhost:" + getFreePort() + "/api");
    private static final URI managementEndpoint = URI.create("http://localhost:" + getFreePort() + "/management/v1");
    private static final String BPN_DIRECTORY = "bpn-directory";

    private static final String AUTH_KEY = "1234";

    private static final String BPN1 = "BPN12345";
    private static final String DID1 = "did:web:localhost/foo";

    private static final String BPN2 = "BPN67890";
    private static final String DID2 = "did:web:localhost/bar";
    private static final String UPDATED_DID2 = "did:web:localhost/baz";

    @RegisterExtension
    protected static EdcRuntimeExtension runtime =
            new EdcRuntimeExtension(
                    ":system-tests:test-server",
                    "bdrs",
                    Map.of("web.http.port", String.valueOf(apiEndpoint.getPort()),
                            "web.http.management.port", String.valueOf(managementEndpoint.getPort()),
                            "web.http.management.path", String.valueOf(managementEndpoint.getPath()),
                            "edc.api.auth.key", AUTH_KEY)
            );

    private ObjectMapper mapper;

    @Test
    void verifyPublicBpnDirectoryRequest() throws IOException {
        seedServer(BPN1, DID1);
        var result = getBpnDirectory(apiRequest());
        assertThat(result.get(BPN1)).isEqualTo(DID1);
    }

    @Test
    void verifyManagementApi() throws IOException {
        seedServer(BPN1, DID1);
        seedServer(BPN2, DID2);

        // verify BPNs are returned
        var result = getBpnDirectory(managementRequest());

        assertThat(result.get(BPN1)).isEqualTo(DID1);
        assertThat(result.get(BPN2)).isEqualTo(DID2);

        // verify delete
        managementRequest()
                .when()
                .delete(BPN_DIRECTORY + "/" + BPN1)
                .then()
                .statusCode(204);

        result = getBpnDirectory(managementRequest());

        assertThat(result.get(BPN1)).isNull();
        assertThat(result.get(BPN2)).isEqualTo(DID2);

        // verify update
        var content = Map.of("bpn", BPN2, "did", UPDATED_DID2);
        managementRequest()
                .body(content)
                .when()
                .put(BPN_DIRECTORY)
                .then()
                .statusCode(204);

        result = getBpnDirectory(managementRequest());

        assertThat(result.get(BPN2)).isEqualTo(UPDATED_DID2);
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    private void seedServer(String bpn, String did) {
        var content = Map.of("bpn", bpn, "did", did);
        managementRequest()
                .body(content)
                .when()
                .post(BPN_DIRECTORY)
                .then()
                .statusCode(204);
    }

    private Map<String, String> getBpnDirectory(RequestSpecification spec) throws IOException {
        return deserialize(spec
                .config(config().decoderConfig(decoderConfig().contentDecoders(DEFLATE)))
                .when()
                .get(BPN_DIRECTORY)
                .then()
                .statusCode(200).
                extract()
                .response()
                .asByteArray());
    }


    private RequestSpecification apiRequest() {
        return given().baseUri(apiEndpoint.toString())
                .headers(Map.of());
    }

    private RequestSpecification managementRequest() {
        return given().baseUri(managementEndpoint.toString())
                .headers(Map.of("x-api-key", AUTH_KEY))
                .contentType(JSON);
    }

    private Map<String, String> deserialize(byte[] response) throws IOException {
        var stream = new GZIPInputStream(new ByteArrayInputStream(response));
        var decompressed = stream.readAllBytes();
        //noinspection unchecked
        return mapper.readValue(decompressed, Map.class);
    }

}
