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
package com.ericsson.oss.cm.sync.test.cases;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cm.sync.test.data.ParallelSynchNodeTestData;
import com.ericsson.oss.cm.sync.test.data.SyncNodeServiceTestDataProvider;

public class SyncNodeParallelTestCase extends SyncNodeTestCaseHelper {

    @Inject
    private TestContext context;
    private static Logger LOGGER = Logger.getLogger(SyncNodeParallelTestCase.class);

    @BeforeSuite
    @TestId(id = "TORF-9323_Func_1", title = "TOR_CM_SYNC_Setup: Setup test environment by deploying the test war in SC-1")
    public void beforeSuite() {
        setup();
    }

    @AfterSuite
    @TestId(id = "TORF-9323_Func_2", title = "TOR_CM_CleanUp: Undeploy test war from SC-1")
    public void afterSuite() {
        tearDown();
    }

    /**
     * Adds X number of nodes in preparation for the Sync Node Parallel test case.
     * 
     * @param synchNodePojo
     *            We are adding multiple CPP nodes of different node versions to prepare for sync multiple nodes in parallel test case.
     * @throws Exception
     */
    @DataDriven(name = "parallelSyncNodeDataProvider")
    @Context(context = { Context.REST })
    @Test(groups = { "acceptance" })
    @TestId(id = "TORF-9323_Func_7", title = "TOR_CM_SYNC_Add_Node_Parallel_Sync: Adding Nodes in preparation for Parallel Sync of X number of nodes.")
    public void SuccessfulAddingOfXNodesForSyncInParallel(@Input("ParallelSyncNodes") final ParallelSynchNodeTestData synchNodePojo) throws Exception {
        LOGGER.info("Adding LTE Nodes for Parallel Node Synchronization test case.");
        final int numberOfNodes = synchNodePojo.getNumberOfNodes();
        final SyncNodeServiceTestDataProvider serviceTestDataProvider = new SyncNodeServiceTestDataProvider(secureHttpTool);
        setTestcase("TORF-9312-007", "Adding " + numberOfNodes + " LTE Nodes for  Parallel Node Synchronization test case.");
        setTestStep("Creating " + numberOfNodes + " node(s) in DPS ");

        assertTrue("Failed to add nodes for parallel sync.", serviceTestDataProvider.createNodesInDPSForParallelSync(synchNodePojo.getNetsimNodeMapInclVersion()));
        // Creating a test context, allows you to set data created in this test and get it in another test.
        context.setAttribute(NUMBER_OF_NODES, numberOfNodes);
        context.setAttribute(NODE_NAMES, synchNodePojo.getNodeNames());
    }

    /**
     * Syncs Nodes in parallel, there is no verification done here other than checking the sync status is changed to synchronized.
     * 
     * @param synchNodePojo
     *            We are syncing multiple CPP nodes of different node versions in parallel to verify that we are able to sync multiple nodes in
     *            parallel.
     * @throws Exception
     */
    @Context(context = { Context.REST })
    @Test(groups = { "acceptance" })
    @TestId(id = "TORF-9323_Func_6", title = "TOR_CM_SYNC_Parallel_Sync: Sync Node Parallel Sync of X number of nodes.")
    // The following 2 lines are required if you want to add and sync in the one method, or if the add is complete and
    // you just want to continuously sync.
    // @DataDriven(name = "parallelSyncNodeDataProvider")
    // public void SuccessfulSyncOfNodesInParallel(@Input("ParallelSyncNodes") final ParallelSynchNodeTestData
    // synchNodePojo) throws Exception {
    public void SuccessfulSyncOfNodesInParallel() throws Exception {
        final int numberOfNodes = context.getAttribute(NUMBER_OF_NODES);
        final SyncNodeServiceTestDataProvider serviceTestDataProvider = new SyncNodeServiceTestDataProvider(secureHttpTool);

        setTestcase("TORF-9312-006", "Verification of successful Parallel Node Synchronization of " + numberOfNodes + " LTE Nodes");
        // nodesAddedList = synchNodePojo.getNodeNames();
        nodesAddedList = context.getAttribute(NODE_NAMES);
        setTestStep("Setting all nodes to Unsynchronized state (regardless of their state).");
        // serviceTestDataProvider.setAllNodesToUnsynchronizedState();
        setTestStep("Synchronize node(s) in parallel");
        assertTrue("Failed to successfully sync nodes for parallel sync", serviceTestDataProvider.synchMultipleNodesInParallel(nodesAddedList));
    }
}