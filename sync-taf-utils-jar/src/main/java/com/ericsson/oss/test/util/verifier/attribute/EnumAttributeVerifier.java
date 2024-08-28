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

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeMemberSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;
import com.ericsson.oss.test.util.verifier.helper.ModelServiceHelper;

public class EnumAttributeVerifier extends AttributeVerifier {

    public void checkEnumAttributes(final CompareResponse response, final String attributeName, final ModelInfo modelInfo, final Object dpsValue,
                                    final Object netsimValue, String parentDataType) {

        final EnumDataTypeSpecification enumTypeAttributes = getModelServiceHelper().getEnumTypeAttributes(modelInfo);
        boolean checkValue = false;

        if (dpsValue != null && netsimValue != null) {
            final EnumDataTypeMemberSpecification memberByName = enumTypeAttributes.getMemberByName((String) dpsValue);
            if (memberByName != null) {
                checkValue = memberByName.getMemberValue() == Integer.valueOf(netsimValue.toString());
            }
        } else if ((dpsValue == null) && (netsimValue == null)) {
            checkValue = true;
        }
        if (parentDataType == null || parentDataType.length() == 0) {
            parentDataType = "enum";
        } else {
            parentDataType = parentDataType + "->enum";
        }
        if (checkValue) {
            response.addSuccess(new AttributeComparison(attributeName, parentDataType, parentDataType, dpsValue, netsimValue, true));

        } else {
            response.addError(new AttributeComparison(attributeName, parentDataType, parentDataType, dpsValue, netsimValue, false));
        }
    }

    private ModelServiceHelper getModelServiceHelper() {
        return ModelServiceHelper.getInstance();
    }
}
