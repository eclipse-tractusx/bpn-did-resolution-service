/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *  Copyright (c) 2025 Cofinity-X GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *       Cofinity-X GmbH
 *
 */
package org.eclipse.tractusx.bdrs.api.directory.authentication;

import dev.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import org.eclipse.edc.http.client.EdcHttpClientImpl;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

@Extension("HTTP Client Extension")
public class HttpClientExtension  implements ServiceExtension {
    public static final String MONITOR_PREFIX = "Presentation Transformation";

    @Provider
    public EdcHttpClient httpClient(ServiceExtensionContext context) {
        return new EdcHttpClientImpl(new OkHttpClient(), RetryPolicy.ofDefaults(), context.getMonitor().withPrefix(MONITOR_PREFIX));
    }
}
