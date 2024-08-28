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

import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class NodeCompareTable {

    public NodeCompareTable(final Iterable<CompareResponse> successList, final Iterable<CompareResponse> failureList) {
        comparisons = new ArrayList<NodeCompareTableRow>();
        for (final CompareResponse resp : failureList) {
            final NodeCompareTableRow resultCompare = new NodeCompareTableRow(resp, resp.getId());
            comparisons.add(resultCompare);
        }
        for (final CompareResponse resp : successList) {
            final NodeCompareTableRow resultCompare = new NodeCompareTableRow(resp, resp.getId());
            comparisons.add(resultCompare);
        }
    }

    List<NodeCompareTableRow> comparisons;
}