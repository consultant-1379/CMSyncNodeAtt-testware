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
package com.ericsson.oss.test.util.common.dto;

import java.io.Serializable;

import com.ericsson.cifwk.taf.data.Host;

public class NodeInfo implements Serializable {

    private static final long serialVersionUID = -4481017320505024691L;

    private String enmVersion;
    private String nodeName;
    private String simulation;
    private String fdn;
    private Host host;

    public NodeInfo() {

    }

    public NodeInfo(final String simulation, final String nodeName, final String enmVersion, final Host host) {
        this.simulation = simulation;
        this.nodeName = nodeName;
        this.enmVersion = enmVersion;
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(final Host host) {
        this.host = host;
    }

    public String getSimulation() {
        return simulation;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getEnmVersion() {
        return enmVersion;
    }

    public void setEnmVersion(final String enmVersion) {
        this.enmVersion = enmVersion;
    }

    public String getFdn() {
        return fdn;
    }

    public void setFdn(final String fdn) {
        this.fdn = fdn;
    }

}
