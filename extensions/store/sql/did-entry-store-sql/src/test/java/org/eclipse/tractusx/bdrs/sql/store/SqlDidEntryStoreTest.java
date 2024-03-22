package org.eclipse.tractusx.bdrs.sql.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.junit.annotations.ComponentTest;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.testfixtures.PostgresqlStoreSetupExtension;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStoreTestBase;
import org.eclipse.tractusx.bdrs.sql.store.schema.BaseSqlDialectStatements;
import org.eclipse.tractusx.bdrs.sql.store.schema.DidEntryStoreStatements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@ComponentTest
@ExtendWith(PostgresqlStoreSetupExtension.class)
class SqlDidEntryStoreTest extends DidEntryStoreTestBase {

    private final DidEntryStoreStatements statements = new BaseSqlDialectStatements();

    private SqlDidEntryStore didEntryStore;

    @BeforeEach
    void setUp(PostgresqlStoreSetupExtension extension, QueryExecutor queryExecutor) throws IOException {

        didEntryStore = new SqlDidEntryStore(extension.getDataSourceRegistry(), extension.getDatasourceName(),
                extension.getTransactionContext(), new ObjectMapper(), queryExecutor, statements);
        var schema = Files.readString(Paths.get("./docs/schema.sql"));
        extension.runQuery(schema);
    }

    @AfterEach
    void tearDown(PostgresqlStoreSetupExtension extension) {
        extension.runQuery("DROP TABLE " + statements.getDidEntryTableName() + " CASCADE");
    }

    @Override
    protected DidEntryStore getStore() {
        return didEntryStore;
    }

}