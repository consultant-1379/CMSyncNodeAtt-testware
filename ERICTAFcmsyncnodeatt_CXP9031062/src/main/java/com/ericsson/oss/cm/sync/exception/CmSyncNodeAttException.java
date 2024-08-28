/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.cm.sync.exception;

/**
 * Custom runtime exception signaling a problem in getting an MO in DPS.
 */
public class CmSyncNodeAttException extends RuntimeException {

    private static final long serialVersionUID = -4726708227623824220L;

    /**
     * Create a CM Sync Node Attribute exception that will contain message that caused the exception
     * 
     * @param message
     *            the message for this exception
     */
    public CmSyncNodeAttException(final String message) {
        super(message);
    }

    /**
     * Create a CM Sync Node Attribute exception that will contain message and the cause
     * 
     * @param message
     *            the message for this exception
     * @param cause
     *            the underlying exception which caused this problem
     */
    public CmSyncNodeAttException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
