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

package org.eclipse.tractusx.bdrs.api.directory.authentication;

import org.eclipse.edc.keys.KeyParserRegistryImpl;
import org.eclipse.edc.keys.keyparsers.JwkParser;
import org.eclipse.edc.keys.keyparsers.PemParser;
import org.eclipse.edc.keys.spi.KeyParserRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

/**
 * This extension must be separate from the {@link CredentialBasedAuthenticationExtension} to avoid a cyclic dependency
 */
@Extension(value = "Provides a KeyParserRegistry")
public class KeyParserRegistryExtension implements ServiceExtension {
    @Inject
    private TypeManager typeManager;

    @Provider
    public KeyParserRegistry keyParserRegistry(ServiceExtensionContext context) {
        var keyParserRegistry = new KeyParserRegistryImpl();
        var monitor = context.getMonitor().withPrefix("PrivateKeyResolution");
        keyParserRegistry.register(new JwkParser(typeManager.getMapper(), monitor));
        keyParserRegistry.register(new PemParser(monitor));
        return keyParserRegistry;
    }
}
