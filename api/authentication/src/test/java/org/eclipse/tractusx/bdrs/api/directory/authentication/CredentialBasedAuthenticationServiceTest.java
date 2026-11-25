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

package org.eclipse.tractusx.bdrs.api.directory.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.edc.iam.verifiablecredentials.spi.VerifiableCredentialValidationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.CredentialSubject;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiablePresentation;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.CredentialValidationRule;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CredentialBasedAuthenticationServiceTest {


    private final Monitor monitor = mock();
    private final VerifiableCredentialValidationService validationService = mock();
    private final TypeTransformerRegistry typeTransformerRegistry = mock();
    private final CredentialBasedAuthenticationService service = new CredentialBasedAuthenticationService(monitor, new ObjectMapper(), validationService, typeTransformerRegistry);

    @Test
    void isAuthenticated_noHeader() {
        assertThatThrownBy(() -> service.isAuthenticated(null)).isInstanceOf(AuthenticationFailedException.class);
        assertThatThrownBy(() -> service.isAuthenticated(Map.of())).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void isAuthenticated_noAuthHeader() {
        assertThatThrownBy(() -> service.isAuthenticated(Map.of("foo", List.of("bar")))).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void isAuthenticated_multipleAuthHeaders() {
        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("foo", "bar", "baz")))).isFalse();
    }

    @Test
    void isAuthenticated_notBearer() {
        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("value")))).isFalse();
    }

    @Test
    void isAuthenticated_tokenNotJwtFormat() {
        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer value")))).isFalse();
    }

    @Test
    void isAuthenticated_vpInvalid() {
        when(validationService.validate(anyList(), any(), any(CredentialValidationRule[].class)))
                .thenReturn(Result.failure("test failure"));
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(VerifiablePresentation.Builder.newInstance().type("VerifiablePresentation").build()));

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwt())))).isFalse();
        verify(monitor).warning(startsWith("Error validating BDRS client VP"));
    }

    @Test
    void isAuthenticated_vpWithEmptyCredentials() {
        var vp = VerifiablePresentation.Builder.newInstance()
                .type("VerifiablePresentation")
                .credentials(List.of())
                .build();
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(vp));

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwtWithAudience())))).isFalse();
        verify(monitor).warning("Verifiable Presentation contains no credentials");
        verify(validationService, never()).validate(anyList(), any(), any(CredentialValidationRule[].class));
    }

    @Test
    void isAuthenticated_vpWithNullCredentials() {
        var vp = VerifiablePresentation.Builder.newInstance()
                .type("VerifiablePresentation")
                .credentials(null)
                .build();
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(vp));

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwtWithAudience())))).isFalse();
        verify(monitor).warning("Verifiable Presentation contains no credentials");
        verify(validationService, never()).validate(anyList(), any(), any(CredentialValidationRule[].class));
    }

    @Test
    void isAuthenticated_unexpectedExceptionDuringValidation() {
        var exception = new RuntimeException("Unexpected error");
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenThrow(exception);

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwtWithAudience())))).isFalse();
        verify(monitor).severe("Unexpected error during credential validation: Unexpected error", exception);
    }

    @Test
    void isAuthenticated_validJwtWithSuccessfulValidation() {
        var credential = VerifiableCredential.Builder.newInstance()
                .id("credential-id")
                .type("CredentialType")
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .claim("membership", "active")
                        .build())
                .issuer(new Issuer("did:example:issuer"))
                .issuanceDate(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        var vp = VerifiablePresentation.Builder.newInstance()
                .type("VerifiablePresentation")
                .credentials(List.of(credential))
                .build();
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(vp));
        when(validationService.validate(anyList(), any(), any(CredentialValidationRule[].class)))
                .thenReturn(Result.success());

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwtWithAudience())))).isTrue();
    }

    @Test
    void isAuthenticated_authHeaderWithNullValues() {
        var headers = new HashMap<String, List<String>>();
        headers.put("Authorization", null);

        assertThat(service.isAuthenticated(headers)).isFalse();
        verify(monitor).warning("Authorization headers are null or empty");
    }

    @Test
    void isAuthenticated_authHeaderWithEmptyValues() {
        assertThat(service.isAuthenticated(Map.of("Authorization", List.of()))).isFalse();
        verify(monitor).warning("Authorization headers are null or empty");
    }

    @Test
    void isAuthenticated_authHeaderValueIsNull() {
        assertThat(service.isAuthenticated(Map.of("Authorization", Arrays.asList((String) null)))).isFalse();
        verify(monitor).warning(startsWith("Error validating BDRS client VP"));
        verify(validationService, never()).validate(anyList(), any(), any(CredentialValidationRule[].class));
    }

    @Test
    void isAuthenticated_lowerCaseBearerPrefix() throws JOSEException {
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(presentationWithCredential()));
        when(validationService.validate(anyList(), eq("test-audience"), any(CredentialValidationRule[].class)))
                .thenReturn(Result.success());

        assertThat(service.isAuthenticated(Map.of("authorization", List.of("bearer " + signedJwt(List.of("test-audience")))))).isTrue();
    }

    @Test
    void isAuthenticated_tokenWithMultipleAudiences() throws JOSEException {
        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + signedJwt(List.of("aud-1", "aud-2")))))).isFalse();
        verify(monitor).warning(startsWith("Error validating BDRS client VP"));
        verify(validationService, never()).validate(anyList(), any(), any(CredentialValidationRule[].class));
    }

    @Test
    void isAuthenticated_tokenNotParseableAsSignedJwt() {
        var encoder = Base64.getUrlEncoder().withoutPadding();
        // header and payload are valid JSON, but the header misses the mandatory "alg" parameter
        var token = encoder.encodeToString("{\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8)) + "." +
                encoder.encodeToString("{\"aud\":\"test-audience\"}".getBytes(StandardCharsets.UTF_8)) + ".c2lnbmF0dXJl";

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + token)))).isFalse();
        verify(monitor).warning(startsWith("Error validating BDRS client VP"));
        verify(validationService, never()).validate(anyList(), any(), any(CredentialValidationRule[].class));
    }

    @Test
    void isAuthenticated_transformationFails() {
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.failure("transformation failed"));

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwtWithAudience())))).isFalse();
        verify(monitor).warning("Error validating BDRS client VP: transformation failed");
        verify(validationService, never()).validate(anyList(), any(), any(CredentialValidationRule[].class));
    }

    @Test
    void isAuthenticated_validationFailsForPresentationWithCredentials() {
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(presentationWithCredential()));
        when(validationService.validate(anyList(), eq("test-audience"), any(CredentialValidationRule[].class)))
                .thenReturn(Result.failure("no membership credential"));

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwtWithAudience())))).isFalse();
        verify(monitor).warning("Error validating BDRS client VP: no membership credential");
    }

    private VerifiablePresentation presentationWithCredential() {
        var credential = VerifiableCredential.Builder.newInstance()
                .id("credential-id")
                .type("MembershipCredential")
                .credentialSubject(CredentialSubject.Builder.newInstance()
                        .claim("holderIdentifier", "BPNL000000000001")
                        .build())
                .issuer(new Issuer("did:example:issuer"))
                .issuanceDate(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        return VerifiablePresentation.Builder.newInstance()
                .type("VerifiablePresentation")
                .credentials(List.of(credential))
                .build();
    }

    private String signedJwt(List<String> audience) throws JOSEException {
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                new JWTClaimsSet.Builder().subject("test-subject").audience(audience).build());
        jwt.sign(new ECDSASigner(new ECKeyGenerator(Curve.P_256).generate()));
        return jwt.serialize();
    }

    private String createSerializedJwtWithAudience() {
        try {
            var key = new ECKeyGenerator(Curve.P_256).generate();
            var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                    new JWTClaimsSet.Builder().subject("test-subject").audience("test-audience").build());
            jwt.sign(new ECDSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String createSerializedJwt() {
        return "eyJhbGciOiJIUzI1NiIsI" +
                "nR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwi" +
                "aWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }
}