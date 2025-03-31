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

package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.edc.iam.identitytrust.service.verification.MultiFormatPresentationVerifier;
import org.eclipse.edc.iam.identitytrust.spi.SecureTokenService;
import org.eclipse.edc.iam.verifiablecredentials.VerifiableCredentialValidationServiceImpl;
import org.eclipse.edc.iam.verifiablecredentials.revocation.bitstring.BitstringStatusListRevocationService;
import org.eclipse.edc.iam.verifiablecredentials.revocation.statuslist2021.StatusList2021RevocationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.RevocationServiceRegistry;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.bitstringstatuslist.BitstringStatusListStatus;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.revocation.statuslist2021.StatusList2021Status;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.token.spi.TokenValidationRulesRegistry;
import org.eclipse.edc.token.spi.TokenValidationService;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.verifiablecredentials.jwt.JwtPresentationVerifier;
import org.eclipse.edc.web.spi.WebService;

import java.time.Clock;

import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;

/**
 * Registers an authentication service that checks MembershipCredentials.
 */
@Extension(CredentialBasedAuthenticationExtension.EXTENSION_NAME)
public class CredentialBasedAuthenticationExtension implements ServiceExtension {

    public static final String EXTENSION_NAME = "Directory API Authentication Extension";

    private static final long DEFAULT_REVOCATION_CACHE_VALIDITY_MILLIS = 15 * 60 * 1000L;
    private static final String DIRECTORY_CONTEXT = "directory";

    @Setting(value = "Validity period of cached StatusList2021 credential entries in milliseconds.", defaultValue = DEFAULT_REVOCATION_CACHE_VALIDITY_MILLIS + "", type = "long")
    public static final String REVOCATION_CACHE_VALIDITY = "edc.iam.credential.revocation.cache.validity";

    @Inject
    private WebService webService;
    @Inject
    private TypeManager typeManager;
    @Inject
    private TokenValidationService tokenValidationService;
    @Inject
    private TokenValidationRulesRegistry rulesRegistry;
    @Inject
    private DidPublicKeyResolver didPublicKeyResolver;
    @Inject
    private Clock clock;
    @Inject
    private ApiAuthenticationRegistry registry;
    @Inject
    private RevocationServiceRegistry revocationServiceRegistry;
    @Inject
    private TrustedIssuerRegistry trustedIssuerRegistry;
    @Inject
    private TypeTransformerRegistry typeTransformerRegistry;

    @Override
    public String name() {
        return EXTENSION_NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        // the DidPublicKeyResolver has a dependency onto the KeyParserRegistry, so that must be created in a separate ext -> KeyParserRegistryExtension
        var jwtVerifier = new JwtPresentationVerifier(typeManager, JSON_LD, tokenValidationService, rulesRegistry, didPublicKeyResolver);
        var presentationVerifier = new MultiFormatPresentationVerifier(null, jwtVerifier);

        var validity = context.getConfig().getLong(REVOCATION_CACHE_VALIDITY, DEFAULT_REVOCATION_CACHE_VALIDITY_MILLIS);
        revocationServiceRegistry.addService(StatusList2021Status.TYPE, new StatusList2021RevocationService(typeManager.getMapper(), validity));
        revocationServiceRegistry.addService(BitstringStatusListStatus.TYPE, new BitstringStatusListRevocationService(typeManager.getMapper(), validity));
        var validationService = new VerifiableCredentialValidationServiceImpl(presentationVerifier, trustedIssuerRegistry, revocationServiceRegistry, clock, typeManager.getMapper());

        var authService = new CredentialBasedAuthenticationService(context.getMonitor(), typeManager.getMapper(), validationService, typeTransformerRegistry);
        registry.register(DIRECTORY_CONTEXT, authService);
        webService.registerResource(DIRECTORY_CONTEXT, new AuthenticationRequestFilter(registry, DIRECTORY_CONTEXT));
    }

    @Provider
    public SecureTokenService secureTokenService() {
        return (map, s) -> null; // not really needed but requested by the runtime because of some tangles into trusted-issuer-core
    }

}
