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

package org.eclipse.tractusx.bdrs.api.directory.authentication;

import dev.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.registry.ApiAuthenticationRegistry;
import org.eclipse.edc.http.client.EdcHttpClientImpl;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.iam.did.spi.resolution.DidPublicKeyResolver;
import org.eclipse.edc.iam.identitytrust.service.verification.MultiFormatPresentationVerifier;
import org.eclipse.edc.iam.identitytrust.transform.to.JwtToVerifiableCredentialTransformer;
import org.eclipse.edc.iam.identitytrust.transform.to.JwtToVerifiablePresentationTransformer;
import org.eclipse.edc.iam.verifiablecredentials.StatusList2021RevocationService;
import org.eclipse.edc.iam.verifiablecredentials.VerifiableCredentialValidationServiceImpl;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry;
import org.eclipse.edc.jsonld.JsonLdConfiguration;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.token.spi.TokenValidationRulesRegistry;
import org.eclipse.edc.token.spi.TokenValidationService;
import org.eclipse.edc.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.verifiablecredentials.jwt.JwtPresentationVerifier;
import org.eclipse.edc.web.spi.WebService;

import java.time.Clock;

import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;
import static org.eclipse.tractusx.bdrs.api.directory.authentication.CredentialBasedAuthenticationExtension.NAME;

/**
 * Registers an authentication service that checks MembershipCredentials.
 */
@Extension(NAME)
public class CredentialBasedAuthenticationExtension implements ServiceExtension {
    public static final long DEFAULT_REVOCATION_CACHE_VALIDITY_MILLIS = 15 * 60 * 1000L;
    @Setting(value = "Validity period of cached StatusList2021 credential entries in milliseconds.", defaultValue = DEFAULT_REVOCATION_CACHE_VALIDITY_MILLIS + "", type = "long")
    public static final String REVOCATION_CACHE_VALIDITY = "edc.iam.credential.revocation.cache.validity";
    public static final String NAME = "Directory API Authentication Extension";
    public static final String MONITOR_PREFIX = "Presentation Transformation";
    private static final String DIRECTORY_CONTEXT = "directory";
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

    private TrustedIssuerRegistry trustedIssuerRegistry;
    private TypeTransformerRegistryImpl typeTransformerRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var mapper = typeManager.getMapper(JSON_LD);
        // the DidPublicKeyResolver has a dependency onto the KeyParserRegistry, so that must be created in a separate ext -> KeyParserRegistryExtension
        var jwtVerifier = new JwtPresentationVerifier(mapper, tokenValidationService, rulesRegistry, didPublicKeyResolver);
        var presentationVerifier = new MultiFormatPresentationVerifier(null, jwtVerifier);

        var validity = context.getConfig().getLong(REVOCATION_CACHE_VALIDITY, DEFAULT_REVOCATION_CACHE_VALIDITY_MILLIS);
        var statuslistService = new StatusList2021RevocationService(typeManager.getMapper(), validity);
        var validationService = new VerifiableCredentialValidationServiceImpl(presentationVerifier, createTrustedIssuerRegistry(), statuslistService, clock);

        var authService = new CredentialBasedAuthenticationService(context.getMonitor(), typeManager.getMapper(), validationService, typeTransformerRegistry(context));
        registry.register(DIRECTORY_CONTEXT, authService);
        webService.registerResource(DIRECTORY_CONTEXT, new AuthenticationRequestFilter(registry, DIRECTORY_CONTEXT));
    }

    // must provide this, so the TrustedIssuerRegistryConfigurationExtension can inject it
    @Provider
    public TrustedIssuerRegistry createTrustedIssuerRegistry() {
        if (trustedIssuerRegistry == null) {
            trustedIssuerRegistry = new TrustedIssuerRegistryImpl();
        }
        return trustedIssuerRegistry;
    }

    @Provider
    public TypeTransformerRegistry typeTransformerRegistry(ServiceExtensionContext context) {
        if (typeTransformerRegistry == null) {
            typeTransformerRegistry = new TypeTransformerRegistryImpl();
            var monitor = context.getMonitor().withPrefix(MONITOR_PREFIX);
            typeTransformerRegistry.register(new JwtToVerifiablePresentationTransformer(monitor, typeManager.getMapper(JSON_LD), new TitaniumJsonLd(monitor, JsonLdConfiguration.Builder.newInstance().build())));
            typeTransformerRegistry.register(new JwtToVerifiableCredentialTransformer(monitor));
        }
        return typeTransformerRegistry;
    }

    @Provider
    public EdcHttpClient httpClient(ServiceExtensionContext context) {
        return new EdcHttpClientImpl(new OkHttpClient(), RetryPolicy.ofDefaults(), context.getMonitor().withPrefix(MONITOR_PREFIX));
    }
}