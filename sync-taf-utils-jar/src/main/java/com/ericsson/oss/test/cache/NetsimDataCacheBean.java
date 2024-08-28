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
package com.ericsson.oss.test.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.oss.test.util.common.dto.NodeInfo;
import com.ericsson.oss.test.util.netsim.NetsimMoReaderUtil;

@Singleton
public class NetsimDataCacheBean {

    private static Logger LOGGER = LoggerFactory.getLogger(NetsimDataCacheBean.class);
    private List<String> nodesCached = new ArrayList<String>();
    private static Map<String, Map<String, Object>> fdnToAllMoAttributesMap = new HashMap<String, Map<String, Object>>();
    private Map<String, Long> fdnToMoRefMap = new HashMap<String, Long>();

    public Map<String, Object> getMoAttributes(final NodeInfo nodeInfo) {
        if (!isNetsimDataCached(nodeInfo)) {
            cacheAllNetsimDataForNode(nodeInfo);
        }
        return fdnToAllMoAttributesMap.get(nodeInfo.getFdn());
    }

    public Map<String, Long> getFdnToMoRefMap(final NodeInfo nodeInfo) {
        if (!isNetsimDataCached(nodeInfo)) {
            cacheAllNetsimDataForNode(nodeInfo);
        }
        final Map<String, Long> fdnToMoRefMap = new HashMap<String, Long>();
        final Set<String> fdnSet = this.fdnToMoRefMap.keySet();
        for (final String fdn : fdnSet) {
            if (fdn.startsWith("MeContext=" + nodeInfo.getNodeName())) {
                final Long moRef = this.fdnToMoRefMap.get(fdn);
                fdnToMoRefMap.put(fdn, moRef);
            }
        }
        return fdnToMoRefMap;
    }

    public List<String> getListOfFdns(final NodeInfo nodeInfo) {
        if (!isNetsimDataCached(nodeInfo)) {
            cacheAllNetsimDataForNode(nodeInfo);
        }
        final List<String> fdnList = new ArrayList<String>();
        final Set<String> fdnSet = getFdnToMoRefMap(nodeInfo).keySet();
        fdnList.addAll(fdnSet);
        return fdnList;
    }

    private void cacheAllNetsimDataForNode(final NodeInfo nodeInfo) {
        checkHostInfoValid(nodeInfo);
        final NetsimMoReaderUtil netsimDataProvider = new NetsimMoReaderUtil(nodeInfo.getHost());
        final String nodeName = nodeInfo.getNodeName();
        final String simulation = nodeInfo.getSimulation();
        LOGGER.info("Retrieving netsim data from [{}], for node [{}]", simulation, nodeName);

        final Map<String, Long> netsimMoRefsResult = netsimDataProvider.getMoRefs(simulation, nodeName);
        LOGGER.debug("Retreived fdn list from netsim node [{}] of size [{}]", nodeName, netsimMoRefsResult.size());
        final Map<String, Map<String, Object>> netsimAttributesResult = netsimDataProvider.getAllMoAttributes(simulation, nodeName);

        fdnToMoRefMap.putAll(netsimMoRefsResult);
        fdnToAllMoAttributesMap.putAll(netsimAttributesResult);
        nodesCached.add(nodeInfo.getNodeName());

    }

    private boolean isNetsimDataCached(final NodeInfo nodeInfo) {
        final String nodeName = nodeInfo.getNodeName();
        return nodesCached.contains(nodeName);
    }

    /*
     * The host properties for the cloud are overwritten by the host configurator, they need to be the original values
     */
    private void checkHostInfoValid(final NodeInfo nodeInfo) {
        final Host host = nodeInfo.getHost();
        final Map<Ports, String> ports = new HashMap<Ports, String>();
        ports.put(Ports.SSH, "22");
        host.setPort(ports);

        if (host.getOriginalIp() != null) {
            host.setIp(host.getOriginalIp());
            LOGGER.debug("Updated netsim host properties. Set to port: 22 and ip [{}]", host.getOriginalIp());
        }
        nodeInfo.setHost(host);

    }
}
