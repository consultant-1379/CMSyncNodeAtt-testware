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

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class SimpleAttributeVerifier extends AttributeVerifier {

    public void compareSimpleDataTypeValue(final CompareResponse response, final String attributeName, final DataType modelServiceDataType,
                                           final Object dpsValue, final Object netsimValue) {

        String dpsDataType = null;
        boolean checkDataType = true;

        if (dpsValue != null) {
            dpsDataType = dpsValue.getClass().getSimpleName();
            checkDataType = modelServiceDataType.toString().equalsIgnoreCase(dpsDataType);
        }

        final boolean testResult = compareTypeAndValue(dpsValue, netsimValue, modelServiceDataType.toString());
        if (testResult && checkDataType) {
            response.addSuccess(new AttributeComparison(attributeName, dpsDataType, modelServiceDataType.toString(), dpsValue, netsimValue, true));

        } else {
            response.addError(new AttributeComparison(attributeName, dpsDataType, modelServiceDataType.toString(), dpsValue, netsimValue, false));
        }
    }

}
