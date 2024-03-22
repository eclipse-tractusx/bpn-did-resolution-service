package org.eclipse.tractusx.bdrs.sql.store;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.spi.system.ServiceExtension;

import static org.eclipse.tractusx.bdrs.sql.store.SqlDidEntryStoreExtension.NAME;

@Extension(value = NAME)
public class SqlDidEntryStoreExtension implements ServiceExtension {
    public static final String NAME = "SQL DID Entry Store extension";

    @Override
    public String name() {
        return NAME;
    }
}
