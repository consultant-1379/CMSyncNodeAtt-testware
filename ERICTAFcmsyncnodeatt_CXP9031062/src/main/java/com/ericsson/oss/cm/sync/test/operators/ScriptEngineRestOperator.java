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
package com.ericsson.oss.cm.sync.test.operators;

import static java.lang.String.format;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.oss.cm.sync.exception.CmSyncNodeAttException;
import com.ericsson.oss.services.cm.rest.CmEditorRestOperator;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.test.util.common.constants.CommonConstants;

public class ScriptEngineRestOperator implements CommonConstants {

    private static Logger LOGGER = LoggerFactory.getLogger(ScriptEngineRestOperator.class);

    final CmEditorRestOperator cmEditor;

    public ScriptEngineRestOperator(final HttpTool secureHttpTool) {
        cmEditor = new CmEditorRestOperator();
        cmEditor.setTool(secureHttpTool);
    }

    public boolean addNode(final String nodeName, final String nodeIpAddress) {
        LOGGER.debug("Start of createNode for [{}], [{}]", nodeName, nodeIpAddress);
        final Set<String> commands = createAddCommands(nodeName, nodeIpAddress);
        try {
            postCommands(commands);
            // Sleep as automatic creation of MOs are asynchronous.
            Thread.sleep(3 * 1000);
        } catch (final Exception e) {
            //commented out until fix for delete
            //return handleAddNodeException(e, nodeName, nodeIpAddress);
            LOGGER.error("Could not add node", e);
            return false;
        }
        LOGGER.debug("End of createNode for [{}]", nodeName);
        return true;
    }

    /**
     * Sends sync command to cmedit.
     * 
     * Return of 'true' only indicates command was sent successfully, not that the node was synced.
     */
    public boolean syncNode(final String nodeName) {
        LOGGER.debug("Start of synchNode for [{}]", nodeName);
        final String syncAction = "cmedit action NetworkElement=" + nodeName + ",CmFunction=1 sync";

        try {
            cmEditor.executeCommand(syncAction);
        } catch (final Exception e) {
            LOGGER.error("Problem while sending cmedit request to sync node.", e);
            return false;
        }
        LOGGER.debug("End of synchNode for [{}]", nodeName);
        return true;
    }

    /**
     * Sends sync command to cmedit.
     * 
     * Return of 'true' indicates node was synced.
     * 
     * @throws InterruptedException
     */
    public boolean syncNodeWithTimeout(final String nodeName, final int timeOut) {
        try {
            syncNode(nodeName);
            return isNodeSyncedWithTimeout(nodeName, timeOut);
        } catch (final Exception e) {
            LOGGER.error("Failed to sync node", e.getMessage());
            return false;
        }
    }

    public boolean deleteNode(final String nodeName) {
        LOGGER.debug("Start of delete operation on [{}]", nodeName);
        final boolean deleteMecontextSuccess = deleteMoAndChildren("MeContext=" + nodeName);
        final boolean deleteNetworkElementSuccess = deleteMoAndChildren("NetworkElement=" + nodeName);
        return deleteMecontextSuccess && deleteNetworkElementSuccess;
    }

    public boolean isNodeSynced(final String nodeName) {
        LOGGER.debug("Checking if [{}] is synced.", nodeName);
        final String syncStatusValue = getSyncStatusAttributeValueInDpsThroughCmEditor(nodeName);
        return DeltaSyncRestOperator.assertActualWithExpectedAttrValue(syncStatusValue, "SYNCHRONIZED");
    }

    public boolean isNodeSyncedWithTimeout(final String nodeName, int timeOut) throws InterruptedException {

        if (isNodeSynced(nodeName)) {
            return true;
        }
        Thread.sleep(2000);
        timeOut -= 2;
        if (timeOut < 0) {
            LOGGER.error("Synch of node {} has taken longer than specified timeout, presuming failure.", nodeName);
            return false;
        }
        return isNodeSyncedWithTimeout(nodeName, timeOut);
    }

    protected String getSyncStatusAttributeValueInDpsThroughCmEditor(final String nodeName) {
        LOGGER.debug("Checking syncStatus attribute of [{}] in DPS through CmEditor.", nodeName);
        final String moName = "CmFunction";
        final String attrName = "syncStatus";
        final Map<String, String> attributeNamesToValues = getAttrNamesAndValuesOfMoThroughCmEditor(nodeName, moName);

        return attributeNamesToValues.get(attrName);
    }

    protected Map<String, String> getAttrNamesAndValuesOfMoThroughCmEditor(final String nodeName, final String moName) {
        LOGGER.debug("Getting attribute names and values of MO={} in DPS from node={}", moName, nodeName);
        final String command = "cmedit get NetworkElement=" + nodeName + "," + moName + "=1";
        final ResponseDto resp = cmEditor.executeCommandWarningsOnly(command);
        if (isLastErrorCausedByFdnNotExisting()) {
            throw new CmSyncNodeAttException("Cannot get MO's attributes and values as, the supplied FDN NetworkElement=" + nodeName + "," + moName + "=1 does not exist in the database");
        }

        return extractAttributeMapFromResponse(nodeName, resp, moName);
    }

    private void postCommands(final Set<String> commands) {
        for (final String cmd : commands) {
            LOGGER.debug("Sending cmedit command [{}] to {}", cmd, APACHE_JBOSS_INSTANCE);
            cmEditor.executeCommand(cmd);
        }

    }

    private boolean deleteMoAndChildren(final String fdn) {
        try {
            cmEditor.executeCommand("cmedit delete " + fdn + " -ALL");
        } catch (final Exception e) {
            return handleDeleteException(e, fdn);
        }
        return true;
    }

    private boolean handleDeleteException(final Exception e, final String fdn) {
        if (!isLastErrorCausedByFdnNotExisting()) {
            LOGGER.error("Problem while sending cmedit command to delete {}", fdn, e);
            return false;
        } else {
            LOGGER.warn(e.getMessage());
            return true;
        }
    }

    private boolean isLastErrorCausedByFdnNotExisting() {
        final int FDN_DOES_NOT_EXIST_ERROR_CODE = 1001;
        final int errorCode = cmEditor.getErrorCode();
        return errorCode == FDN_DOES_NOT_EXIST_ERROR_CODE;
    }

    private Map<String, String> extractAttributeMapFromResponse(final String nodeName, final ResponseDto resp, final String extractAttrFromMo) {
        final Map<String, Map<String, String>> resultsMap = cmEditor.getAttributesPerFdn(resp);
        return resultsMap.get("NetworkElement=" + nodeName + "," + extractAttrFromMo + "=1");
    }

    private Set<String> createAddCommands(final String nodeName, final String nodeIpAddress) {
        final Set<String> commands = new LinkedHashSet<String>();
        commands.add(format(ADD_ME_CONTEXT, nodeName));
        commands.add(format(ADD_NETWORK_ELEMENT, nodeName));
        commands.add(format(ADD_CPP_CI, nodeName, nodeIpAddress));
        return commands;
    }

}
