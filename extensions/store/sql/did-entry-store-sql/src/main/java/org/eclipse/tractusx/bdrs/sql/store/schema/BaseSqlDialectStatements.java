package org.eclipse.tractusx.bdrs.sql.store.schema;

public class BaseSqlDialectStatements implements DidEntryStoreStatements {
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
}
