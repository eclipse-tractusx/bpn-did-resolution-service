package org.eclipse.tractusx.bdrs.sql.store.schema;

import org.eclipse.edc.sql.statement.SqlStatements;

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

    String getDeleteByBpnTemplate();

    String getInsertTemplate();

    String getUpdateTemplate();

    String findByBpnTemplate();
}
