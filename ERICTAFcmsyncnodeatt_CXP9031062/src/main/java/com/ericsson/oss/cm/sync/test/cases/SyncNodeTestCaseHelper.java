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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.enm.data.ENMUser;
import com.ericsson.nms.launcher.LauncherOperator;
import com.ericsson.nms.security.OpenIDMOperatorImpl;
import com.ericsson.oss.cm.sync.test.operators.DeltaSyncRestOperator;
import com.ericsson.oss.cm.sync.test.operators.ScriptEngineRestOperator;
import com.ericsson.oss.cm.sync.test.operators.SyncStatusOperator;
import com.ericsson.oss.test.util.common.NodeVersionMapper;
import com.ericsson.oss.test.util.common.constants.CommonConstants;
import com.ericsson.oss.test.util.common.constants.DeltaSyncConstants;
import com.ericsson.oss.test.util.connection.JbossUtility;
import com.ericsson.oss.test.util.connection.ToolGetter;
import com.ericsson.oss.test.util.connection.UserUtility;
import com.ericsson.oss.test.util.verifier.MoVerifierRestClient;

public class SyncNodeTestCaseHelper extends TorTestCaseHelper implements CommonConstants, DeltaSyncConstants {

    @Inject
    private OpenIDMOperatorImpl openIDMOperator;

    @Inject
    private LauncherOperator launcherOperator;

    protected List<String> nodesAddedList = new ArrayList<String>();
    protected HttpTool secureHttpTool;
    protected HttpTool unsecureHttpTool;
    protected ScriptEngineRestOperator scriptEngineRestOperator;
    protected DeltaSyncRestOperator deltaSyncRestOperator;
    protected MoVerifierRestClient moVerifier;
    protected JbossUtility jbossUtility;

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncNodeTestCaseHelper.class);

    private final ToolGetter toolGetter = new ToolGetter();
    private SyncStatusOperator syncStatusOperator = new SyncStatusOperator();
    private ENMUser enmUser;
    private final String nLine = System.getProperty("line.separator");

    public void setup() {
        try {
            jbossUtility = new JbossUtility();
            //            jbossUtility.setSyncStatusLogToDebug();
            LOGGER.info("Running test against SC1 with IP [{}]", toolGetter.getHost(SC1_PROPERTY).getIp());

            final UserUtility userUtility = new UserUtility();
            enmUser = userUtility.createUserAndAssignRole(openIDMOperator);

            secureHttpTool = userUtility.logInSecurely(launcherOperator, enmUser);
            unsecureHttpTool = new ToolGetter().getUnsecureHttpTool(CMSERV_0_JBOSS_INSTANCE);
            scriptEngineRestOperator = new ScriptEngineRestOperator(secureHttpTool);
            deltaSyncRestOperator = new DeltaSyncRestOperator(scriptEngineRestOperator);
            moVerifier = new MoVerifierRestClient(unsecureHttpTool, VALIDATE_MO_REST_URI, VALIDATE_NODE_REST_URI);
            assertTrue("Dependencies for the SynchNode have not been validated", jbossUtility.updateWarDeployedOnJboss());
        } catch (final Exception e) {
            LOGGER.error("Test setup failed", e);
            fail("Test setup failed");
        }
        LOGGER.info("Starting test ...");
    }

    public void tearDown() {
        try {
            jbossUtility.setSyncStatusLogToInfo();
            jbossUtility.cleanUp();

            LOGGER.info("deleting nodes...");
            for (final String nodeName : nodesAddedList) {
                scriptEngineRestOperator.deleteNode(nodeName);
            }
            LOGGER.info("Nodes deleted.");

            launcherOperator.logout();
            openIDMOperator.deleteUser(enmUser.getUsername());
            toolGetter.closeAllHttpTools();
        } catch (final Exception e) {
            LOGGER.error("Test teardown failed", e);
            fail("Test teardown failed");
        }
    }

    public void log(final String nodeName, final String nodeIp, final String enmVersion, final boolean isAdded, final boolean isSynced, final Date syncStartTime) {

        final boolean testResult = isAdded & isSynced;
        final String syncStatesAndTimes, syncNodeResult;

        if (!isAdded) {
            syncNodeResult = "Sync skipped due to add node failure";
            syncStatesAndTimes = "-";
        } else {
            syncNodeResult = (isSynced ? "P A S S" : "F A I L");
            syncStatesAndTimes = syncStatusOperator.getSyncTimesFromLogs(syncStartTime, nodeName);
        }

        final StringBuilder testInfo = new StringBuilder();
        testInfo.append(nLine + nLine + "#############  Test: Add, Sync Node and Verify Node Status Synced  #############");
        testInfo.append(nLine + "----------------------");
        testInfo.append(nLine + "-->         Node Name: " + nodeName);
        testInfo.append(nLine + "-->           Node IP: " + nodeIp);
        testInfo.append(nLine + "-->      Node Version: " + enmVersion);
        testInfo.append(nLine + "-->   Add Node Result: " + (isAdded ? "P A S S" : "F A I L"));
        testInfo.append(nLine + "-->  Sync Node Result: " + syncNodeResult);
        testInfo.append(nLine + "-->        Sync Times: " + syncStatesAndTimes);
        testInfo.append(nLine + "----------------------");
        testInfo.append(nLine + "-->       Test result: " + (testResult ? "P A S S" : "F A I L") + nLine);
        LOGGER.info(testInfo.toString());
    }

    public void log(final String nodeName, final String simulation, final String enmReleaseVersion, final boolean testResult) {
        final Map<String, String> versions = NodeVersionMapper.getNodeVersions(enmReleaseVersion);
        final StringBuilder testInfo = new StringBuilder();
        testInfo.append(nLine + nLine + "#############  Test: Sync Node Attribute verification with DPS, Model service and Netsim  #############");
        testInfo.append(nLine + "----------------------");
        testInfo.append(nLine + "-->         Node Name: " + nodeName);
        testInfo.append(nLine + "--> Netsim Simulation: " + simulation);
        testInfo.append(nLine + "-->      ERBS Version: " + versions.get("ERBS"));
        testInfo.append(nLine + "-->       CPP Version: " + versions.get("CPP"));
        testInfo.append(nLine + "----------------------");
        testInfo.append(nLine + "-->       Test result: " + (testResult ? "P A S S" : "F A I L") + nLine);
        LOGGER.info(testInfo.toString());
    }
}
