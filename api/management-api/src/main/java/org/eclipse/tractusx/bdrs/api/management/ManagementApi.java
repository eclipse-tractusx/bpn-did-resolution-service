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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.web.spi.ApiErrorDetail;

/**
 * Exposes the BDRS management API. Note that this API should not be exposed over a public network.
 */
@OpenAPIDefinition
@Tag(name = "BDRS Server Management API")
public interface ManagementApi {
    @Operation(description = "Gets a binary gzipped stream with BPN/DID mapping entries.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "The GZipped binary stream contains BPN-to-DID mapping entries."),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "401", description = "User is not authenticated",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "403", description = "User is not authorized to obtain BPN/DID mapping data",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class))))

            })
    Response getData();

    @Operation(description = "Creates a new BpnMapping entry, or updates an existing one.",
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = BpnMapping.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "BPN/DID mapping entry successfully created (updated)."),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "401", description = "User is not authenticated",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "403", description = "User is not authorized to create BPN/DID mapping data",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class))))
            })
    void save(BpnMapping mapping);

    @Operation(description = "Updates a BpnMapping entry",
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = BpnMapping.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "BPN/DID mapping entry successfully created"),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "401", description = "User is not authenticated",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "403", description = "User is not authorized to update BPN/DID mapping data",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "404", description = "No mapping for that BPN was registered",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
            })
    void update(BpnMapping mapping);

    @Operation(description = "Removes a BpnMapping entry for the given BPN",
            responses = {
                    @ApiResponse(responseCode = "204", description = "BpnMapping entry was deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Request body was malformed",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "401", description = "User is not authenticated",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "403", description = "User is not authorized to delete BPN/DID mapping data",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
                    @ApiResponse(responseCode = "404", description = "No mapping for that BPN was registered",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApiErrorDetail.class)))),
            })
    void delete(@PathParam("bpn") String bpn);
}
