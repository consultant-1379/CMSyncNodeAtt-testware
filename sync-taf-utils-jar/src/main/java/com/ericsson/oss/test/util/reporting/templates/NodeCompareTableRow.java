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

public class NodeCompareTableRow {
    NodeCompareTableRow(final CompareResponse resp, final String pageID) {
        isSuccess = Boolean.toString(resp.isSuccess());
        link = "compare/" + pageID + ".html";
        fdn = resp.getFdn();
        modelAttributes = resp.getModelAttributesFound();
        if (resp.getDpsAttributesFound() == -1) {
            dpsAttributes = -1;
        } else {
            dpsAttributes = resp.getDpsAttributesFound() - resp.getNotSyncableAttributesFound();
        }
        netsimAttributes = resp.getNetsimAttributesFound();
        pmAttributes = resp.getNotSyncableAttributesFound();
        result = resp.getResponseMessage();
    }

    String isSuccess, link, fdn, result;
    int modelAttributes, dpsAttributes, netsimAttributes, pmAttributes;
}