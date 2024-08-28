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

import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.COMPLEX_REF;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.ENUM_REF;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.MO_REF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.EModelAttributeSpecification;
import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class ListAttributeVerifier extends AttributeVerifier {

    public void compareListDataTypeValue(final CompareResponse response, final EModelAttributeSpecification attributeSpec, final Object dpsValue,
                                         final Map<String, Object> netSimData, final Map<String, Long> moRef) {
        final String attributeName = attributeSpec.getName();
        final Object netsimListValue = netSimData.get(attributeName.toLowerCase());

        String dpsDT = "";
        String netsimDT = "";
        if (dpsValue != null) {
            dpsDT = dpsValue.getClass().getName();
        }

        List netsimValueList = new ArrayList();
        if (netsimListValue != null) {
            netsimDT = netsimListValue.getClass().getName();
            if (netsimListValue.getClass().getSimpleName().equalsIgnoreCase("string")) {
                netsimValueList = Arrays.asList(((String) netsimListValue).split(","));
            } else {
                netsimValueList = (List) netsimListValue;
            }
        }
        if (dpsValue != null) {
            final List dpsValueList = (List) dpsValue;
            if (dpsValueList != null) {
                for (int i = 0; i < dpsValueList.size(); i++) {
                    Object dpsElement = null;
                    if (dpsValueList.size() > 0) {
                        dpsElement = dpsValueList.get(i);
                    }
                    Object netsimElement = null;
                    if (netsimValueList.size() > 0) {
                        netsimElement = netsimValueList.get(i);
                    }
                    final DataType valueDataType = attributeSpec.getDataTypeSpecification().getValuesDataTypeSpecification().getDataType();
                    if (valueDataType.equals(ENUM_REF)) {
                        final EnumAttributeVerifier attributeVerifier = new EnumAttributeVerifier();
                        attributeVerifier.checkEnumAttributes(response, attributeName, attributeSpec.getDataTypeSpecification()
                                .getValuesDataTypeSpecification().getReferencedDataType(), dpsElement, netsimElement, attributeSpec
                                .getDataTypeSpecification().getDataType().toString());

                    } else if (valueDataType.equals(MO_REF)) {
                        final MoRefAttributeVerifier attributeVerifier = new MoRefAttributeVerifier();
                        attributeVerifier.compareMoRef(response, attributeName, dpsElement, netsimElement, moRef, attributeSpec
                                .getDataTypeSpecification().getDataType().toString()
                                + "->" + valueDataType);

                    } else if (valueDataType.equals(COMPLEX_REF)) {
                        final CdtAttributeVerifier attributeVerifier = new CdtAttributeVerifier();
                        attributeVerifier.checkComplexAttributes(response, attributeName, attributeSpec.getDataTypeSpecification()
                                .getValuesDataTypeSpecification().getReferencedDataType(), dpsElement, netsimElement, netSimData, moRef, "List");

                    } else {
                        final boolean localTestResult = compareTypeAndValue(dpsElement, netsimElement, dpsElement.getClass().getSimpleName());

                        if (localTestResult) {
                            response.addSuccess(new AttributeComparison(attributeName, dpsDT + " -> " + valueDataType, dpsDT, dpsElement,
                                    netsimElement, true));

                        } else {
                            response.addError(new AttributeComparison(attributeName, dpsDT + " -> " + valueDataType, dpsDT, dpsElement,
                                    netsimElement, false));
                        }
                    }

                }
            }
        }

    }
}
