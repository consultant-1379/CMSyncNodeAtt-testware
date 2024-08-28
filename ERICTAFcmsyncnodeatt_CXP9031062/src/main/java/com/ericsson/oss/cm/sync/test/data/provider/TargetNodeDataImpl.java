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
package com.ericsson.oss.cm.sync.test.data.provider;

import java.util.*;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.oss.cm.sync.test.data.ParallelSynchNodeTestData;

public class TargetNodeDataImpl extends TargetNodeData {

    private static String D144_VERSION = "4.1.44";
    private static String D144_SIMULATION = "LTED144-limx160-RV-FDD-LTE32";

    private static String D1189_VERSION = "4.1.189";
    private static String D1189_SIMULATION = "LTED1189-limx160-FDD-LTE07";

    private static String E149_VERSION = "5.1.49";
    private static String E149_SIMULATION = "LTEE149x160-TOR-FDD-LTE05";

    private static String E1120_VERSION = "5.1.120";
    private static String E1120_SIMULATION_1 = "LTEE1120-V3x160-TOR-FDD-LTE11";
    private static String E1120_SIMULATION_2 = "LTEE1120-V2limx160-TOR-TDD-LTE02";
    private static String E1120_SIMULATION_3 = "LTEE1120-V2limx160-TOR-FDD-LTE01";
    private static String E1120_SIMULATION_4 = "LTEE1120-V3x160-TOR-FDD-LTE12";

    private static int NUMBER_OF_NODES_FOR_EACH_SIM = 160;

    //A custom Java Class should be used if you need more control of the test data
    //A single method annotated with @DataSource is required when using a java class as a data source
    @DataSource
    public List<Map<String, Object>> dataSource() {

        final Map<String, String> simMap = new HashMap<String, String>();
        simMap.put(D144_SIMULATION, D144_VERSION);
        simMap.put(D1189_SIMULATION, D1189_VERSION);
        simMap.put(E149_SIMULATION, E149_VERSION);
        simMap.put(E1120_SIMULATION_1, E1120_VERSION);
        simMap.put(E1120_SIMULATION_2, E1120_VERSION);
        simMap.put(E1120_SIMULATION_3, E1120_VERSION);
        simMap.put(E1120_SIMULATION_4, E1120_VERSION);

        final ParallelSynchNodeTestData synchNodePojo = generateTestData(NUMBER_OF_NODES_FOR_EACH_SIM, simMap);
        synchNodePojo.setNumberOfNodes(simMap.size() * NUMBER_OF_NODES_FOR_EACH_SIM);

        final Map<String, Object> data = Collections.<String, Object> singletonMap("ParallelSyncNodes", synchNodePojo);
        return Collections.singletonList(data);
    }
}