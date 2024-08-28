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

public class MoCompareTableRow {
    public MoCompareTableRow(final String isSuccess, final String name, final String dpsType, final String modelServiceType, final String dpsValue, final String netsimValue) {
        this.isSuccess = isSuccess;
        this.name = name;
        this.dpsType = dpsType;
        this.modelServiceType = modelServiceType;
        this.dpsValue = dpsValue;
        this.netsimValue = netsimValue;
    }

    String isSuccess, name, dpsType, modelServiceType, dpsValue, netsimValue;
}