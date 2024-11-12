package com.match_intel.backend.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})    //unneeded RuntimeException attributes
public class ServerErrorException extends RuntimeException {

    @JsonSerialize(as = LocalDateTime.class)
    private final LocalDateTime timestamp;
    @JsonSerialize(as = Integer.class)
    private final int status;
    @JsonSerialize(as = String.class)
    private final String error;
    @JsonSerialize(as = String.class)
    private final String message;


    public ServerErrorException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.timestamp = LocalDateTime.now();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = errorMessage;
    }


    public int getStatus() {
        return status;
    }
}
