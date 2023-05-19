package com.wireguard.api.controllers;

import com.wireguard.external.wireguard.dto.WgInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeersAPIController {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WgInterface.class)) }),
            @ApiResponse(responseCode = "404", description = "No interface found",
                    content = @Content) })
    @GetMapping("/interface")
    public WgInterface getInterface() {
        return new WgInterface("privateKey", "publicKey", 1234, 0);
    }
}
