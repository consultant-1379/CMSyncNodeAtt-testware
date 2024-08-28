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

import java.util.Map;

import org.apache.log4j.Logger;

import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class MoRefAttributeVerifier extends AttributeVerifier {

    private static Logger LOGGER = Logger.getLogger(MoRefAttributeVerifier.class);

    public void compareMoRef(final CompareResponse response, final String attributeName, final Object dpsValue, final Object netsimValue,
                             final Map<String, Long> moRef, final String dataType) {

        boolean testResult = true;
        final String meContextFdn = getNodeName(response);
        if (isEmpty(netsimValue) && isEmpty(dpsValue)) {
            testResult = true;
        } else if (netsimValue != null && dpsValue != null && moRef.get(meContextFdn + "," + dpsValue) == null) {
            testResult = false;
        } else {
            testResult = moRef.get(meContextFdn + "," + dpsValue).toString().equalsIgnoreCase(netsimValue.toString());
        }
        if (testResult) {
            response.addSuccess(new AttributeComparison(attributeName, dataType.toString(), dataType.toString(), dpsValue, netsimValue, true));

        } else {
            response.addError(new AttributeComparison(attributeName, dataType.toString(), dataType.toString(), dpsValue, netsimValue, false));
        }
    }

    private boolean isEmpty(final Object str) {
        return str == null || "".equals(str.toString().trim());
    }

    private String getNodeName(final CompareResponse response) {
        final String fdn = response.getFdn();
        final String[] splitFdn = fdn.split(",");
        return splitFdn[0];
    }
}
