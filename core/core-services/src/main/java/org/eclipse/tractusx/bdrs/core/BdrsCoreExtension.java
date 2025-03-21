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

package org.eclipse.tractusx.bdrs.core;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.tractusx.bdrs.core.store.InMemoryDidEntryStore;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import static org.eclipse.tractusx.bdrs.core.BdrsCoreExtension.NAME;

/**
 * Loads BDRS core services
 */
@Extension(NAME)
public class BdrsCoreExtension implements ServiceExtension {
    public static final String NAME = "BDRS Core";

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Provider(isDefault = true)
    public DidEntryStore defaultDidEntryStore() {
        return new InMemoryDidEntryStore(typeManager.getMapper());
    }

}
