package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.CredentialValidationRule;
import org.eclipse.edc.spi.result.Result;

public class MustHaveMemberhipCredentialRule implements CredentialValidationRule {
    private static final String MEMBERSHIP_TYPE = "MembershipCredential";

    @Override
    public Result<Void> apply(VerifiableCredential verifiableCredential) {
        return verifiableCredential.getType().contains(MEMBERSHIP_TYPE) ? Result.success() : Result.failure("Expected only credentials containing type '%s', but got '%s'.".formatted(MEMBERSHIP_TYPE, verifiableCredential.getType()));
    }
}
