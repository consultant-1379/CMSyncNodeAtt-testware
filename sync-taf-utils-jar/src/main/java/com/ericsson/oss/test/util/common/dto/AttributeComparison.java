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
package com.ericsson.oss.test.util.common.dto;

public class AttributeComparison {

    private String attributeName;
    private String dpsType;
    private String modelServiceType;
    private Object dpsValue;
    private Object modelServiceValue;
    private boolean isMatch;

    public AttributeComparison(String attributeName, String dpsType, String modelServiceType, Object dpsValue, Object modelServiceValue, boolean isMatch) {
        this.attributeName = attributeName;
        this.dpsType = dpsType;
        this.modelServiceType = modelServiceType;
        this.dpsValue = dpsValue;
        this.modelServiceValue = modelServiceValue;
        this.isMatch = isMatch;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getDpsType() {
        return dpsType;
    }

    public void setDpsType(String dpsType) {
        this.dpsType = dpsType;
    }

    public String getModelServiceType() {
        return modelServiceType;
    }

    public void setModelServiceType(String modelServiceType) {
        this.modelServiceType = modelServiceType;
    }

    public Object getDpsValue() {
        return dpsValue;
    }

    public void setDpsValue(Object dpsValue) {
        this.dpsValue = dpsValue;
    }

    public Object getModelServiceValue() {
        return modelServiceValue;
    }

    public void setModelServiceValue(Object modelServiceValue) {
        this.modelServiceValue = modelServiceValue;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setIsMatch(boolean isMatch) {
        this.isMatch = isMatch;
    }
}
