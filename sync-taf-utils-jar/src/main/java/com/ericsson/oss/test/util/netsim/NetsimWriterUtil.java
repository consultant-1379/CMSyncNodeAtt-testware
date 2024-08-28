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
package com.ericsson.oss.test.util.netsim;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestData;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimSession;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.oss.test.util.common.constants.DeltaSyncConstants;
import com.ericsson.oss.test.util.common.dto.NodeInfo;

public class NetsimWriterUtil implements TestData, DeltaSyncConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetsimWriterUtil.class);

    private final String CREATE_MO_SHELL_COMMAND = "createmo:parentid=\"" + "%s\"" + ",type=\"" + "%s\"" + ",name=\"" + "%s\";";
    private final String SET_MO_ATTRIBUTE_SHELL_COMMAND = "setmoattribute:mo=\"%s\", attributes=\"%s=%s\";";
    //    private final String setEnumAttribute = "setmoattribute:mo=\"%s\", attributes=\"%s=%d\";";
    private final String DELETE_MO_SHELL_COMMAND = "deletemo:moid=\"%s\";";

    private NetSimSession netSimSession;
    private String simulation;
    private String nodeName;

    public NetsimWriterUtil(final NodeInfo nodeInfo) {
        this.simulation = nodeInfo.getSimulation();
        this.nodeName = nodeInfo.getNodeName();
        netSimSession = NetSimCommandHandler.getSession(nodeInfo.getHost());
    }

    public void createAndUpdateSctpMoOnNode() {
        createMoOnNode(SCTP, RIVENDELL_MO_NAME);
        updateAttrOnNode(SCTP, RIVENDELL_MO_NAME, USER_LABEL);
    }

    public void createMoAndUpdateStructOnNode() {
        createMoOnNode(EUTRANCELLFDD, RIVENDELL_MO_NAME);
        updateAttrOnNode(EUTRANCELLFDD, RIVENDELL_MO_NAME, EUTRAN_CELL_POLYGON_ATTRIBUTE_NAME);
    }

    public void createMoAndUpdateSeqOfMoRefsOnNode() {
        createMoOnNode(EUTRANCELLFDD, RIVENDELL_MO_NAME);
        updateAttrOnNode(EUTRANCELLFDD, RIVENDELL_MO_NAME, SECTOR_CARR_REF_ATTRIBUTE_NAME);
    }

    public void deleteCreatedEutranCellFddFromNode() {
        deleteMoOnNode(EUTRANCELLFDD, RIVENDELL_MO_NAME);
    }

    public void deleteCreatedSctpMoFromNode() {
        deleteMoOnNode(SCTP, RIVENDELL_MO_NAME);
    }

    public void cleanUpNode() {
        deleteCreatedEutranCellFddFromNode();
        deleteCreatedSctpMoFromNode();
    }

    private void createMoOnNode(final String moType, final String moName) {
        LOGGER.debug("Creating MO on node");
        final String parentFdn = getParentFdn(moType);
        final String createMoCommand = String.format(CREATE_MO_SHELL_COMMAND, parentFdn, moType, moName);
        executeCommandOnNode(createMoCommand);
    }

    private void updateAttrOnNode(final String moType, final String moName, final String attrName) {
        final String moToUpdate = getMoFdn(moType, moName);
        final String attrValue = getAttributeValue(attrName);
        final String updateAttrCommand = String.format(SET_MO_ATTRIBUTE_SHELL_COMMAND, moToUpdate, attrName, attrValue);
        executeCommandOnNode(updateAttrCommand);
    }

    private void deleteMoOnNode(final String moType, final String moName) {
        LOGGER.debug("Deleting MO on node");
        final String moToDelete = getMoFdn(moType, moName);
        final String deleteMoCommand = String.format(DELETE_MO_SHELL_COMMAND, moToDelete);
        executeCommandOnNode(deleteMoCommand);
    }

    public void closeNetsimSession() {
        netSimSession.close();
    }

    private void executeCommandOnNode(final String command) {
        final NetSimResult commandResult = netSimSession.exec(NetSimCommands.open(simulation), NetSimCommands.selectnocallback(nodeName + "\n" + command));
        final String rawOutput = commandResult.getRawOutput();
        LOGGER.info("RawOutput of command on node: {}", rawOutput);
    }

    private String getMoFdn(final String moType, final String moName) {
        final String parentFdn = getParentFdn(moType);
        return parentFdn + "," + moType + "=" + moName;
    }

    private String getParentFdn(final String moType) {
        LOGGER.debug("Getting the parent of {}", moType);
        final Map<String, String> childParentMapping = new HashMap<String, String>();
        childParentMapping.put(EUTRANCELLFDD, ENODEB_FUNCTION_FDN);
        childParentMapping.put(SCTP, TRANSPORT_NETWORK_FDN);

        final String parent = childParentMapping.get(moType);
        LOGGER.debug("Parent of {} is {}", moType, parent);
        return parent;
    }

    private String getAttributeValue(final String attrName) {
        final Map<String, String> attrNameToValue = new HashMap<String, String>();
        attrNameToValue.put(USER_LABEL, GANDALF);
        attrNameToValue.put(SECTOR_CARR_REF_ATTRIBUTE_NAME, SECTOR_CARR_REF_ATTRIBUTE_VALUE);
        attrNameToValue.put(EUTRAN_CELL_POLYGON_ATTRIBUTE_NAME, EUTRAN_CELL_POLYGON_ATTRIBUTE_VALUE);
        return attrNameToValue.get(attrName);
    }

}