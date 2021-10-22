package org.onap.cps.ncmp.dmi.exception;

import org.springframework.http.HttpStatus;

/*
Use this exception when SDNC expected contracts fails
 */
public class SDNCException extends DmiException {

    private static final String HTTP_RESPONSE_DETAILS_FORMAT = "sdnc http status: %s, response body : %s ";

    public SDNCException(String message, HttpStatus httpStatus, String responseBody) {
        super(message, String.format(HTTP_RESPONSE_DETAILS_FORMAT, httpStatus.toString(), responseBody));
    }

    public SDNCException(String message, String details, Throwable cause) {
        super(message, details, cause);
    }

}
