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

import java.util.*;

public class CompareResponse {

    private String modelName;
    private String nodeName;
    private String fdn;
    private String version;
    private String namespace;
    private final UUID uuid = UUID.randomUUID();
    private int modelAttributesFound;
    private int dpsAttributesFound;
    private int netsimAttributesFound;
    private int nonSyncableAttributesFound;

    private final List<AttributeComparison> errorList = new ArrayList<AttributeComparison>();
    private final List<AttributeComparison> successList = new ArrayList<AttributeComparison>();
    private String errorMessage;

    public String getAttributeStats() {
        return String.format("ModelService=%d,PM ModelService=%d, DPS=%d and Netsim=%d <br>", modelAttributesFound, nonSyncableAttributesFound,
                dpsAttributesFound, netsimAttributesFound);
    }

    public String getErrorMsg() {
        String result = "Error(s): <br>";
        if (errorMessage != null) {
            result += errorMessage;
        }
        if (!isTotalAttributesFromProvidersEqual()) {
            result += String.format("- Total Attributes found mismatch ModelService=%d,PM ModelService=%d, DPS=%d and Netsim=%d <br>",
                    modelAttributesFound, nonSyncableAttributesFound, dpsAttributesFound, netsimAttributesFound);
        }
        if (!errorList.isEmpty()) {
            result += "Attribute(s) type/value mistmatch (Expand to view, highlighted errors)";
        }
        return result;
    }

    public String getResponseMessage() {
        if (isSuccess()) {
            return "Success";
        } else {
            return "Error: " + getShortErrorMessage();
        }
    }

    private String getShortErrorMessage() {
        final String result = errorMessage;
        if (!isTotalAttributesFromProvidersEqual()) {
            return "Total Attributes found mismatch";
        }
        if (!errorList.isEmpty()) {
            return "Attribute(s) type/value mistmatch";
        }
        return result;
    }

    public boolean isSuccess() {
        return (errorMessage == null && errorList.isEmpty() && isTotalAttributesFromProvidersEqual());
    }

    public boolean isTotalAttributesFromProvidersEqual() {
        return ((modelAttributesFound == netsimAttributesFound) && (modelAttributesFound == dpsAttributesFound));
    }

    public int getModelAttributesFound() {
        return modelAttributesFound;
    }

    public void setModelAttributesFound(final int modelAttributesFound) {
        this.modelAttributesFound = modelAttributesFound;
    }

    public int getDpsAttributesFound() {
        return dpsAttributesFound;
    }

    public void setDpsAttributesFound(final int dpsAttributesFound) {
        this.dpsAttributesFound = dpsAttributesFound;
    }

    public int getNetsimAttributesFound() {
        return netsimAttributesFound;
    }

    public void setNetsimAttributesFound(final int netsimAttributesFound) {
        this.netsimAttributesFound = netsimAttributesFound;
    }

    public void addError(final AttributeComparison error) {
        errorList.add(error);
    }

    public void addSuccess(final AttributeComparison success) {
        successList.add(success);
    }

    public int getTotalErrors() {
        return errorList.size();
    }

    public int getTotalSuccesses() {
        return successList.size();
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }

    public List<AttributeComparison> getErrorList() {
        return errorList;
    }

    public List<AttributeComparison> getSuccessList() {
        return successList;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getNotSyncableAttributesFound() {
        return nonSyncableAttributesFound;
    }

    public void setNonSyncableAttributesFound(final int nonSyncableAttributesFound) {
        this.nonSyncableAttributesFound = nonSyncableAttributesFound;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getFdn() {
        return fdn;
    }

    public void setFdn(final String fdn) {
        this.fdn = fdn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return uuid.toString().replace("-", "");
    }

}
