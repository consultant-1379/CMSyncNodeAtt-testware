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

import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class TotalAttributesTable {

    //Constructor For Node Comparison
    public TotalAttributesTable(final int[] totals) {
        modelCount = totals[0];
        pmCount = totals[1];
        netsimCount = totals[2];
        dpsCount = totals[3] - totals[1];

        final int MANAGEDELEMENT_MO_EXTRA_ATTRIBUES = 4;
        //MO ManagedElement=1  has 4 OSS attributes added to it which are not included in the model/dps
        isEqual = ((modelCount == (dpsCount + pmCount - MANAGEDELEMENT_MO_EXTRA_ATTRIBUES)) && (modelCount == netsimCount));
    }

    //Constructor For Model/MO Comparison
    public TotalAttributesTable(final CompareResponse resp) {
        modelCount = resp.getModelAttributesFound();
        pmCount = resp.getNotSyncableAttributesFound();
        netsimCount = resp.getNetsimAttributesFound();
        dpsCount = resp.getDpsAttributesFound() - resp.getNotSyncableAttributesFound();

        isEqual = ((modelCount == (dpsCount + pmCount)) && (modelCount == netsimCount));
    }

    int modelCount, pmCount, netsimCount, dpsCount;
    boolean isEqual;
}