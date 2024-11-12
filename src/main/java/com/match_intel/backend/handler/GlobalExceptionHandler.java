package com.match_intel.backend.handler;

import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.ServerErrorException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ClientErrorException> handleClientErrorException(ClientErrorException exception) {
        return new ResponseEntity<>(exception, HttpStatusCode.valueOf(exception.getStatus()));
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<ServerErrorException> handleServerErrorException(ServerErrorException exception) {
        return new ResponseEntity<>(exception, HttpStatusCode.valueOf(exception.getStatus()));
    }
}
