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
package com.ericsson.oss.test.util.common.constants;

import com.ericsson.cifwk.taf.data.DataHandler;

public interface CommonConstants {

    // JBoss Instance Names
    // String CMSERV_0_JBOSS_INSTANCE = (String) DataHandler.getAttribute("jboss.serv");
    String CMSERV_0_JBOSS_INSTANCE = "cmserv_su0";
    String MSCM_0_JBOSS_INSTANCE = "internal_mscm_su0";
    String MSCM_1_JBOSS_INSTANCE = "internal_mscm_su1";
    String APACHE_JBOSS_INSTANCE = "httpd_su0";
    String SC1_PROPERTY = "sc1";
    String SC2_PROPERTY = "sc2";

    String DEPLOYED_TEST_WAR = (String) DataHandler.getAttribute("artifact.sync-utils-filename");
    String PATH_TO_JCAT = "test-pom/target/Jcat_LOGS";
    String SYNC_UTILS_REST_CALL = "/sync-taf-utils-war/syncVerification/";
    String VALIDATE_MO_REST_URI = SYNC_UTILS_REST_CALL + "compareMO";
    String VALIDATE_NODE_REST_URI = SYNC_UTILS_REST_CALL + "compareNode";
    String GET_FDNS_REST_URI = SYNC_UTILS_REST_CALL + "fdnList";
    Long NETSIM_READTIME = 80000L;

    // HTTP URLs
    String APACHE_LOGIN_URI = "/login";
    String SCRIPT_ENGINE_REST_URI = (String) DataHandler.getAttribute("rest.script.engine");

    // HTTPS headers
    String ID_TOKEN_1 = "IDToken1";
    String ID_TOKEN_2 = "IDToken2";
    String TOR_USERID = "TorUserID";

    // HTTP Response codes
    String VALID_LOGIN = "0";

    // APACHE LOGIN
    String DEFAULT_ENM_USER = "administrator";
    String DEFAULT_ENM_PASS = "TestPassw0rd";
    String ENM_ADMIN_USER = "ADMINISTRATOR";

    // ENM User Creation
    String USERNAME = "erivdell";
    String FIRSTNAME = "Riven";
    String LASTNAME = "Dell";
    String EMAIL = "rivendell@ericsson.com";
    String PASSWORD = "Password1";

    // Parallel Sync Properties
    String NUMBER_OF_NODES = "NumberOfNodes";
    String NODE_NAMES = "NodeNames";

    // Cmedit Commands
    String ADD_ME_CONTEXT = "cmedit create MeContext=%1$s MeContextId=%1$s, neType=ERBS, platformType=CPP -ns=OSS_TOP -version=2.0.0";
    String ADD_NETWORK_ELEMENT = "cmedit create NetworkElement=%1$s networkElementId=%1$s, neType=ERBS, platformType=CPP, ossPrefix=\"MeContext=%1$s\" -ns=OSS_NE_DEF -version=1.0.0";
    String ADD_CPP_CI = "cmedit create NetworkElement=%s,CppConnectivityInformation=1 CppConnectivityInformationId=1, ipAddress=\"%s\", port=80 -ns=CPP_MED -version=1.0.0";
}
