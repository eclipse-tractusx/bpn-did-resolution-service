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

package org.eclipse.tractusx.bdrs.test.directory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.document.VerificationMethod;
import org.eclipse.edc.iam.did.spi.resolution.DidResolver;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerMethodExtension;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
import org.eclipse.edc.verifiablecredentials.jwt.JwtCreationUtils;
import org.eclipse.edc.web.spi.ApiErrorDetail;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.DecoderConfig.ContentDecoder.DEFLATE;
import static io.restassured.config.DecoderConfig.decoderConfig;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.spi.result.Result.success;
import static org.eclipse.edc.util.io.Ports.getFreePort;
import static org.eclipse.tractusx.bdrs.test.directory.TestData.VP_CONTENT_EXAMPLE;

/**
 * Performs end-to-end testing of the BPN Directory.
 */
@EndToEndTest
public class DirectoryEndToEndTest {
    private static final URI API_ENDPOINT = URI.create("http://localhost:" + getFreePort() + "/api");
    private static final URI MANAGEMENT_ENDPOINT = URI.create("http://localhost:" + getFreePort() + "/management/v1");
    private static final URI DIRECTORY_ENDPOINT = URI.create("http://localhost:" + getFreePort() + "/directory/v1");
    private static final String BPN_DIRECTORY = "bpn-directory";

    private static final String AUTH_KEY = "1234";
    private static final String BPN1 = "BPN12345";
    private static final String DID1 = "did:web:localhost/foo";

    @RegisterExtension
    protected static RuntimeExtension runtime = new RuntimePerMethodExtension(
            new EmbeddedRuntime("BDRS Server", ":runtimes:bdrs-server-memory")
                    .configurationProvider(() -> ConfigFactory.fromMap(Map.of(
                            "web.http.port", String.valueOf(API_ENDPOINT.getPort()),
                            "web.http.management.port", String.valueOf(MANAGEMENT_ENDPOINT.getPort()),
                            "web.http.management.path", String.valueOf(MANAGEMENT_ENDPOINT.getPath()),
                            "web.http.directory.port", String.valueOf(DIRECTORY_ENDPOINT.getPort()),
                            "web.http.directory.path", String.valueOf(DIRECTORY_ENDPOINT.getPath()),
                            "edc.iam.issuer.id", "any",
                            "edc.iam.trusted-issuer.test.id", "did:web:some-issuer",
                            "edc.api.auth.key", AUTH_KEY))
                    )
    );

    private final String issuerId = "did:web:some-issuer";
    private final String holderId = "did:web:bdrs-client";
    private ECKey vcIssuerKey;
    private ECKey vpHolderKey;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() throws JOSEException {
        mapper = new ObjectMapper();
        vcIssuerKey = new ECKeyGenerator(Curve.P_256).keyID(issuerId + "#key-1").generate();
        vpHolderKey = new ECKeyGenerator(Curve.P_256).keyID(holderId + "#key-1").generate();
    }

    @DisplayName("Reject a request without Authorization header")
    @Test
    void getDirectory_missingAuthHeader() throws IOException {
        seedServer(BPN1, DID1);
        var errors = getBpnDirectory(apiRequest()).statusCode(401)
                .extract()
                .body()
                .as(ApiErrorDetail[].class);

        assertThat(errors.length).isEqualTo(1);
        assertThat(errors[0].getType()).isEqualTo("AuthenticationFailed");
        assertThat(errors[0].getMessage()).isEqualTo("Header 'Authorization' not present");
    }

    @DisplayName("Reject an Authorization header that does not start with \"Bearer\"")
    @Test
    void getDirectory_authHeaderNoBearer() throws IOException {
        seedServer(BPN1, DID1);
        var errors = getBpnDirectory(apiRequest().header("Authorization", "foobar")).statusCode(401)
                .extract()
                .body()
                .as(ApiErrorDetail[].class);

        assertThat(errors.length).isEqualTo(1);
        assertThat(errors[0].getType()).isEqualTo("AuthenticationFailed");
        assertThat(errors[0].getMessage()).isEqualTo("Request could not be authenticated");
    }

