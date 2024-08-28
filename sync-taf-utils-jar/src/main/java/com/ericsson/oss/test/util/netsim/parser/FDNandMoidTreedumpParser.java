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

import java.util.HashMap;
import java.util.Map;

public class FDNandMoidTreedumpParser {

    private static final String SET = "SET";
    private static final String COMMA = ",";
    private static final String EQUALS = "=";
    private static final String IDENTITY = "identity \"";
    private static final String MOID_COMMENT = "// moid = ";
    private static final String PARENT = "parent \"";
    private static final String TYPE = "moType ";
    private static final String ID = "\"%sId\" String \"";

    public static Map<String, Long> parseTreeMoDumpOutput(final String input, final String nodeName) {
        final Map<String, Long> fdns = new HashMap<>();

        final String newLineString = Utils.getCorrectNewLineString(input);
        final String[] attLines = input.split(newLineString);
        for (final String line : attLines) {
            final String strippedLine = line.trim();
            if (Utils.isEmpty(strippedLine)) {
                continue;
            }

            if (processLine(strippedLine, fdns, nodeName)) {
                break;
            }
        }
        return fdns;
    }

    //  CREATE
    //  (
    //      parent "ManagedElement=1,TransportNetwork=1"
    //      // moid = 939
    //      identity "1"
    //      moType Sctp
    //      exception none
    //      nrOfAttributes 64
    //      "SctpId" String "1"
    //       .
    //       .
    //   )

    // state
    private static String parent = null;
    private static String moType = null;
    private static String moid = null;
    private static String moTypeId = null;
    private static String identity = null;

    private static boolean processLine(final String line, final Map<String, Long> fdns, final String nodeName) {

        // done! quitting
        if (SET.equals(line)) {
            return true;
        }

        if (line.startsWith(PARENT)) {
            parent = line.substring(PARENT.length(), line.length() - 1);
            return false;
        }

        // no point continuing unless parent value found
        if (parent != null) {
            if (line.startsWith(MOID_COMMENT)) {
                moid = line.substring(MOID_COMMENT.length());
            } else if (line.startsWith(TYPE)) {
                moType = line.substring(TYPE.length());
                moTypeId = String.format(ID, moType);
            } else if (line.startsWith(IDENTITY)) {
                identity = line.substring(IDENTITY.length(), line.length() - 1);
            } else if (moTypeId != null && line.startsWith(moTypeId)) {

                final String idStr = identity;
                final Long id = Long.parseLong(moid);
                final StringBuilder fdn = new StringBuilder(parent);
                if (!Utils.isEmpty(parent)) {
                    fdn.append(COMMA);
                }
                fdn.append(moType);
                fdn.append(EQUALS);
                fdn.append(idStr);
                fdns.put("MeContext=" + nodeName + "," + fdn.toString(), id);
                parent = null;
            }
        }
        return false;
    }
}
