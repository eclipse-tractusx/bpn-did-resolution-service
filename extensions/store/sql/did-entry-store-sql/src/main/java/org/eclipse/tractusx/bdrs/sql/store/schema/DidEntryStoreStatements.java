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

    /**
     * Returns the template for the SQL statement to find DID entries by DID.
     *
     * @return The SQL statement template for finding DID entries by DID.
     */
    String findByDidTemplate();

    /**
     * Returns the SQL statement template for inserting multiple DID entries.
     *
     * @param entries The list of DID entries to be inserted.
     * @return The SQL statement template for inserting multiple DID entries.
     */
    String getInsertMultipleStatement(List<DidEntry> entries);

    default String getVersionColumn() {
        return "version";
    }

    String updateLatestVersionTemplate();


    String getMetadataTable();
}