    @DisplayName("Reject an Authorization header that is not in JWT format")
    @Test
    void getDirectory_authHeaderNotJwt() throws IOException {
        seedServer(BPN1, DID1);
        var errors = getBpnDirectory(apiRequest().header("Authorization", "Bearer foobar")).statusCode(401)
                .extract()
                .body()
                .as(ApiErrorDetail[].class);

        assertThat(errors.length).isEqualTo(1);
        assertThat(errors[0].getType()).isEqualTo("AuthenticationFailed");
        assertThat(errors[0].getMessage()).isEqualTo("Request could not be authenticated");
    }

    @DisplayName("Accept a valid VP containing a valid MembershipCredential")
    @Test
    void getDirectory_validPresentation() throws IOException {
        registerDids();

        // create VC-JWT (signed by the central issuer)
        var vcJwt1 = JwtCreationUtils.createJwt(vcIssuerKey, issuerId, "degreeSub", holderId, Map.of("vc", asMap(TestData.MEMBERSHIP_CREDENTIAL.formatted(holderId))));

        // create VP-JWT (signed by the presenter) that contains the VP as a claim
        var vpJwt = JwtCreationUtils.createJwt(vpHolderKey, holderId, null, "bdrs-server-audience", Map.of("vp", asMap(VP_CONTENT_EXAMPLE.formatted(holderId, "\"" + vcJwt1 + "\""))));

        seedServer(BPN1, DID1);
        var bytes = getBpnDirectory(apiRequest().header("Authorization", "Bearer " + vpJwt))
                .statusCode(200)
                .extract().response().asByteArray();
        var result = deserialize(bytes);

        assertThat(result).isNotEmpty().containsEntry(BPN1, DID1);
    }

    @DisplayName("Reject VPs that contain a single VC, that is not a MembershipCredential")
    @Test
    void getDirectory_validPresentation_notMembershipCred() throws IOException {
        registerDids();

        // create VC-JWT (signed by the central issuer)
        var vcJwt1 = JwtCreationUtils.createJwt(vcIssuerKey, issuerId, "degreeSub", holderId, Map.of("vc", asMap(TestData.SOME_OTHER_CREDENTIAL.formatted(holderId))));

        // create VP-JWT (signed by the presenter) that contains the VP as a claim
        var vpJwt = JwtCreationUtils.createJwt(vpHolderKey, holderId, null, "bdrs-server-audience", Map.of("vp", asMap(VP_CONTENT_EXAMPLE.formatted(holderId, "\"" + vcJwt1 + "\""))));

        seedServer(BPN1, DID1);
        getBpnDirectory(apiRequest().header("Authorization", "Bearer " + vpJwt))
                .statusCode(401);
    }

    @DisplayName("Reject VP that contains no credentials")
    @Test
    void getDirectory_validPresentation_noCredential() throws IOException {
        registerDids();

        // create VP-JWT (signed by the presenter) that contains the VP as a claim
        var vpJwt = JwtCreationUtils.createJwt(vpHolderKey, holderId, null, "bdrs-server-audience", Map.of());

        seedServer(BPN1, DID1);
        getBpnDirectory(apiRequest().header("Authorization", "Bearer " + vpJwt))
                .statusCode(401);
    }

    @DisplayName("Reject VPs, that contain a MC and other credentials")
    @Test
    void getDirectory_validPresentation_multipleCredentials() throws IOException {
        registerDids();

        // create VC-JWT (signed by the central issuer)
        var vcJwt1 = JwtCreationUtils.createJwt(vcIssuerKey, issuerId, "degreeSub", holderId, Map.of("vc", asMap(TestData.SOME_OTHER_CREDENTIAL.formatted(holderId))));


        // create VC-JWT (signed by the central issuer)
        var vcJwt2 = JwtCreationUtils.createJwt(vcIssuerKey, issuerId, "degreeSub", holderId, Map.of("vc", asMap(TestData.MEMBERSHIP_CREDENTIAL.formatted(holderId))));

        // create VP-JWT (signed by the presenter) that contains the VP as a claim
        var credentialContent = "\"%s\", \"%s\"".formatted(vcJwt1, vcJwt2);
        var vpJwt = JwtCreationUtils.createJwt(vpHolderKey, holderId, null, "bdrs-server-audience", Map.of("vp", asMap(VP_CONTENT_EXAMPLE.formatted(holderId, credentialContent))));

        seedServer(BPN1, DID1);
        getBpnDirectory(apiRequest().header("Authorization", "Bearer " + vpJwt))
                .statusCode(401);
    }

