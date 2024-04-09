package org.eclipse.tractusx.bdrs.api.directory.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.iam.verifiablecredentials.spi.VerifiableCredentialValidationService;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiablePresentation;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.CredentialValidationRule;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.AuthenticationFailedException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
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
        when(validationService.validate(anyList(), any(CredentialValidationRule[].class)))
                .thenReturn(Result.failure("test failure"));
        when(typeTransformerRegistry.transform(any(), eq(VerifiablePresentation.class)))
                .thenReturn(Result.success(VerifiablePresentation.Builder.newInstance().type("VerifiablePresentation").build()));

        assertThat(service.isAuthenticated(Map.of("Authorization", List.of("Bearer " + createSerializedJwt())))).isFalse();
        verify(monitor).warning(startsWith("Error validating BDRS client VP"));
    }

    private String createSerializedJwt() {
        return "eyJhbGciOiJIUzI1NiIsI" +
                "nR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwi" +
                "aWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }
}