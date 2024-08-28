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
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.LIST;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.MO_REF;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeAttributeSpecification;
import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;
import com.ericsson.oss.test.util.verifier.helper.ModelServiceHelper;

public class CdtAttributeVerifier extends AttributeVerifier {

    public void checkComplexAttributes(final CompareResponse response, final String attributeName, final ModelInfo modelInfo, final Object dpsValue,
                                       Object netsimValue, final Map<String, Object> netSimData, final Map<String, Long> moRefs,
                                       final String parentDataType) {

        if (netsimValue == null) {
            netsimValue = netSimData.get(attributeName.toLowerCase());
        }

        if (dpsValue != null && netSimData != null) {
            final Map dbsMap = new TreeMap((Map) dpsValue);
            final Map netsimMap = (Map) netsimValue;
            final Collection<ComplexDataTypeAttributeSpecification> complexTypeAttributes = getModelServiceHelper().getComplexTypeAttributes(
                    modelInfo);

            for (final ComplexDataTypeAttributeSpecification cmpx : complexTypeAttributes) {

                final DataType cmpxDataType = cmpx.getDataTypeSpecification().getDataType();
                final String modelServiceDT = cmpxDataType.toString();
                // TODO CHECK DATA TYPE
                final Object dpsAttributeValue = dbsMap.get(cmpx.getName());
                Object netsimAttributeValue = null;
                if (netsimMap != null) {
                    netsimAttributeValue = netsimMap.get(cmpx.getName().toLowerCase());
                }
                final String displayMessage = getCDTDisplayMessage(attributeName, parentDataType, cmpx);
                if (cmpxDataType.equals(ENUM_REF)) {
                    final EnumAttributeVerifier attributeVerifier = new EnumAttributeVerifier();
                    attributeVerifier.checkEnumAttributes(response, displayMessage + ">>" + cmpx.getName(), cmpx.getDataTypeSpecification()
                            .getReferencedDataType(), dpsAttributeValue, netsimAttributeValue, cmpxDataType.toString());

                } else if (cmpxDataType.equals(LIST)) {
                    final ListAttributeVerifier attributeVerifier = new ListAttributeVerifier();
                    attributeVerifier.compareListDataTypeValue(response, cmpx, dpsAttributeValue, netsimMap, moRefs);

                } else if (cmpxDataType.equals(MO_REF)) {
                    final MoRefAttributeVerifier attributeVerifier = new MoRefAttributeVerifier();
                    attributeVerifier.compareMoRef(response, displayMessage + ">>" + cmpx.getName(), dpsAttributeValue, netsimAttributeValue, moRefs,
                            cmpxDataType.toString() + "->" + modelServiceDT);

                } else if (cmpxDataType.equals(COMPLEX_REF)) {
                    checkComplexAttributes(response, displayMessage + ">>" + cmpx.getName(), cmpx.getDataTypeSpecification().getReferencedDataType(),
                            dpsAttributeValue, netsimAttributeValue, netSimData, moRefs, "List");

                } else {

                    String dpsDT = "";
                    if (dpsAttributeValue != null) {
                        dpsDT = dpsAttributeValue.getClass().getSimpleName();
                    }

                    final boolean testResult = compareTypeAndValue(dpsAttributeValue, netsimAttributeValue, dpsDT);
                    final boolean checkDataType = modelServiceDT.equalsIgnoreCase(dpsDT);

                    if (testResult && checkDataType) {
                        response.addSuccess(new AttributeComparison(displayMessage, dpsDT, modelServiceDT, dpsAttributeValue, netsimAttributeValue,
                                true));
                    } else {
                        response.addError(new AttributeComparison(displayMessage, dpsDT, modelServiceDT, dpsAttributeValue, netsimAttributeValue,
                                false));
                    }
                }
            }
        } else {
            response.addError(new AttributeComparison(attributeName + " is null in dps and netsim", null, "CDT", null, null, false));
        }

    }

    private String getCDTDisplayMessage(final String attributeName, final String parentDataType, final ComplexDataTypeAttributeSpecification cmpx) {
        String displayMessage = "";
        if (parentDataType != null && parentDataType.length() > 0) {
            displayMessage = String.format("<span class=\"blue\"> %s ->CDT </span> %s<span class=\"blue\"> » </span> %s", parentDataType,
                    attributeName, cmpx.getName());
        } else {
            displayMessage = String.format("<span class=\"blue\"> CDT </span> %s<span class=\"blue\"> » </span> %s", attributeName, cmpx.getName());
        }
        return displayMessage;
    }

    private ModelServiceHelper getModelServiceHelper() {
        return ModelServiceHelper.getInstance();
    }
}
