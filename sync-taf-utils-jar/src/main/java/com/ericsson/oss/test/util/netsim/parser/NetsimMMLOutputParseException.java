/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.test.util.netsim.parser;

@SuppressWarnings("serial")
public class NetsimMMLOutputParseException extends Exception {

	public NetsimMMLOutputParseException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public NetsimMMLOutputParseException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NetsimMMLOutputParseException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
