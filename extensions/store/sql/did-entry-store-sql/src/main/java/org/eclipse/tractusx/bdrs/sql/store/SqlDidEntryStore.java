package org.eclipse.tractusx.bdrs.sql.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.eclipse.tractusx.bdrs.sql.store.schema.DidEntryStoreStatements;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class SqlDidEntryStore extends AbstractSqlStore implements DidEntryStore {
    private final ObjectMapper mapper;
    private final DidEntryStoreStatements statements;

    public SqlDidEntryStore(DataSourceRegistry dataSourceRegistry,
                            String dataSourceName,
                            TransactionContext transactionContext,
                            ObjectMapper objectMapper,
                            QueryExecutor queryExecutor, DidEntryStoreStatements statements) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.mapper = objectMapper;
        this.statements = statements;
    }

    @Override
    public byte[] entries() {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {

                var result = queryExecutor.query(connection, true, this::mapDidEntry, "SELECT * FROM %s".formatted(statements.getDidEntryTableName()))
                        .collect(Collectors.toMap(DidEntry::bpn, DidEntry::did));
                var bas = new ByteArrayOutputStream();
                try (var gzip = new GZIPOutputStream(bas)) {
                    gzip.write(mapper.writeValueAsBytes(result));
                } catch (IOException e) {
                    throw new EdcException(e);
                }
                return bas.toByteArray();

            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void save(DidEntry entry) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                upsert(connection, entry);
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void save(Stream<DidEntry> entries) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                entries.forEach(e -> upsert(connection, e));
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void update(DidEntry entry) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                upsert(connection, entry);
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void delete(String bpn) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                if (findByBpn(connection, bpn) != null) {
                    var stmt = statements.getDeleteByBpnTemplate();
                    queryExecutor.execute(connection, stmt, bpn);
                }
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    /**
     * inserts or updates the {@link DidEntry}
     * This method is NOT transactional, and may only be called inside a transaction!
     */
    private void upsert(Connection connection, DidEntry entry) {
        if (findByBpn(connection, entry.bpn()) != null) {
            update(connection, entry);
        } else {
            insert(connection, entry);
        }
    }

    /**
     * returns the {@link DidEntry} for the given BPN or null.
     * This method is NOT transactional, and may only be called inside a transaction!
     */
    private @Nullable DidEntry findByBpn(Connection connection, String bpn) {
        var stmt = statements.findByBpnTemplate();
        return queryExecutor.single(connection, false, this::mapDidEntry, stmt, bpn);
    }

    private DidEntry mapDidEntry(ResultSet resultSet) throws SQLException {
        var bpn = resultSet.getString(statements.getBpnColumn());
        var did = resultSet.getString(statements.getDidColumn());
        return new DidEntry(bpn, did);
    }

    /**
     * inserts the {@link DidEntry}
     * This method is NOT transactional, and may only be called inside a transaction!
     */
    private void insert(Connection connection, DidEntry entry) {
        var stmt = statements.getInsertTemplate();
        queryExecutor.execute(connection, stmt, entry.bpn(), entry.did());
    }

    /**
     * updates the {@link DidEntry}
     * This method is NOT transactional, and may only be called inside a transaction!
     */
    private void update(Connection connection, DidEntry entry) {
        var stmt = statements.getUpdateTemplate();
        queryExecutor.execute(connection, stmt, entry.bpn(), entry.did(), entry.bpn());
    }
}
