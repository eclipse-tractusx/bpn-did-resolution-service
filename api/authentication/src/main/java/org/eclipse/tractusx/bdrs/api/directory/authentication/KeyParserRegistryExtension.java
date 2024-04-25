package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.keys.KeyParserRegistryImpl;
import org.eclipse.edc.keys.keyparsers.JwkParser;
import org.eclipse.edc.keys.keyparsers.PemParser;
import org.eclipse.edc.keys.spi.KeyParserRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

/**
 * This extension must be separate from the {@link CredentialBasedAuthenticationExtension} to avoid a cyclic dependency
 */
@Extension(value = "Provides a KeyParserRegistry")
public class KeyParserRegistryExtension implements ServiceExtension {
    @Inject
    private TypeManager typeManager;

    @Provider
    public KeyParserRegistry keyParserRegistry(ServiceExtensionContext context) {
        var keyParserRegistry = new KeyParserRegistryImpl();
        var monitor = context.getMonitor().withPrefix("PrivateKeyResolution");
        keyParserRegistry.register(new JwkParser(typeManager.getMapper(), monitor));
        keyParserRegistry.register(new PemParser(monitor));
        return keyParserRegistry;
    }
}
