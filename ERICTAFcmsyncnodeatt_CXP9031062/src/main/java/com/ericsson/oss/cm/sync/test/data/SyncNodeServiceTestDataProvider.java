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
package com.ericsson.oss.cm.sync.test.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestData;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.oss.cm.sync.test.operators.ParallelSyncNodeOperator;
import com.ericsson.oss.cm.sync.test.operators.ScriptEngineRestOperator;
import com.ericsson.oss.test.util.common.constants.CommonConstants;
import com.ericsson.oss.test.util.netsim.NetsimConfiguratorUtil;

public class SyncNodeServiceTestDataProvider implements TestData, CommonConstants {

    private final HttpTool secureHttpTool;
    int addNodeSuccessCount = 0;

    public SyncNodeServiceTestDataProvider(final HttpTool secureHttpTool) {
        this.secureHttpTool = secureHttpTool;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncNodeServiceTestDataProvider.class);

    public Boolean createNodesInDPSForParallelSync(final Map<String, Map<String, NetworkElement>> netsimNodeMapInclVersion) {
        Boolean createdWithoutError = false;
        try {
            for (final Entry<String, Map<String, NetworkElement>> entry : netsimNodeMapInclVersion.entrySet()) {
                createMultipleNodesInDPS(entry.getValue(), entry.getKey());
            }
            createdWithoutError = true;
        } catch (final Exception e) {
            LOGGER.error("Couldn't Create Node .... ", e);
            createdWithoutError = false;
        }
        return createdWithoutError;
    }

    public void createMultipleNodesInDPS(final Map<String, NetworkElement> netsimNodes, final String version) throws Exception {
        final ScriptEngineRestOperator scriptEngineRestOperator = new ScriptEngineRestOperator(secureHttpTool);
        final int numberOfNodes = netsimNodes.size();
        LOGGER.debug("Called createMultipleNodesInDPS for [{}] number of nodes. Node version [{}]", numberOfNodes, version);

        boolean addNodeSuccess = false;
        for (final Map.Entry<String, NetworkElement> netsimNodeName : netsimNodes.entrySet()) {
            final NetworkElement ne = netsimNodeName.getValue();
            final String nodeName = ne.getName();
            final String ipAddress = ne.getIp();

            LOGGER.debug("Creating [{}] with address [{}] in DPS", nodeName, ipAddress);
            addNodeSuccess = scriptEngineRestOperator.addNode(nodeName, ipAddress);
            if (addNodeSuccess) {
                addNodeSuccessCount++;
                LOGGER.info("Sucessfully added [{}] node(s)", addNodeSuccessCount);
            } else {
                throw new Exception("Add failed on [" + nodeName + "], ip = [" + ipAddress + "] for version = [" + version + "]");
            }
        }
    }

    public boolean synchMultipleNodesInParallel(final List<String> synchFdnList) throws Exception {
        LOGGER.debug("Called synchMultipleNodesInParallel for [{}] number of nodes", synchFdnList.size());
        final ParallelSyncNodeOperator monitor = new ParallelSyncNodeOperator(synchFdnList, secureHttpTool);
        return monitor.startSynchNodeProcess();
    }

    public static ParallelSynchNodeTestData readTopologyForMultipleNodesFromNetsim(final Map<String, String> simulationMap, final int reqdNoOfNodes) {
        final Map<String, Map<String, NetworkElement>> netsimNodeMapInclVersion = new HashMap<String, Map<String, NetworkElement>>();
        final List<String> simulations = new ArrayList<String>(simulationMap.keySet());
        Map<String, NetworkElement> netsimNodeMap;
        int overallNeCount = 0;

        for (final String simulation : simulations) {
            final String version = simulationMap.get(simulation);
            netsimNodeMap = new HashMap<String, NetworkElement>();
            //We are using getNumberOfNEsFromSimulationAndStart method to start selected NEs instead of getStartedNEsFromSimulation
            // method which makes call for each node to check if node is started. This is for performance reasons. 
            for (final NetworkElement netsimNE : NetsimConfiguratorUtil.getNumberOfNEsFromSimulationAndStart(simulation, reqdNoOfNodes)) {
                final String nodeName = netsimNE.getName();
                final String nodeIP = netsimNE.getIp();
                netsimNodeMap.put(nodeName, netsimNE);
                LOGGER.info("Node under test = [{}], version = [{}], IP = [{}], simulation = [{}]", nodeName, version, nodeIP, netsimNE.getSimulationName());
                overallNeCount++;
            }

            if (netsimNodeMapInclVersion.containsKey(version)) {
                netsimNodeMapInclVersion.get(version).putAll(netsimNodeMap);
            } else {
                netsimNodeMapInclVersion.put(version, netsimNodeMap);
            }
        }

        if (overallNeCount != 0) {
            final ParallelSynchNodeTestData syn = new ParallelSynchNodeTestData(netsimNodeMapInclVersion);
            syn.setNumberOfNodes(overallNeCount);
            return syn;
        } else {
            LOGGER.error("No Active Netsim Nodes could be found please check your Netsim Simulations");
            return null;
        }

    }

    public void deleteNode(final String nodeName) {
        final ScriptEngineRestOperator scriptEngineRestOperator = new ScriptEngineRestOperator(secureHttpTool);
        scriptEngineRestOperator.deleteNode(nodeName);
    }
}