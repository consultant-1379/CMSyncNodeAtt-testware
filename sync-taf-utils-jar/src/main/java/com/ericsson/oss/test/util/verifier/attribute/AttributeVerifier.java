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
package com.ericsson.oss.test.util.verifier.attribute;

public class AttributeVerifier {

    public boolean compareTypeAndValue(Object dpsValue, Object netsimValue, final String dataType) {
        if (netsimValue == null && dpsValue == null) {
            return true;

        }
        if (netsimValue != null && ("null".equals(netsimValue) || netsimValue.equals("\"\""))) {
            netsimValue = "";
            if (dpsValue == null) {
                dpsValue = "";
            }
        }

        boolean testResult = false;
        if (netsimValue != null && dpsValue != null) {
            if (dataType.equalsIgnoreCase("long")) {
                testResult = ((Long) dpsValue).longValue() == (Long.valueOf((String) netsimValue)).longValue();
            } else if (dataType.equalsIgnoreCase("integer")) {
                testResult = ((Integer) dpsValue).intValue() == (Integer.valueOf((String) netsimValue)).intValue();
            } else if (dataType.equalsIgnoreCase("double")) {

                testResult = ((Double) dpsValue).doubleValue() == (Double.valueOf((String) netsimValue)).doubleValue();
            } else if (dataType.equalsIgnoreCase("boolean")) {

                testResult = ((Boolean) dpsValue).booleanValue() == Boolean.valueOf((String) netsimValue).booleanValue();
            } else if (dataType.equalsIgnoreCase("short")) {
                testResult = ((Short) dpsValue).shortValue() == (Short.valueOf((String) netsimValue)).shortValue();

            } else if (dataType.equalsIgnoreCase("byte")) {
                testResult = ((Byte) dpsValue).byteValue() == (Byte.valueOf((String) netsimValue)).byteValue();
            } else {
                testResult = dpsValue.equals(netsimValue);
            }
        }
        return testResult;
    }
}
