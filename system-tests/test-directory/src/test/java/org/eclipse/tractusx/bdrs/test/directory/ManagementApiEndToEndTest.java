/*
 * Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
 * Copyright (c) 2025 Cofinity-X GmbH
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

package org.eclipse.tractusx.bdrs.test.directory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
import static org.eclipse.edc.util.io.Ports.getFreePort;

/**
 * Performs end-to-end testing of the BPN Directory.
 */
@EndToEndTest
public class ManagementApiEndToEndTest {
    private static final URI API_ENDPOINT = URI.create("http://localhost:" + getFreePort() + "/api");
    private static final URI MANAGEMENT_ENDPOINT = URI.create("http://localhost:" + getFreePort() + "/management/v1");
    private static final URI DIRECTORY_ENDPOINT = URI.create("http://localhost:" + getFreePort() + "/directory/v1");
    private static final String BPN_DIRECTORY = "bpn-directory";

    private static final String AUTH_KEY = "1234";

    private static final String BPN1 = "BPN12345";
    private static final String DID1 = "did:web:localhost/foo";

    private static final String BPN2 = "BPN67890";
    private static final String DID2 = "did:web:localhost/bar";
    private static final String UPDATED_DID2 = "did:web:localhost/baz";

    @RegisterExtension
    protected static RuntimeExtension runtime = new RuntimePerClassExtension(
            new EmbeddedRuntime("bdrs", ":runtimes:bdrs-server-memory")
                    .configurationProvider(() -> ConfigFactory.fromMap(Map.of(
                            "web.http.port", String.valueOf(API_ENDPOINT.getPort()),
                            "web.http.management.port", String.valueOf(MANAGEMENT_ENDPOINT.getPort()),
                            "web.http.management.path", String.valueOf(MANAGEMENT_ENDPOINT.getPath()),
                            "web.http.directory.port", String.valueOf(DIRECTORY_ENDPOINT.getPort()),
                            "web.http.directory.path", String.valueOf(DIRECTORY_ENDPOINT.getPath()),
                            "edc.iam.issuer.id", "any",
                            "web.http.management.auth.alias", AUTH_KEY,
                            "web.http.management.auth.type", "tokenbased",
                            "web.http.management.auth.key", AUTH_KEY))
                    )
    );

    private ObjectMapper mapper;

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

    @ParameterizedTest
    @ValueSource(strings = "invalid-key")
    @EmptySource
    void verifyManagementApi_invalidAuthHeader(String invalidAuthHeader) {
        seedServer(BPN1, DID1);
        seedServer(BPN2, DID2);

        given().baseUri(MANAGEMENT_ENDPOINT.toString())
                .headers("x-api-key", invalidAuthHeader)
                .contentType(JSON)
                .then()
                .statusCode(401);
    }

    @Test
    void verifyManagementApi_missingAuthHeader() {
        given().baseUri(MANAGEMENT_ENDPOINT.toString())
                .contentType(JSON)
                .then()
                .statusCode(401);
    }

    @Test
    void verifyDuplicateDid() {
        seedServer(BPN1, DID1);

        // try to add duplicate DID
        var content = Map.of("bpn", BPN2, "did", DID1);
        managementRequest()
                .body(content)
                .when()
                .post(BPN_DIRECTORY)
                .then()
                .statusCode(409);

        // try to update with duplicate DID
        seedServer(BPN2, DID2);
        content = Map.of("bpn", BPN2, "did", DID1);
        managementRequest()
                .body(content)
                .when()
                .put(BPN_DIRECTORY)
                .then()
                .statusCode(409);
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
                .statusCode(Matchers.isOneOf(
                        Response.Status.CREATED.getStatusCode(),
                        Response.Status.NO_CONTENT.getStatusCode(),
                        Response.Status.CONFLICT.getStatusCode()
                ));
    }

    private Map<String, String> getBpnDirectory(RequestSpecification spec) throws IOException {
        return deserialize(spec
                .config(config().decoderConfig(decoderConfig().contentDecoders(DEFLATE)))
                .when()
                .get(BPN_DIRECTORY)
                .then()
                .statusCode(200)
                .extract()
                .response()
                .asByteArray());
    }


    private RequestSpecification managementRequest() {
        return given().baseUri(MANAGEMENT_ENDPOINT.toString())
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
