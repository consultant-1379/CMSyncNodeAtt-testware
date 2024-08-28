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
package com.ericsson.oss.test.util.connection;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.handlers.JbossCommandExecutor;
import com.ericsson.cifwk.taf.handlers.JbossHandler;
import com.ericsson.oss.test.util.common.constants.CommonConstants;

public class JbossUtility implements CommonConstants {

    static final Logger LOGGER = LoggerFactory.getLogger(JbossUtility.class);
    private final ToolGetter toolGetter;

    private final JbossHandler cmservJbossHandler;
    private final JbossHandler mscm1JbossHandler;
    //    private final JbossHandler mscm2JbossHandler;

    private final JbossCommandExecutor mscm1CommandExecutor;

    //    private final JbossCommandExecutor mscm2CommandExecutor;

    public JbossUtility() {
        toolGetter = new ToolGetter();
        cmservJbossHandler = new JbossHandler(toolGetter.getHost(CMSERV_0_JBOSS_INSTANCE), toolGetter.getHost(SC1_PROPERTY));
        mscm1JbossHandler = new JbossHandler(toolGetter.getHost(MSCM_0_JBOSS_INSTANCE), toolGetter.getHost(SC1_PROPERTY));
        //        mscm2JbossHandler = new JbossHandler(toolGetter.getHost(MSCM_1_JBOSS_INSTANCE), toolGetter.getHost(SC2_PROPERTY));

        mscm1CommandExecutor = mscm1JbossHandler.getCommandService();
        //        mscm2CommandExecutor = mscm2JbossHandler.getCommandService();
    }

    public boolean updateWarDeployedOnJboss() {
        String fileName = "";
        final File myCurrentDir = new File("");
        final String myCurrentDirPath = myCurrentDir.getAbsolutePath();
        LOGGER.debug("Here is the current absolute path {}", myCurrentDirPath);
        if (System.getProperty("os.name").startsWith("Windows")) {
            fileName = myCurrentDirPath + "\\sync-taf-utils-war\\target\\" + DEPLOYED_TEST_WAR; // works
            // for
            // windows
        } else if (System.getProperty("os.name").startsWith("Linux")) {
            fileName = myCurrentDirPath + "/sync-taf-utils-war/target/" + DEPLOYED_TEST_WAR;
        } else {
            fileName = "/local/Jenkins_Slaves/BuildServer_2/workspace/CmSyncNode_Regression_14B/sync-taf-utils-war/target/" + DEPLOYED_TEST_WAR; // works on jenkins
        }

        LOGGER.debug("fileName is {}", fileName);
        return deployFile(fileName);

    }

    public void cleanUp() {
        try {
            LOGGER.debug("Undeploy WAR..");
            unDeployFile(DEPLOYED_TEST_WAR);
            LOGGER.debug("Undeployed WAR..");
        } catch (final Exception e) {
            LOGGER.error("Problem undeploying WAR", e);
        } finally {
            LOGGER.debug("Close JBoss JMS");
            closeJbossJmx();
            LOGGER.debug("Closed JBoss JMS");
        }
    }

    public void setSyncStatusLogToDebug() {
        setSyncStatusLogLevel(mscm1CommandExecutor, "DEBUG");
        //        setSyncStatusLogLevel(mscm2CommandExecutor, "DEBUG");
    }

    public void setSyncStatusLogToInfo() {
        setSyncStatusLogLevel(mscm1CommandExecutor, "INFO");
        //        setSyncStatusLogLevel(mscm2CommandExecutor, "INFO");
    }

    private void setSyncStatusLogLevel(final JbossCommandExecutor commandExecutor, final String level) {
        final String packageToChange = "com.ericsson.nms.mediation.component.dps.common";
        final String READ_RESOURCE = "/subsystem=logging/logger=" + packageToChange + ":read-resource";
        final String ADD_RESOURCE = "/subsystem=logging/logger=" + packageToChange + ":add";
        final String WRITE_ATTRIBUTE = "/subsystem=logging/logger=" + packageToChange + ":write-attribute(name=\"level\"";

        if (!commandExecutor.execute(READ_RESOURCE)) {
            LOGGER.warn("Logger resource {} not found. Creating logger resource.", packageToChange);
            commandExecutor.execute(ADD_RESOURCE);
        }

        if (commandExecutor.execute(WRITE_ATTRIBUTE + ", value=\"" + level + "\")")) {
            LOGGER.info("Logger resource {} on server {}, changed to {}", packageToChange, commandExecutor.getJbossNode().getHostname(), level);
        } else {
            new RuntimeException("Failed to set jboss logging to debug");
        }
    }

    private boolean deployFile(final String targerEar) {
        final Boolean activate = true;
        final Boolean forcedeploy = true;
        final File targetEarFile = new File(targerEar);
        LOGGER.info("Deploying " + targerEar);
        return cmservJbossHandler.deployFile(targetEarFile, activate, forcedeploy);
    }

    private void unDeployFile(final String targerEar) {
        final Boolean removeContent = true;
        cmservJbossHandler.undeployFile(targerEar, removeContent);
    }

    private void closeJbossJmx() {
        cmservJbossHandler.getJmxService().close();
        mscm1JbossHandler.getJmxService().close();
        //        mscm2JbossHandler.getJmxService().close();
    }
}
