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

import java.io.IOException;
import java.util.Date;

import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.test.util.common.dto.NodeInfo;
import com.ericsson.oss.test.util.reporting.DisplayResultsUtility;

public class SyncNodeAttributeVerification extends SyncNodeTestCaseHelper {

    private final DisplayResultsUtility displayResultsUtility = new DisplayResultsUtility();
    private final Host host = DataHandler.getHostByName("Netsim");

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
     * 
     * @param nodeName
     * @param nodeIp
     * @param erbsVersion
     *            We are adding a node to ENM using cmedit. We are sending sync request using cmedit.
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    @DataDriven(name = "sync_data")
    @TestId(id = "TORF-9323_Func_3", title = "TOR_CM_SYNC_AddSync_and_check_syncronized: Add, sync node and verify synced status")
    public void addAndSyncNode(@Input("nodename") final String nodeName, @Input("nodeIp") final String nodeIp, @Input("enmVersion") final String enmVersion) {

        // Add
        nodesAddedList.add(nodeName);
        if (!scriptEngineRestOperator.addNode(nodeName, nodeIp)) {
            log(nodeName, nodeIp, enmVersion, false, false, null);
            fail("Failed To Add Node");
        }

        // Sync
        final Date startSyncTime = new Date();
        final boolean isNodeSynced = scriptEngineRestOperator.syncNodeWithTimeout(nodeName, 45);

        log(nodeName, nodeIp, enmVersion, true, isNodeSynced, startSyncTime);
        assertTrue("Failed To Sync Node", isNodeSynced);
    }

    /**
     * 
     * @param simulation
     * @param nodeName
     * @param erbsVersion
     * @param cppVersion
     * 
     *            We are checking all MO's and attributes that should be persisted in DPS and are available on the node. We are comparing what is on
     *            the node with what is in DPS. We check with Model Service if attribute type is matching. We make sure that attributes which not
     *            suppose to be persisted are not stored in DPS.
     * @throws IOException
     */
    @Test(groups = { "acceptance" })
    @DataDriven(name = "sync_data")
    @TestId(id = "TORF-9323_Func_4", title = "TOR_CM_SYNC_Verify_All_Attributes: Sync Node Attribute verification with DPS, Model service and Netsim complete compare test case")
    public void verifyAllNodeAttributes(@Input("simulation") final String simulation, @Input("nodename") final String nodeName, @Input("enmVersion") final String enmVersion) throws IOException {

        if (!scriptEngineRestOperator.isNodeSynced(nodeName)) {
            throw new SkipException("Attribute verification skipped as node " + nodeName + " was not synced");
        }

        final NodeInfo nodeInfo = new NodeInfo(simulation, nodeName, enmVersion, host);
        final boolean testResult = moVerifier.validateNode(nodeInfo);

        displayResultsUtility.logNodeComparisionResult(nodeName, moVerifier.getPreviousNodeComparisonList());
        log(nodeName, simulation, enmVersion, testResult);
        assertTrue("Attribute comparison failure", testResult);
    }

}