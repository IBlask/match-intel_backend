package com.match_intel.backend.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})    //unneeded RuntimeException attributes
public class ClientErrorException extends RuntimeException {

    @Schema(description = "Timestamp when error occurred", type = "string", example = "2024-11-30T10:02:43")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    @Getter
    @Schema(description = "HTTP status code", example = "400")
    @JsonSerialize(as = Integer.class)
    private final int statusCode;

    @Schema(description = "HTTP status message", example = "Bad Request")
    @JsonSerialize(as = String.class)
    private final String error;

    @Schema(description = "Server's error message", example = "No data provided!")
    @JsonSerialize(as = String.class)
    private final String message;


    public ClientErrorException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.timestamp = LocalDateTime.now();
        this.statusCode = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = errorMessage;
    }


    @Override
    public String getMessage() {
        return this.message;
    }
}