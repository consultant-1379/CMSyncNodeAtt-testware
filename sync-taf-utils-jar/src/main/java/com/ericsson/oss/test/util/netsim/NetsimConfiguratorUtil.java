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
package com.ericsson.oss.test.util.netsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.ericsson.cifwk.taf.handlers.netsim.domain.SimulationGroup;

public class NetsimConfiguratorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetsimConfiguratorUtil.class);
    private static NetSimCommandHandler commandHandler;
    static Map<String, List<NetworkElement>> simulationNEMap = new HashMap<String, List<NetworkElement>>();

    private static NetsimConfiguratorUtil netsimHelper;

    private NetsimConfiguratorUtil() {
    }

    public static NetsimConfiguratorUtil getInstance() {
        if (netsimHelper == null) {
            netsimHelper = new NetsimConfiguratorUtil();
        }
        return netsimHelper;
    }

    /**
     * 
     * @param netSimCommandHandler
     * @param simulation
     * @return all the started NEs for the simulation
     */
    private static List<NetworkElement> getAllStartedNEs(final NetSimCommandHandler netSimCommandHandler, final String simulation) {
        final List<NetworkElement> startedNeList = new ArrayList<NetworkElement>();
        List<NetworkElement> neList;
        final SimulationGroup simGroup = netSimCommandHandler.getSimulations(simulation);
        final Simulation currentSimulation = simGroup.get(simulation);

        neList = getSimulationNEs(netSimCommandHandler, simulation);
        for (final NetworkElement ne : neList) {
            LOGGER.debug("Checking if node: {} is started.", ne.getName());
            if (checkIfNodeIsStarted(ne.getName(), currentSimulation)) {
                //Taf method doesn't work, ne.isStarted();
                //or netsimCommandHandler.isStarted(ne.getNodeName());
                //see CIP-5870
                LOGGER.debug("Node: {} is started, adding to started ne list.", ne.getName());
                startedNeList.add(ne);
            }
        }
        NetSimCommandHandler.closeAllContexts();
        return startedNeList;
    }

    /**
     * Gets a specified amount of started NE's from a simulation
     * 
     * @param simulation
     * @param requiredNoOfNodes
     * @return a list of the specified amount of *Started* NEs for the simulation
     */
    public static List<NetworkElement> getStartedNEsFromSimulation(final String simulation, final int requiredNoOfNodes) {
        final NetSimCommandHandler netSimCommandHandler = getNetsimCommandHandler();
        final List<NetworkElement> startedNeList = new ArrayList<NetworkElement>();
        List<NetworkElement> neList;
        final SimulationGroup simGroup = netSimCommandHandler.getSimulations(simulation);
        final Simulation currentSimulation = simGroup.get(simulation);

        neList = getSimulationNEs(netSimCommandHandler, simulation);

        int noOfNodesAdded = 0;
        LOGGER.debug("Trying to get {} STARTED NEs from simulation = {}", requiredNoOfNodes, currentSimulation);
        for (final NetworkElement ne : neList) {
            if (noOfNodesAdded >= requiredNoOfNodes) {
                break;
            } else {
                if (checkIfNodeIsStarted(ne.getName(), currentSimulation)) {
                    LOGGER.debug("Adding to started list - Name[{}], ipaddress[{}], simulation[{}]", ne.getName(), ne.getIp(), ne.getSimulation());
                    startedNeList.add(ne);
                    noOfNodesAdded++;
                }
            }
        }
        LOGGER.debug("Got {} STARTED NEs from simulation = {}", startedNeList.size(), currentSimulation);
        NetSimCommandHandler.closeAllContexts();
        return startedNeList;
    }

    public static List<NetworkElement> getNumberOfNEsFromSimulationAndStart(final String simulation, final int requiredNoOfNodes) {
        final NetSimCommandHandler netSimCommandHandler = getNetsimCommandHandler();
        final List<NetworkElement> startedNeList = new ArrayList<NetworkElement>();
        List<NetworkElement> neList;
        final SimulationGroup simGroup = netSimCommandHandler.getSimulations(simulation);
        final Simulation currentSimulation = simGroup.get(simulation);
        neList = getSimulationNEs(netSimCommandHandler, simulation);
        String nodeNames = "";

        int noOfNodesAdded = 0;
        LOGGER.debug("Trying to start {} NEs from simulation = {}", requiredNoOfNodes, currentSimulation);
        for (final NetworkElement ne : neList) {
            if (noOfNodesAdded >= requiredNoOfNodes) {
                break;
            } else {
                nodeNames = nodeNames + " " + ne.getName();
                startedNeList.add(ne);
                noOfNodesAdded++;
            }
        }
        LOGGER.debug("sending start command for nodes {} from simulation {}", nodeNames, simulation);
        netSimCommandHandler.exec(NetSimCommands.open(simulation), NetSimCommands.selectnocallback(nodeNames),
                NetSimCommands.start().setParallel(true));

        LOGGER.debug("Got {} STARTED NEs from simulation = {}", startedNeList.size(), currentSimulation);
        NetSimCommandHandler.closeAllContexts();
        return startedNeList;
    }

    /**
     * Gets a list of the NE's for the simulation.
     * 
     * @param netSimCommandHandler
     * @param simulation
     * @return a list of the NEs of type <code>NetworkElement</code> for the simulation (regardless of whether they are started or not)
     */
    private static List<NetworkElement> getSimulationNEs(final NetSimCommandHandler netSimCommandHandler, final String simulation) {
        List<NetworkElement> neList = new ArrayList<NetworkElement>();
        if (simulation == "") {
            return neList;
        }
        LOGGER.debug("Getting NEs from simulation = {}", simulation);
        final NeGroup neGroup = netSimCommandHandler.getSimulationNEs(simulation);
        LOGGER.debug("Total Network Elements found in simulation: {}", neGroup.size());
        neList = neGroup.getNetworkElements();
        return neList;
    }

    //TODO this method should be removed when CIP-5870 is released.
    private static boolean checkIfNodeIsStarted(final String node, final Simulation currentSimulation) {
        LOGGER.debug("Checking if node: {} is started.", node);
        final String command = node + "\n.isstarted";
        final NetSimResult netSimResult = currentSimulation.exec(NetSimCommands.selectnocallback(command));
        final CommandOutput[] commandOutputs = netSimResult.getOutput();
        final String isStartedResult = commandOutputs[1].getRawOutput();
        if (isStartedResult.contains("NotStarted")) {
            LOGGER.debug("Node: {} is NOT started.", node);
            return false;
        } else {
            LOGGER.debug("Node: {} is STARTED.", node);
            return true;
        }
    }

    private static void addToSimNeMap(final String simulationName, final NetworkElement networkElement) {
        List<NetworkElement> list = simulationNEMap.get(simulationName);
        if (list == null) {
            list = new ArrayList<NetworkElement>();
            list.add(networkElement);
        } else {
            list.add(networkElement);
        }
        simulationNEMap.put(simulationName, list);
    }

    public static List<NetworkElement> getNetsimNEFromMap(final String simulationName) {
        List<NetworkElement> list = simulationNEMap.get(simulationName);
        if (list == null) {
            list = new ArrayList<NetworkElement>();
        }

        return list;
    }

    private static NetSimCommandHandler getNetsimCommandHandler() {
        final Host netsimHost = DataHandler.getHostByName("Netsim");
        commandHandler = NetSimCommandHandler.getInstance(netsimHost);
        return commandHandler;
    }

    public static String getIPAddress(final String simulation, final String nodeName) {
        final List<NetworkElement> neList = getAllStartedNEs(getNetsimCommandHandler(), simulation);
        LOGGER.debug("Total Network Elements started in simulation: {}", neList.size());

        for (final NetworkElement networkElement : neList) {
            if (networkElement.getName().equalsIgnoreCase(nodeName)) {
                return networkElement.getIp();
            }
        }
        return "";
    }

    public static List<NetworkElement> getAllStartedNesFromSimList(final List<String> simulationNames) {

        populateStartedNesMap(simulationNames);

        final Set<String> simsWithStartedNEs = simulationNEMap.keySet();
        final List<NetworkElement> allTheNodes = new ArrayList<>();
        for (final String simulationName : simulationNames) {
            for (final String nameOfSimWithStartedNEs : simsWithStartedNEs) {
                if (simulationName.startsWith(nameOfSimWithStartedNEs)) {
                    allTheNodes.addAll(simulationNEMap.get(nameOfSimWithStartedNEs));
                }
            }
        }
        return allTheNodes;
    }

    private static void populateStartedNesMap(final List<String> simulations) {
        clearMap();
        for (final String sim : simulations) {
            final List<NetworkElement> neList = getAllStartedNEs(getNetsimCommandHandler(), sim);
            LOGGER.debug("neList size is {}", neList.size());
            for (final NetworkElement ne : neList) {
                LOGGER.debug("Adding to map- Name [{}], ip [{}], simulation [{}]", ne.getName(), ne.getIp(), ne.getSimulation());
                addToSimNeMap(sim, ne);
            }
        }
    }

    private static void clearMap() {
        simulationNEMap.clear();
    }
}