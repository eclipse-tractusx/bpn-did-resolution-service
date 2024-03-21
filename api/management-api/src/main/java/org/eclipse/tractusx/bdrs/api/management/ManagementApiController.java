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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.ok;

/**
 * Implements the BPN Directory Management API.
 */
@Path("/")
@Produces(APPLICATION_JSON)
public class ManagementApiController implements ManagementApi {
    private final DidEntryStore store;

    public ManagementApiController(DidEntryStore store) {
        this.store = store;
    }

    @Override
    @Path("/bpn-directory")
    @GET
    public Response getData() {
        return ok().entity((StreamingOutput) stream -> stream.write(store.entries()))
                .encoding("gzip")
                .build();
    }

    @Override
    @Path("/bpn-directory")
    @POST
    public void save(BpnMapping mapping) {
        store.save(new DidEntry(mapping.bpn(), mapping.did()));
    }

    @Override
    @Path("/bpn-directory")
    @PUT
    public void update(BpnMapping mapping) {
        store.update(new DidEntry(mapping.bpn(), mapping.did()));
    }

    @Override
    @Path("/bpn-directory/{bpn}")
    @DELETE
    public void delete(@PathParam("bpn") String bpn) {
        store.delete(bpn);
    }
}
