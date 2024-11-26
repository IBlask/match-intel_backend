package com.match_intel.backend.handler;

import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.ServerErrorException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientErrorException.class)
    @ApiResponse(
            responseCode = "4xx",
            description = "Client error",
            content = @Content(schema = @Schema(implementation = ClientErrorException.class))
    )
    public ResponseEntity<ClientErrorException> handleClientErrorException(ClientErrorException exception) {
        return new ResponseEntity<>(exception, HttpStatusCode.valueOf(exception.getStatusCode()));
    }


    @ExceptionHandler(ServerErrorException.class)
    @ApiResponse(
            responseCode = "5xx",
            description = "Server error",
            content = @Content(schema = @Schema(implementation = ServerErrorException.class))
    )
    public ResponseEntity<ServerErrorException> handleServerErrorException(ServerErrorException exception) {
        return new ResponseEntity<>(exception, HttpStatusCode.valueOf(exception.getStatusCode()));
    }
}
