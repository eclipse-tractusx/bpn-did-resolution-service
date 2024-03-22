package org.eclipse.tractusx.bdrs.sql.store.schema;

import org.eclipse.tractusx.bdrs.spi.store.DidEntry;

import java.util.List;
import java.util.stream.Collectors;

public class PostgresDialectStatements implements DidEntryStoreStatements {
    @Override
    public String getDeleteByBpnTemplate() {
        return executeStatement()
                .column(getBpnColumn())
                .column(getDidColumn())
                .delete(getDidEntryTableName(), getBpnColumn());
    }

    @Override
    public String getInsertTemplate() {
        return executeStatement()
                .column(getBpnColumn())
                .column(getDidColumn())
                .insertInto(getDidEntryTableName());
    }

    @Override
    public String getUpdateTemplate() {
        return executeStatement()
                .column(getBpnColumn())
                .column(getDidColumn())
                .update(getDidEntryTableName(), getBpnColumn());
    }

    @Override
    public String findByBpnTemplate() {
        return "SELECT * FROM %s WHERE %s = ?".formatted(getDidEntryTableName(), getBpnColumn());
    }

    @Override
    public String getLatestVersionStatement() {
        return "SELECT * FROM %s;".formatted(getMetadataTable());
    }

    @Override
    public String getInsertMultipleStatement(List<DidEntry> entries) {
        var str = entries.stream().map(e -> "(?, ?)").collect(Collectors.joining(","));
        return "INSERT INTO %s (%s, %s) VALUES %s;".formatted(getDidEntryTableName(), getBpnColumn(), getDidColumn(), str);
    }

    @Override
    public String updateLatestVersionTemplate() {
        return "UPDATE %s SET %s = ?;".formatted(getMetadataTable(), getVersionColumn());
    }

    @Override
    public String getMetadataTable() {
        return "edc_did_entry_metadata";
    }
}
