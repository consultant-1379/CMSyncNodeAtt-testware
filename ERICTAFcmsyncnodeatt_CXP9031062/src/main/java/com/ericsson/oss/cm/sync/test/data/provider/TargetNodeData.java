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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.cm.sync.test.data.ParallelSynchNodeTestData;
import com.ericsson.oss.cm.sync.test.data.SyncNodeServiceTestDataProvider;

public class TargetNodeData {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetNodeData.class);

    protected ParallelSynchNodeTestData generateTestData(final int numberOfNodes, final Map<String, String> simMap) {

        LOGGER.info("Generating test data from Netsim for " + simMap.size() * numberOfNodes + " node(s)");
        LOGGER.info("The simulation(s) being read from are: " + simMap.keySet());

        final ParallelSynchNodeTestData synchNodePojo = SyncNodeServiceTestDataProvider.readTopologyForMultipleNodesFromNetsim(simMap, numberOfNodes);

        if (synchNodePojo == null) {
            LOGGER.error("There is No Test Case Data Object, cannot proceed with the TestCase [{}]", synchNodePojo);
            return null;
        } else {
            LOGGER.info("Test Case Data Object was created correctly with following number of nodes [{}]", synchNodePojo.getNumberOfNodes());
        }
        return synchNodePojo;
    }
}
