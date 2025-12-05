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
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.web.spi.ApiErrorDetail;
import org.eclipse.tractusx.bdrs.spi.store.DidEntry;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import java.util.Optional;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.ok;

/**
 * Implements the BPN Directory Management API.
 */
@Path("/")
@Produces(APPLICATION_JSON)
public class ManagementApiController implements ManagementApi {
    private final DidEntryStore store;
    private final Monitor monitor;


    public ManagementApiController(DidEntryStore store, Monitor monitor) {
        this.store = store;
        this.monitor = monitor;
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

    public Response save(BpnMapping mapping) {
        String bpn = mapping.bpn();
        String did = mapping.did();
        if (store.exists(bpn)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiErrorDetail.Builder.newInstance()
                            .message("The mapping for '%s' is already exists".formatted(bpn))
                            .build())
                    .build();
        }
        if (store.existsByDid(did)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiErrorDetail.Builder.newInstance()
                            .message("The mapping for '%s' is already exists".formatted(did))
                            .build())
                    .build();
        }
        store.save(new DidEntry(mapping.bpn(), mapping.did()));
        return Response.noContent().build();
    }

    @Override
    @Path("/bpn-directory")
    @PUT
    public Response update(BpnMapping mapping) {
        String bpn = mapping.bpn();
        String did = mapping.did();
        Optional<DidEntry> didEntry = store.getByBpn(bpn);
        if (didEntry.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiErrorDetail.Builder.newInstance()
                            .message("The mapping for '%s' is not exists".formatted(bpn))
                            .build())
                    .build();
        }

        //validate existing did
        Optional<DidEntry> didEntryByDid = store.getByDid(did);
        if (didEntryByDid.isPresent() && !didEntryByDid.get().bpn().equals(bpn)) {
            monitor.severe("The did '%s' is already mapped to another bpn '%s'".formatted(did, didEntryByDid.get().bpn()));
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiErrorDetail.Builder.newInstance()
                            .message("The did '%s' is already mapped to another bpn '%s'".formatted(did, didEntryByDid.get().bpn()))
                            .build())
                    .build();
        }
        if (didEntry.get().did().equals(mapping.did())) {
            monitor.debug("No changes in did for bpn '%s', skipping update record".formatted(mapping.bpn()));
        } else {
            store.update(new DidEntry(mapping.bpn(), mapping.did()));
        }
        return Response.noContent().build();
    }

    @Override
    @Path("/bpn-directory/{bpn}")
    @DELETE
    public void delete(@PathParam("bpn") String bpn) {
        store.delete(bpn);
    }
}
