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

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeAttributeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class AttributeVerifierInfo {

    final Object netsimValue;
    final Object dpsValue;
    final ModelInfo modelInfo;
    final PrimaryTypeAttributeSpecification attributeSpec;
    final Map<String, Long> moRefs;
    final Map<String, Object> netSimData;
    final CompareResponse response;

    public AttributeVerifierInfo(final Object netsimValue, final Object dpsValue, final ModelInfo modelInfo,
                                 final PrimaryTypeAttributeSpecification attributeSpec, final Map<String, Long> moRefs,
                                 final Map<String, Object> netSimData, final CompareResponse response) {
        this.netsimValue = netsimValue;
        this.dpsValue = dpsValue;
        this.modelInfo = modelInfo;
        this.attributeSpec = attributeSpec;
        this.moRefs = moRefs;
        this.response = response;
        this.netSimData = netSimData;
    }

    public Map<String, Object> getNetSimData() {
        return netSimData;
    }

    public String getAttributeName() {
        return attributeSpec.getName();
    }

    public Object getNetsimValue() {
        return netsimValue;
    }

    public Object getDpsValue() {
        return dpsValue;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public PrimaryTypeAttributeSpecification getAttributeSpec() {
        return attributeSpec;
    }

    public Map<String, Long> getMoRefs() {
        return moRefs;
    }

    public CompareResponse getResponse() {
        return response;
    }

    public DataType getDataType() {
        return attributeSpec.getDataTypeSpecification().getDataType();
    }

}
