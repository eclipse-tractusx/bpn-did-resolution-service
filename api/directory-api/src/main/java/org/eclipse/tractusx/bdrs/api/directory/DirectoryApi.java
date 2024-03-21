package org.eclipse.tractusx.bdrs.api.directory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.web.spi.ApiErrorDetail;

/**
 * Provides the public BPN Directory API.
 */
@OpenAPIDefinition
@Tag(name = "BDRS Server Directory API")
public interface DirectoryApi {
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
}
