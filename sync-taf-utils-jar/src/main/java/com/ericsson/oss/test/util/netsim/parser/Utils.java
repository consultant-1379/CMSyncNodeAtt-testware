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

public class Utils {

	// dosnt work, need to be careful & check what happens in Linux env
		//private static final String NEW_LINE   = System.lineSeparator();
		private static final String NEW_LINE_LINUX   = "\n";
		private static final String NEW_LINE_WIN     = "\r\n";

		public static boolean isEmpty(final String str ) {
			return str == null || "".equals(str.trim());
		}
		
		public static String getCorrectNewLineString(final String input) {
			if(input.indexOf(NEW_LINE_WIN) > 0) {
				return NEW_LINE_WIN;
			}else {
				return NEW_LINE_LINUX;
			}
		}
}
