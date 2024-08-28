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
package com.ericsson.oss.test.util.reporting.templates;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class MoCompareTable {

    public MoCompareTable(final CompareResponse resp) {
        attributes = new ArrayList<MoCompareTableRow>();
        for (final AttributeComparison comparison : resp.getErrorList()) {
            final String netsimValue = getStringFromObjectValue(comparison, comparison.getModelServiceValue());
            final String dpsValue = getStringFromObjectValue(comparison, comparison.getDpsValue());
            final MoCompareTableRow atr = new MoCompareTableRow("false", comparison.getAttributeName(), comparison.getDpsType(),
                    comparison.getModelServiceType(), dpsValue, netsimValue);
            attributes.add(atr);
        }
        for (final AttributeComparison comparison : resp.getSuccessList()) {
            final String netsimValue = getStringFromObjectValue(comparison, comparison.getModelServiceValue());
            final String dpsValue = getStringFromObjectValue(comparison, comparison.getDpsValue());
            final MoCompareTableRow atr = new MoCompareTableRow("true", comparison.getAttributeName(), comparison.getDpsType(),
                    comparison.getModelServiceType(), dpsValue, netsimValue);
            attributes.add(atr);
        }
    }

    private String getStringFromObjectValue(final AttributeComparison comparison, final Object object) {
        if (object == null) {
            return "null";
        } else if (comparison.getModelServiceType().equals("STRING")) {
            return addQuotesToStringTypes(object);
        } else {
            return object.toString();
        }
    }

    private String addQuotesToStringTypes(final Object row) {
        return ("\"" + row + "\"");
    }

    List<MoCompareTableRow> attributes;
}