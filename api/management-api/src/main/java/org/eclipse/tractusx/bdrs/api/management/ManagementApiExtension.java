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

package org.eclipse.tractusx.bdrs.api.management;

import org.eclipse.edc.api.auth.spi.AuthenticationRequestFilter;
import org.eclipse.edc.api.auth.spi.AuthenticationService;
import org.eclipse.edc.runtime.metamodel.annotation.BaseExtension;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebServer;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import static org.eclipse.tractusx.bdrs.api.management.ManagementApiExtension.NAME;

/**
 * Loads resources for the BPN Directory Management API.
 */
@BaseExtension
@Extension(NAME)
public class ManagementApiExtension implements ServiceExtension {
    public static final String NAME = "Management API";
    static final String CONTEXT_NAME = "management";
    static final String PATH = "/management/v1";
    static final int PORT = 8282;

    @Inject
    private DidEntryStore store;

    @Inject
    private WebService webService;

    @Inject
    private WebServer webServer;

    @Inject
    private AuthenticationService authService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        webServer.addPortMapping(CONTEXT_NAME, PORT, PATH);
        webService.registerResource(CONTEXT_NAME, new ManagementApiController(store));
        webService.registerResource(CONTEXT_NAME, new AuthenticationRequestFilter(authService));
    }

}

