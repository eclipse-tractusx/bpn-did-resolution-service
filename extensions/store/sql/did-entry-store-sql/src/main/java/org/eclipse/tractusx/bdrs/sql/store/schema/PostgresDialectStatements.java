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
        return "UPDATE %s SET %s = ?, %s = ?;".formatted(getMetadataTable(), getVersionColumn(), getUpdatedAtColumn());
    }

    @Override
    public String getMetadataTable() {
        return "edc_did_entry_metadata";
    }
}
