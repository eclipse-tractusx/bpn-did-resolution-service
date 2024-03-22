package org.eclipse.tractusx.bdrs.sql.store.schema;

import org.eclipse.edc.sql.statement.SqlStatements;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;

import java.util.List;

public interface DidEntryStoreStatements extends SqlStatements {
    default String getDidEntryTableName() {
        return "edc_did_entries";
    }

    default String getBpnColumn() {
        return "bpn";
    }

    default String getDidColumn() {
        return "did";
    }

    default String getUpdatedAtColumn() {
        return "updated_at";
    }

    String getDeleteByBpnTemplate();

    String getInsertTemplate();

    String getUpdateTemplate();

    String findByBpnTemplate();

    String getLatestVersionStatement();

    String getInsertMultipleStatement(List<DidEntry> entries);

    default String getVersionColumn() {
        return "version";
    }

    String updateLatestVersionTemplate();


    String getMetadataTable();
}
