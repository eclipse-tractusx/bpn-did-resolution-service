package org.eclipse.tractusx.bdrs.sql.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class SqlDidEntryStore extends AbstractSqlStore implements DidEntryStore {
    private final ObjectMapper mapper;
    private final DidEntryStoreStatements statements;
    private final AtomicReference<byte[]> cache = new AtomicReference<>();
    private final Monitor monitor;
    private int latestVersion = 0;

    public SqlDidEntryStore(DataSourceRegistry dataSourceRegistry,
                            String dataSourceName,
                            TransactionContext transactionContext,
                            ObjectMapper objectMapper,
                            QueryExecutor queryExecutor, DidEntryStoreStatements statements, Monitor monitor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.mapper = objectMapper;
        this.statements = statements;

        // cannot invalidate the cache here, because the data source may not yet be initialized
        this.monitor = monitor;
    }

    @Override
    public byte[] entries() {
        if (cache.get() == null) {
            invalidateCache();
        }
        return cache.get();
    }

    @Override
    public void save(DidEntry entry) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                upsert(connection, entry);
                invalidateCache();
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public void save(Stream<DidEntry> entries) {
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {

                //do a batch insert. only 65.535 params are supported in one batch, so we segment
                var chunkSize = 32_000;

                var entryList = chunked(entries.toList(), chunkSize);

                entryList.forEach(chunk -> {
                    var stmt = statements.getInsertMultipleStatement(chunk);

                    var params = chunk.stream().map(e -> List.of(e.bpn(), e.did()))
                            .flatMap(List::stream)
                            .toArray();

                    queryExecutor.execute(connection, stmt, params);
                });

                updateLatestVersion(connection);
                invalidateCache();
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
                invalidateCache();
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
                    updateLatestVersion(connection);
                    invalidateCache();
                }
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    /**
     * This method performs a cache update by first checking if the database has new data available, and if it does, reloads the
     * internal cache with values from the database.
     * As a means of change detection, the {@code version} field is used.
     * <p>
     * This method is transactional.
     */
    public void updateCache() {
        monitor.debug("Checking if cache is out-of-date");
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var dbVersion = getLatestVersion(connection);
                if (dbVersion > latestVersion) {
                    monitor.debug("Local version is %s, database version is %d, will update cache".formatted(latestVersion, dbVersion));
                    invalidateCache();
                    latestVersion = dbVersion;
                }
            } catch (SQLException e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    /**
     * obtains the latest version information of the did entry list from the database. If no such entry exists, the locally held {@code latestVersion} is returned.
     */
    private int getLatestVersion(Connection connection) {
        var stmt = statements.getLatestVersionStatement();
        var list = queryExecutor.query(connection, true, r -> r.getInt(statements.getVersionColumn()), stmt);
        return list.findFirst().orElse(latestVersion);
    }

    /**
     * Partitions the list in to smaller sub-lists, each containing {@code chunkSize} elements
     * Algorithm borrowed from <a href="https://davidvlijmincx.com/posts/split_a_list_in_java/">here</a>
     */
    private List<List<DidEntry>> chunked(List<DidEntry> list, int chunkSize) {
        var counter = new AtomicInteger();
        var mapOfChunks = list.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize));
        // Create a list containing the lists of chunks
        return new ArrayList<>(mapOfChunks.values());

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
        updateLatestVersion(connection);
    }

    /**
     * Obtains the latest version from the database, increments it by 1 and writes it back to the database
     */
    private void updateLatestVersion(Connection connection) {
        latestVersion = getLatestVersion(connection) + 1;
        var stmt = statements.updateLatestVersionTemplate();
        queryExecutor.execute(connection, stmt, latestVersion, Timestamp.from(Instant.now()));
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

    /**
     * loads ALL entries from the database and puts them in a local cache.
     * This method is NOT transactional, and may only be called inside a transaction!
     */
    private void invalidateCache() {
        try (var connection = getConnection()) {

            var result = queryExecutor.query(connection, true, this::mapDidEntry, "SELECT * FROM %s".formatted(statements.getDidEntryTableName()))
                    .collect(Collectors.toMap(DidEntry::bpn, DidEntry::did));
            var bas = new ByteArrayOutputStream();
            try (var gzip = new GZIPOutputStream(bas)) {
                gzip.write(mapper.writeValueAsBytes(result));
            } catch (IOException e) {
                throw new EdcException(e);
            }
            cache.set(bas.toByteArray());

        } catch (SQLException e) {
            throw new EdcPersistenceException(e);
        }
    }
}
