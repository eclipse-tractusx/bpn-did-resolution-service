package org.eclipse.tractusx.bdrs.sql.store;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.eclipse.tractusx.bdrs.sql.store.schema.DidEntryStoreStatements;
import org.eclipse.tractusx.bdrs.sql.store.schema.PostgresDialectStatements;

import static org.eclipse.tractusx.bdrs.sql.store.SqlDidEntryStoreExtension.NAME;

@Extension(value = NAME)
public class SqlDidEntryStoreExtension implements ServiceExtension {
    public static final String NAME = "SQL DID Entry Store extension";

    @Setting(required = true)
    public static final String DATASOURCE_SETTING_NAME = "edc.datasource.didentry.name";

    @Inject
    private DataSourceRegistry dataSourceRegistry;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private TypeManager typeManager;

    @Inject
    private QueryExecutor queryExecutor;

    @Inject(required = false)
    private DidEntryStoreStatements dialect;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public DidEntryStore createSqlDidEntryStore(ServiceExtensionContext context) {
        var dataSourceName = context.getConfig().getString(DATASOURCE_SETTING_NAME, DataSourceRegistry.DEFAULT_DATASOURCE);
        return new SqlDidEntryStore(dataSourceRegistry, dataSourceName, transactionContext, typeManager.getMapper(), queryExecutor, getDialect());
    }

    private DidEntryStoreStatements getDialect() {
        return dialect != null ? dialect : new PostgresDialectStatements();
    }
}
