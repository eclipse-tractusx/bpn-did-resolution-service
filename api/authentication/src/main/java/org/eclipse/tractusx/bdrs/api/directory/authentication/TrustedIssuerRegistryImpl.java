package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TrustedIssuerRegistryImpl implements TrustedIssuerRegistry {
    private final Map<String, Issuer> store = new HashMap<>();

    @Override
    public void addIssuer(Issuer issuer) {
        store.put(issuer.id(), issuer);
    }

    @Override
    public Issuer getById(String id) {
        return store.get(id);
    }

    @Override
    public Collection<Issuer> getTrustedIssuers() {
        return store.values();
    }
}