    @DisplayName("Reject a spoofed VP")
    @Test
    void getDirectory_spoofedPresentation() throws Exception {
        registerDids();

        var spoofedKey = new ECKeyGenerator(Curve.P_256).keyID(holderId + "#key-1").generate();
        // create VC-JWT (signed by the central issuer)
        var vcJwt1 = JwtCreationUtils.createJwt(vcIssuerKey, issuerId, "degreeSub", holderId, Map.of("vc", asMap(TestData.MEMBERSHIP_CREDENTIAL.formatted(holderId))));

        // create VP-JWT (signed by the presenter) that contains the VP as a claim
        var vpJwt = JwtCreationUtils.createJwt(spoofedKey, holderId, null, "bdrs-server-audience", Map.of("vp", asMap(VP_CONTENT_EXAMPLE.formatted(holderId, "\"" + vcJwt1 + "\""))));

        seedServer(BPN1, DID1);
        getBpnDirectory(apiRequest().header("Authorization", "Bearer " + vpJwt))
                .statusCode(401);
    }

    @DisplayName("Reject a spoofed VC")
    @Test
    void getDirectory_spoofedCredential() throws Exception {
        registerDids();

        var spoofedKey = new ECKeyGenerator(Curve.P_256).keyID(holderId + "#key-1").generate();
        // create VC-JWT (signed by the central issuer)
        var vcJwt1 = JwtCreationUtils.createJwt(spoofedKey, issuerId, "degreeSub", holderId, Map.of("vc", asMap(TestData.MEMBERSHIP_CREDENTIAL.formatted(holderId))));

        // create VP-JWT (signed by the presenter) that contains the VP as a claim
        var vpJwt = JwtCreationUtils.createJwt(vpHolderKey, holderId, null, "bdrs-server-audience", Map.of("vp", asMap(VP_CONTENT_EXAMPLE.formatted(holderId, "\"" + vcJwt1 + "\""))));

        seedServer(BPN1, DID1);
        getBpnDirectory(apiRequest().header("Authorization", "Bearer " + vpJwt))
                .statusCode(401);
    }

    // registers a DidResolver for the "web" method. this method has to be called from a test method!
    private void registerDids() {
        runtime.getService(DidResolverRegistry.class)
                .register(new DidResolver() {
                    @Override
                    public @NotNull String getMethod() {
                        return "web";
                    }

                    @Override
                    public @NotNull Result<DidDocument> resolve(String s) {
                        return Stream.of(vcIssuerKey, vpHolderKey).filter(key -> key.getKeyID().startsWith(s))
                                .findFirst()
                                .map(key -> success(createDidDocument(key.toPublicJWK(), s)))
                                .orElseGet(() -> Result.failure("No such did"));
                    }
                });
    }

    private DidDocument createDidDocument(ECKey publickey, String did) {
        return DidDocument.Builder.newInstance()
                .verificationMethod(List.of(VerificationMethod.Builder.newInstance()
                        .id(publickey.getKeyID())
                        .publicKeyJwk(publickey.toJSONObject())
                        .type("JsonWebKey2020")
                        .build()))
                .id(did)
                .build();
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

    private ValidatableResponse getBpnDirectory(RequestSpecification spec) throws IOException {
        return spec.config(config().decoderConfig(decoderConfig().contentDecoders(DEFLATE)))
                .when()
                .get(BPN_DIRECTORY)
                .then();
    }

    private RequestSpecification apiRequest() {
        return given().baseUri(DIRECTORY_ENDPOINT.toString())
                .headers(Map.of());
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

    private Map<String, Object> asMap(String rawContent) {
        try {
            return mapper.readValue(rawContent, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
