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

package org.eclipse.tractusx.bdrs.api.directory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.tractusx.bdrs.spi.store.DidEntryStore;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.ok;

/**
 * Implements the BPN Directory API. The BPN Directory is returned in compressed (GZIP) form.
 */
@Path("/")
@Produces(APPLICATION_JSON)
public class DirectoryApiController implements DirectoryApi {
    private final DidEntryStore store;

    public DirectoryApiController(DidEntryStore store) {
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

}
