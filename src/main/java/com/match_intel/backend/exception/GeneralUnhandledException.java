package com.match_intel.backend.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})    //unneeded RuntimeException attributes
public class GeneralUnhandledException extends RuntimeException{

    public GeneralUnhandledException(String errorMessage) {
        super(errorMessage);
    }
}
