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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestData;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.DumpmotreeCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.test.util.netsim.parser.FDNandMoidTreedumpParser;
import com.ericsson.oss.test.util.netsim.parser.NetsimMMLDeepScopeOutputParser;
import com.ericsson.oss.test.util.netsim.parser.NetsimMMLOutputParseException;

public class NetsimMoReaderUtil implements TestData {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetsimMoReaderUtil.class);
    private NetSimCommandHandler netSimCommandHandler;

    public NetsimMoReaderUtil(final Host host) {
        netSimCommandHandler = NetSimCommandHandler.getInstance(host);
    }

    public Map<String, Long> getMoRefs(final String simulation, final String nodeName) {

        final DumpmotreeCommand moRefsNetSimCommand = getMoRefsNetSimCommand();
        final NetworkElement ne = getNetworkElement(simulation, nodeName);
        final String output = executeCommandOnNe(ne, moRefsNetSimCommand);
        final Map<String, Long> moRef = FDNandMoidTreedumpParser.parseTreeMoDumpOutput(output, nodeName);
        return moRef;
    }

    public Map<String, Map<String, Object>> getAllMoAttributes(final String simulation, final String nodeName) {

        Map<String, Map<String, Object>> allMOAttributes = new HashMap<String, Map<String, Object>>();
        final DumpmotreeCommand allMOAttributesNetSimCommand = getAllMoAttributesNetSimCommand();
        final NetworkElement ne = getNetworkElement(simulation, nodeName);
        final String output = executeCommandOnNe(ne, allMOAttributesNetSimCommand);

        try {
            allMOAttributes = NetsimMMLDeepScopeOutputParser.parseMMLOutput(output, nodeName);

        } catch (final NetsimMMLOutputParseException e) {
            LOGGER.error("Problem parsing MML output from netsim.", e);
        }
        return allMOAttributes;
    }

    private String executeCommandOnNe(final NetworkElement ne, final NetSimCommand netsimCommand) {

        final NetSimResult results = ne.exec(netsimCommand);
        final CommandOutput[] commandOutput = results.getOutput();
        //There was only one command sent so there will only be one CommandOutput
        return commandOutput[0].getRawOutput();
    }

    private DumpmotreeCommand getMoRefsNetSimCommand() {

        final DumpmotreeCommand dumpmotreeCommand = NetSimCommands.dumpmotree();
        dumpmotreeCommand.setKerOut(true);
        dumpmotreeCommand.setMoidcomment(true);
        return dumpmotreeCommand;
    }

    private DumpmotreeCommand getAllMoAttributesNetSimCommand() {

        final DumpmotreeCommand dumpmotreeCommand = NetSimCommands.dumpmotree();
        dumpmotreeCommand.setPrintattrs(true);
        return dumpmotreeCommand;
    }

    private NetworkElement getNetworkElement(final String simulation, final String nodeName) {

        final NeGroup neGroup = netSimCommandHandler.getSimulationNEs(simulation);
        return neGroup.get(nodeName);
    }
}