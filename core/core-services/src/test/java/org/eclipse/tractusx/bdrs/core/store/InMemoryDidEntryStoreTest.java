/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.tractusx.bdrs.core.store;

import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStoreTestBase;

class InMemoryDidEntryStoreTest extends DidEntryStoreTestBase {
    private final InMemoryDidEntryStore store= new InMemoryDidEntryStore(mapper);

    @Override
    protected DidEntryStore getStore() {
        return store;
    }
}
