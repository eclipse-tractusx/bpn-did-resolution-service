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
import com.nimbusds.jwt.SignedJWT;
import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.VerifiableCredentialValidationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.CredentialFormat;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiablePresentation;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiablePresentationContainer;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

/**
 * AuthenticationService that takes a "Bearer" token from the "Authorization" header, and
 * checks whether that token is a valid VerifiablePresentation, and contains a valid MembershipCredential.
 */
public class CredentialBasedAuthenticationService implements AuthenticationService {
    private final Monitor monitor;
    private final ObjectMapper objectMapper;
    private final VerifiableCredentialValidationService verifiableCredentialValidationService;
    private final TypeTransformerRegistry typeTransformerRegistry;

    public CredentialBasedAuthenticationService(Monitor monitor, ObjectMapper objectMapper, VerifiableCredentialValidationService verifiableCredentialValidationService, TypeTransformerRegistry typeTransformerRegistry) {
        this.monitor = monitor;
        this.objectMapper = objectMapper;
        this.verifiableCredentialValidationService = verifiableCredentialValidationService;
        this.typeTransformerRegistry = typeTransformerRegistry;
    }

    @Override
    public boolean isAuthenticated(Map<String, List<String>> headers) {

        if (headers == null || headers.isEmpty()) {
            var msg = "Headers were null or empty";
            monitor.warning(msg);
            throw new AuthenticationFailedException(msg);
        }

        var authHeaders = headers.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(AUTHORIZATION))
                .map(headers::get)
                .findFirst();

        return authHeaders.map(this::performCredentialValidation).orElseThrow(() -> {
            var msg = "Header '%s' not present";
            monitor.warning(msg);
            return new AuthenticationFailedException(msg.formatted(AUTHORIZATION));
        });
    }

    private boolean performCredentialValidation(List<String> authHeaders) {
        if (authHeaders.size() != 1) {
            monitor.warning("Expected exactly 1 Authorization header, found %d".formatted(authHeaders.size()));
            return false;
        }
        try {
            String token = authHeaders.stream()
                    .filter(t -> t.toLowerCase().startsWith("bearer "))
                    .map(t -> t.substring(6).trim())
                    .findFirst()
                    .orElseThrow(() -> new EdcException("Authorization header must start with 'bearer '"));

            String audience = getAudienceFromToken(token);

            return typeTransformerRegistry.transform(token, VerifiablePresentation.class)
                    .compose(pres -> verifiableCredentialValidationService.validate(
                            List.of(new VerifiablePresentationContainer(token, CredentialFormat.VC1_0_JWT, pres)),
                            audience,
                            new MustHaveMemberhipCredentialRule()))
                    .onFailure(f -> monitor.warning("Error validating BDRS client VP: %s".formatted(f.getFailureDetail())))
                    .succeeded();
        } catch (EdcException e) {
            monitor.warning("Error validating the BDRS client VP: %s".formatted(e.getLocalizedMessage()));
            return false;
        }
    }

    /**
     * Parses the received token and extracts all relevant information out of the token.
     *
     * This is for now the audience, as the current BDRS mechanism does not really use the audience properly.
     * Instead, a connector sets the audience to the its own did, which cannot be determined at this place, so the
     * token validation does a dummy check by comparing the audience in the token with itself in the called
     * verifiableCredentialValidation service. The audience of the token should actually be the BDRS DID and it
     * should be checked whether more validations should occur on the token.
     *
     * @param token The token as received in the Authorization header
     * @return The content of a valid AUDIENCE claim.
     * @throws EdcException If the parsing of the claim fails
     */
    private String getAudienceFromToken(String token) {
        try {
            var audience = SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getAudience();
            if (audience.size() != 1) {
                throw new EdcException("Token misses a single string audience claim");
            }

            return audience.get(0);
        } catch (ParseException e) {
            throw new EdcException(e);
        }
    }
}
