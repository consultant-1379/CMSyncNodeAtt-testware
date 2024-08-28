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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;

public class ParallelSynchNodeTestData {
    private Map<String, Map<String, NetworkElement>> netsimNodeMapInclVersion;
    private int numberOfNodes;

    /**
     * @param netsimNodeMapInclVersion
     */
    public ParallelSynchNodeTestData(final Map<String, Map<String, NetworkElement>> netsimNodeMapInclVersion) {
        this.netsimNodeMapInclVersion = netsimNodeMapInclVersion;
    }

    /**
     * @return the netsimNodeMapInclVersion
     */
    public Map<String, Map<String, NetworkElement>> getNetsimNodeMapInclVersion() {
        return netsimNodeMapInclVersion;
    }

    /**
     * 
     * @return list of node names
     */
    public List<String> getNodeNames() {
        final List<String> nodeNames = new ArrayList<String>();
        for (final Entry<String, Map<String, NetworkElement>> versionNodeDetailsMapping : netsimNodeMapInclVersion.entrySet()) {
            for (final Entry<String, NetworkElement> nodeNameNEMapping : versionNodeDetailsMapping.getValue().entrySet()) {
                nodeNames.add(nodeNameNEMapping.getKey());
            }

        }

        return nodeNames;
    }

    /**
     * @return the numberOfNodes
     */
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    /**
     * @param numberOfNodes
     *            the newNumberOfNodes to set
     */
    public void setNumberOfNodes(final int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }
}
