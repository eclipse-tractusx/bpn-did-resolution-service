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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.junit.annotations.PostgresqlIntegrationTest;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.testfixtures.PostgresqlStoreSetupExtension;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStoreTestBase;
import org.eclipse.tractusx.bdrs.sql.store.schema.DidEntryStoreStatements;
import org.eclipse.tractusx.bdrs.sql.store.schema.PostgresDialectStatements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.stream.IntStream;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@PostgresqlIntegrationTest
@ExtendWith(PostgresqlStoreSetupExtension.class)
class SqlDidEntryStoreTest extends DidEntryStoreTestBase {

    private final DidEntryStoreStatements statements = new PostgresDialectStatements();

    private SqlDidEntryStore didEntryStore;
    private DataSource dataSource;
    private QueryExecutor queryExecutor;

    @BeforeEach
    void setUp(PostgresqlStoreSetupExtension extension, QueryExecutor queryExecutor) throws IOException {

        var schema = Files.readString(Paths.get("./docs/schema.sql"));
        extension.runQuery(schema);

        var dataSourceRegistry = extension.getDataSourceRegistry();
        var datasourceName = extension.getDatasourceName();
        dataSource = dataSourceRegistry.resolve(datasourceName);
        this.queryExecutor = queryExecutor;
        didEntryStore = new SqlDidEntryStore(dataSourceRegistry, datasourceName,
                extension.getTransactionContext(), new ObjectMapper(), this.queryExecutor, statements, mock());
    }

    @AfterEach
    void tearDown(PostgresqlStoreSetupExtension extension) {
        extension.runQuery("DROP TABLE " + statements.getDidEntryTableName() + " CASCADE");
        extension.runQuery("DROP TABLE " + statements.getMetadataTable() + " CASCADE");
    }

    @Test
    void saveStream_assertVersionUpdate() {
        var entries = IntStream.range(0, 10)
                .mapToObj(i -> new DidEntry("bpn" + i, "did" + i));
        getStore().save(entries);

        try (var conn = dataSource.getConnection()) {
            var stmt = statements.getLatestVersionStatement();
            var list = queryExecutor.query(conn, true, r -> r.getInt(statements.getVersionColumn()), stmt);
            assertThat(list).containsExactly(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void save_assertVersionUpdate() {
        getStore().save(new DidEntry("bpn1", "did:web:1"));
        getStore().save(new DidEntry("bpn2", "did:web:2"));
        getStore().save(new DidEntry("bpn3", "did:web:3"));

        try (var conn = dataSource.getConnection()) {
            var stmt = statements.getLatestVersionStatement();
            var list = queryExecutor.query(conn, true, r -> r.getInt(statements.getVersionColumn()), stmt);
            assertThat(list).containsExactly(3);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DidEntryStore getStore() {
        return didEntryStore;
    }

}