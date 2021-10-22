package org.onap.cps.ncmp.dmi.exception;

import org.springframework.http.HttpStatus;

/*
To represent SDNC Exception correctly
 */
public class SDNCException extends DmiException {

    private static final String DETAILS_FORMAT = "sdnc http status: %s, response body : %s ";

    public SDNCException(String message, HttpStatus httpStatus, String responseBody) {
        super(message, String.format(DETAILS_FORMAT, httpStatus.toString(), responseBody));
    }

    public SDNCException(String message, String details) {
        super(message, details);
    }
}
