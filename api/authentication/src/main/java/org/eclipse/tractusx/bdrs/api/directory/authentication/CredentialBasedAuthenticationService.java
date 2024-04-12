package org.eclipse.tractusx.bdrs.api.directory.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.VerifiableCredentialValidationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.CredentialFormat;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiablePresentation;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiablePresentationContainer;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            throw new AuthenticationFailedException("Headers were null or empty");
        }
        Objects.requireNonNull(headers, "headers");

        var authHeaders = headers.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(AUTHORIZATION))
                .map(headers::get)
                .findFirst();

        return authHeaders.map(this::performCredentialValidation).orElseThrow(() -> new AuthenticationFailedException("Header '%s' not present".formatted(AUTHORIZATION)));
    }

    private boolean performCredentialValidation(List<String> authHeaders) {
        if (authHeaders.size() != 1) {
            return false;
        }
        var token = authHeaders.get(0);
        if (!token.toLowerCase().startsWith("bearer ")) {
            return false;
        }
        token = token.substring(6).trim(); // "bearer" has 7 characters, it could be upper case, lower case or capitalized

        if (!isValidJwt(token)) {
            return false;
        }

        var finalToken = token;
        return typeTransformerRegistry.transform(token, VerifiablePresentation.class)
                .compose(pres -> verifiableCredentialValidationService.validate(List.of(new VerifiablePresentationContainer(finalToken, CredentialFormat.JWT, pres)), new MustHaveMemberhipCredentialRule()))
                .onFailure(f -> monitor.warning("Error validating BDRS client VP: %s".formatted(f.getFailureDetail())))
                .succeeded();
    }

    /**
     * checks if a string is a valid JWT by splitting it on ".", and checking that the first two parts are valid JSON
     */
    private boolean isValidJwt(String token) {
        var parts = token.split("\\.");
        if (parts.length != 3) { // The JWT is composed of three parts
            return false;
        }
        var decoder = Base64.getUrlDecoder();
        return canParse(decoder.decode(parts[0])) && canParse(decoder.decode(parts[1]));
    }

    /**
     * checks if a string is valid JSON
     */
    private boolean canParse(byte[] rawInput) {
        try (var parser = objectMapper.createParser(rawInput)) {
            parser.nextToken();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
