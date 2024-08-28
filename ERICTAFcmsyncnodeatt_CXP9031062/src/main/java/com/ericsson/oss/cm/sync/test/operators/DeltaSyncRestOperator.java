/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.cm.sync.test.operators;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;

import com.ericsson.oss.test.util.common.constants.CommonConstants;
import com.ericsson.oss.test.util.common.constants.DeltaSyncConstants;

public class DeltaSyncRestOperator implements CommonConstants, DeltaSyncConstants {

    private static Logger LOGGER = LoggerFactory.getLogger(DeltaSyncRestOperator.class);

    private ScriptEngineRestOperator scriptEngineRestOperator;

    public DeltaSyncRestOperator(final ScriptEngineRestOperator scriptEngineRestOperator) {
        this.scriptEngineRestOperator = scriptEngineRestOperator;
    }

    public void syncNodeAndVerifySynced(final String nodeName, final String testName) {
        LOGGER.info("#####@@@@@ Starting {} test @@@@@#####", testName);
        final boolean isNodeSynced = scriptEngineRestOperator.syncNodeWithTimeout(nodeName, 45);
        if (!isNodeSynced) {
            throw new SkipException("Test skipped as node " + nodeName + " was not synced");
        }
    }

    public boolean verifyFullSyncIsCalled(final String nodeName, int timeOut) {
        try {
            if (verifyFullSyncIsCalled(nodeName)) {
                return true;
            }
            Thread.sleep(1000);
            timeOut -= 1;
            if (timeOut < 0) {
                LOGGER.error("Verifying of full sync has timed out, presuming full sync was not invoked.");
                return false;
            }
            return verifyFullSyncIsCalled(nodeName, timeOut);
        } catch (final InterruptedException e) {
            LOGGER.error("Failed to verify if full sync was called.", e.getMessage());
            return false;
        }
    }

    private boolean verifyFullSyncIsCalled(final String nodeName) {
        LOGGER.debug("Checking if full sync was called for [{}]", nodeName);
        final String syncStatusValue = scriptEngineRestOperator
                .getSyncStatusAttributeValueInDpsThroughCmEditor(nodeName);
        return assertActualWithExpectedAttrValue(syncStatusValue, "TOPOLOGY", "ATTRIBUTE");
    }

    public int getGenerationCounterFromDps(final String nodeName) {
        LOGGER.debug("Getting generation counter of [{}] from DPS", nodeName);
        final Map<String, String> attributeNamesToValues = scriptEngineRestOperator
                .getAttrNamesAndValuesOfMoThroughCmEditor(nodeName, CPP_CONN_INFO_MO_NAME);
        final int generationCounter = Integer.parseInt(attributeNamesToValues.get(GEN_COUNTER_ATTR_NAME));
        LOGGER.info("Generation counter of {} is {}", nodeName, generationCounter);
        return generationCounter;
    }

    public void setGenerationCounterInDpsToZero(final String nodeName) {
        setAttributeInDps(nodeName, CPP_CONN_INFO_MO_NAME, GEN_COUNTER_ATTR_NAME, "0");
    }

    public void turnSupervisionOff(final String nodeName) {
        setAttributeInDps(nodeName, CM_NODE_HB_SUPERVISION_MO_NAME, ACTIVE_ATTR_NAME, "false");
        getSupervisionStatusfromDps(nodeName);
    }

    public void turnSupervisionOn(final String nodeName) {
        setAttributeInDps(nodeName, CM_NODE_HB_SUPERVISION_MO_NAME, ACTIVE_ATTR_NAME, "true");
        getSupervisionStatusfromDps(nodeName);
    }

    public void turnSupervisionOn(final List<String> nodeNames) {
        for (final String nodeName : nodeNames) {
            turnSupervisionOn(nodeName);
        }
    }

    public void turnSupervisionOff(final List<String> nodeNames) {
        for (final String nodeName : nodeNames) {
            turnSupervisionOff(nodeName);
        }
    }

    public void setSyncStatusToUnsynchronized(final String nodeName) {
        setAttributeInDps(nodeName, "CmFunction", "syncStatus", "UNSYNCHRONIZED");
    }

    protected static boolean assertActualWithExpectedAttrValue(final String actualAttrValue,
                                                               final String... expectedAttrValue) {
        for (final String attrValue : expectedAttrValue) {
            if (actualAttrValue.equals(attrValue)) {
                return true;
            }
        }
        return false;
    }

    private void setAttributeInDps(final String nodeName, final String moName, final String attrName,
                                   final String attrValue) {
        LOGGER.debug("Start of setAttributeInDps for node [{}], setting attribute [{}] to [{}]", nodeName, attrName,
                attrValue);
        final String setCmd = "cmedit set NetworkElement=" + nodeName + "," + moName + "=1 " + attrName + "="
                + attrValue;
        scriptEngineRestOperator.cmEditor.executeCommand(setCmd);
        LOGGER.debug("End of setAttributeInDps for node [{}], setting attribute [{}] to [{}]", nodeName, attrName,
                attrValue);
    }

    private void getSupervisionStatusfromDps(final String nodeName) {
        LOGGER.debug("Getting Supervision status of [{}] from DPS", nodeName);
        final Map<String, String> attributeNamesToValues = scriptEngineRestOperator
                .getAttrNamesAndValuesOfMoThroughCmEditor(nodeName, CM_NODE_HB_SUPERVISION_MO_NAME);
        final String supervisionStatus = attributeNamesToValues.get(ACTIVE_ATTR_NAME);
        LOGGER.info("Supervision status of [{}] is {}", nodeName, supervisionStatus);
    }

}
