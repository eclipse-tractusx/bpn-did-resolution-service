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

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.eclipse.tractusx.bdrs.sql.store.SqlDidEntryStoreExtension.NAME;

@Extension(value = NAME)
public class SqlDidEntryStoreExtension implements ServiceExtension {
    public static final String NAME = "SQL DID Entry Store extension";

    public static final int DEFAULT_PERIOD_SEC = 60;

    @Setting(required = true)
    public static final String DATASOURCE_SETTING_NAME = "edc.datasource.didentry.name";

    @Setting(required = false, value = "Initial delay in seconds before the periodic checking of the database starts. Defaults to a random interval [1..3] sec")
    public static final String INITIAL_DELAY_PROPERTY = "edc.bdrs.didentry.store.cache.initialdelay";

    @Setting(value = "Period in seconds at which the database is polled for updated entries. Defaults to " + DEFAULT_PERIOD_SEC + " sec.")
    public static final String PERIOD_PROPERTY = "edc.bdrs.didentry.store.cache.period";
    public static final String MONITOR_PREFIX = "SQL DidEntry Store";


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
    private SqlDidEntryStore store;
    private ScheduledFuture<?> periodicFuture;
    private ServiceExtensionContext context;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.context = context;
    }

    @Override
    public void start() {
        long initialDelay = context.getConfig().getInteger(INITIAL_DELAY_PROPERTY, randomDelay());
        long period = context.getConfig().getInteger(PERIOD_PROPERTY, DEFAULT_PERIOD_SEC);
        context.getMonitor().withPrefix(MONITOR_PREFIX)
                .debug("Schedule periodic cache update every %d seconds, starting in %d seconds".formatted(period, initialDelay));
        var executor = Executors.newSingleThreadScheduledExecutor();
        periodicFuture = executor.scheduleAtFixedRate(() -> store.updateCache(), initialDelay, period, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        if (periodicFuture != null && !periodicFuture.isCancelled()) {
            periodicFuture.cancel(true);
        }
    }

    @Provider
    public DidEntryStore createSqlDidEntryStore(ServiceExtensionContext context) {
        if (store == null) {
            var dataSourceName = context.getConfig().getString(DATASOURCE_SETTING_NAME, DataSourceRegistry.DEFAULT_DATASOURCE);
            store = new SqlDidEntryStore(dataSourceRegistry, dataSourceName, transactionContext, typeManager.getMapper(), queryExecutor, getDialect(),
                    context.getMonitor().withPrefix(MONITOR_PREFIX));
        }
        return store;
    }

    private int randomDelay() {
        return 1 + new Random().nextInt(4);
    }

    private DidEntryStoreStatements getDialect() {
        return dialect != null ? dialect : new PostgresDialectStatements();
    }
}
