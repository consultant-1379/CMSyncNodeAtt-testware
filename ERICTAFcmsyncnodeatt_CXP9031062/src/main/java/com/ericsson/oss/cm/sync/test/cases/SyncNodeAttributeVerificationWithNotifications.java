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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.KertayleCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.nms.host.HostConfigurator;
import com.ericsson.oss.netsim.operator.cm.NetSimCmOperatorImpl;
import com.ericsson.oss.test.util.common.constants.CommonConstants;
import com.ericsson.oss.test.util.common.dto.NodeInfo;
import com.ericsson.oss.test.util.reporting.DisplayResultsUtility;

public class SyncNodeAttributeVerificationWithNotifications extends SyncNodeTestCaseHelper implements TestCase,
        CommonConstants {

    private static final Logger logger = LoggerFactory.getLogger(SyncNodeAttributeVerificationWithNotifications.class);

    private final DisplayResultsUtility displayResultsUtility = new DisplayResultsUtility();
    private final Host host = DataHandler.getHostByName("Netsim");

    @Inject
    private NetSimCmOperatorImpl netSimCmOperatorImpl;

    private long sleepAfterNofications = 35000;
    private long sleepForKeratyleExec = 4000;

    @BeforeSuite
    @TestId(id = "TORF-31493_Func_1", title = "TOR_CM_SYCN_Setup: Setup test environment by deploying the test war in SC-1")
    public void beforeSuite() {
        setup();
    }

    @AfterSuite
    @TestId(id = "TORF-31493_Func_2", title = "TOR_CM_CleanUp: Undeploy test war from SC-1")
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
    @DataDriven(name = "trigger_notifications")
    @TestId(id = "TORF-31493_Func_3", title = "TOR_CM_SYCN_AddSync_and_check_syncronized: Add test ERBS to DPS, sync node and verify synced status")
    public void addAndSyncNode(@Input("nodename") final String nodeName, @Input("nodeIp") final String nodeIp,
                               @Input("enmVersion") final String enmVersion) throws Exception {

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
     * @param nodeName
     * @param nodeIp
     * @param erbsVersion
     *            We are adding a node to ENM using cmedit. We are sending sync request using cmedit.
     * @throws Exception
     */
    @Test(groups = { "acceptance" })
    @DataDriven(name = "trigger_notifications")
    @TestId(id = "TORF-31493_Func_4", title = "TOR_CM_SYCN_trigger_notifications: create a test MO and change its attributes to trigger notifications")
    public void triggerCreateAndUpdateNotifications(@Input("simulation") final String sim,
                                                    @Input("nodename") final String nodeName,
                                                    @Input("createKer") final String createKer,
                                                    @Input("updateKer") final String updateKer) throws Exception {

        logger.info("trigger notifications, sim: " + sim + " name: " + nodeName + " update ker: '" + updateKer + "'");

        final String createKerFilePath = getKerFilePathFromResourcesDir(createKer);
        final String updateKerFilePath = getKerFilePathFromResourcesDir(updateKer);

        final Host host = HostConfigurator.getNetsim();
        logger.debug("netsim: {}  ip: {}", host.getHostname(), host.getOriginalIp());

        final NetworkElement ne = netSimCmOperatorImpl.getNetworkElement("MeContext=" + nodeName);
        assertTrue("sim name: '" + sim + "', node name: '" + nodeName + "', node should be started", ne.isStarted());

        logger.debug("creating the test MO");
        NetSimResult result = executeKertayleFile(createKerFilePath, host, ne);
        assertGoodNetsimResult(result);
        Thread.sleep(sleepAfterNofications);

        logger.info("modifying attributes on the test MO file: " + updateKerFilePath);
        result = executeKertayleFile(updateKerFilePath, host, ne);
        assertGoodNetsimResult(result);
        Thread.sleep(sleepAfterNofications);

        logger.debug("notifications triggered!");
    }

    @Test(groups = { "acceptance" })
    @DataDriven(name = "trigger_notifications")
    @TestId(id = "TORF-31493_Func_5", title = "TOR_CM_SYCN_Verify_All_Attributes_After_Notifications: After notifications Sync Node Attribute verification with DPS, Model service and Netsim complete compare test case")
    public void verifyAllNodeAttributesAfterUpdateNotifications(@Input("simulation") final String simulation,
                                                                @Input("nodename") final String nodeName,
                                                                @Input("enmVersion") final String enmVersion
            ) throws IOException {

        if (!scriptEngineRestOperator.isNodeSynced(nodeName)) {
            throw new SkipException("Attribute verification skipped as node " + nodeName + " was not synced");
        }

        final NodeInfo nodeInfo = new NodeInfo(simulation, nodeName, enmVersion, host);
        final boolean testResult = moVerifier.validateNode(nodeInfo);

        displayResultsUtility.logNodeComparisionResult(nodeName, moVerifier.getPreviousNodeComparisonList());
        log(nodeName, simulation, enmVersion, testResult);
        assertTrue("Attribute comparison failure", testResult);
    }

    @Test(groups = { "acceptance" })
    @DataDriven(name = "trigger_notifications")
    @TestId(id = "TORF-31493_Func_6", title = "TOR_CM_SYCN_Verify_All_Attributes_After_Notifications: After notifications Sync Node Attribute verification with DPS, Model service and Netsim complete compare test case")
    public void triggerDeleteNotification(@Input("simulation") final String sim,
                                          @Input("nodename") final String nodeName,
                                          @Input("enmVersion") final String enmVersion,
                                          @Input("deleteKer") final String deleteKer
            ) throws IOException, InterruptedException {
        logger.info("trigger delete notifications, sim: " + sim + " name: " + nodeName + " delete ker: '" + deleteKer
                + "'");

        final String deleteKerFilePath = getKerFilePathFromResourcesDir(deleteKer);

        final Host host = HostConfigurator.getNetsim();
        logger.debug("netsim: {}  ip: {}", host.getHostname(), host.getOriginalIp());

        final NetworkElement ne = netSimCmOperatorImpl.getNetworkElement("MeContext=" + nodeName);
        assertTrue("sim name: '" + sim + "', node name: '" + nodeName + "', node should be started", ne.isStarted());

        logger.debug("deleting the test MO");
        final NetSimResult result = executeKertayleFile(deleteKerFilePath, host, ne);
        assertGoodNetsimResult(result);
        Thread.sleep(sleepAfterNofications);

        logger.debug("delete notification triggered!");
    }

    @Test(groups = { "acceptance" })
    @DataDriven(name = "trigger_notifications")
    @TestId(id = "TORF-31493_Func_7", title = "TOR_CM_SYCN_Verify_All_Attributes_After_Notifications: After notifications Sync Node Attribute verification with DPS, Model service and Netsim complete compare test case")
    public void verifyAllNodeAttributesAfterDeleteNotification(@Input("simulation") final String simulation,
                                                               @Input("nodename") final String nodeName,
                                                               @Input("enmVersion") final String enmVersion
            ) throws IOException {

        if (!scriptEngineRestOperator.isNodeSynced(nodeName)) {
            throw new SkipException("Attribute verification skipped as node " + nodeName + " was not synced");
        }

        final NodeInfo nodeInfo = new NodeInfo(simulation, nodeName, enmVersion, host);
        final boolean testResult = moVerifier.validateNode(nodeInfo);

        displayResultsUtility.logNodeComparisionResult(nodeName, moVerifier.getPreviousNodeComparisonList());
        log(nodeName, simulation, enmVersion, testResult);
        assertTrue("Attribute comparison failure", testResult);
    }

    private String getKerFilePathFromResourcesDir(final String kerFile) {
        final String currDir = System.getProperty("user.dir");
        final String midPath = "ERICTAFcmsyncnodeatt_CXP9031062/src/main/resources";
        final Path path = Paths.get(currDir, midPath, kerFile);
        return path.toString();
    }

    private NetSimResult executeKertayleFile(final String kertayleFileLocation, final Host host, final NetworkElement ne)
            throws InterruptedException {

        final RemoteFileHandler remote = new RemoteFileHandler(host);
        final String fileName = String.valueOf(UUID.randomUUID());
        final String remoteFileLocation = "/tmp/taf/kertayle/" + fileName;
        remote.copyLocalFileToRemote(kertayleFileLocation, remoteFileLocation);

        final KertayleCommand kertayleCommand = NetSimCommands.kertayle();
        kertayleCommand.setFile(remoteFileLocation);
        final NetSimResult result = ne.exec(kertayleCommand);
        Thread.sleep(sleepForKeratyleExec);
        remote.deleteRemoteFile(remoteFileLocation);
        return result;
    }

    private void assertGoodNetsimResult(final NetSimResult result) {
        logger.debug("processing netsim result....");
        final CommandOutput[] outputs = result.getOutput();
        for (final CommandOutput cmdOutput : outputs) {
            assertTrue("expected empty command response list on a successful execution", cmdOutput.asList().isEmpty());
        }
    }

}
