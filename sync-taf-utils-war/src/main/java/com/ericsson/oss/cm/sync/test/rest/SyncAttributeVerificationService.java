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
package com.ericsson.oss.cm.sync.test.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.ericsson.oss.test.cache.NetsimDataCacheBean;
import com.ericsson.oss.test.util.common.dto.CompareResponse;
import com.ericsson.oss.test.util.common.dto.NodeInfo;
import com.ericsson.oss.test.util.verifier.MoVerifier;

@Path("/syncVerification")
@RequestScoped
public class SyncAttributeVerificationService {

    private static Logger LOGGER = Logger.getLogger(SyncAttributeVerificationService.class);

    @Inject
    protected UserTransaction utx;

    @EJB
    private NetsimDataCacheBean netsimDataCacheBean;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/fdnList")
    public Response getFdnListForNetsimNode(final String nodeInfoJson) {
        final NodeInfo nodeInfo;
        List<String> fdnList = null;
        try {
            nodeInfo = parseNodeInfoJson(nodeInfoJson);
            fdnList = netsimDataCacheBean.getListOfFdns(nodeInfo);
        } catch (final Exception e) {
            LOGGER.error("Problem occured while retrieving netsim node fdn list.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(200).entity(fdnList).build();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/compareNode/")
    public Response compareNode(final String nodeInfoJson) {
        NodeInfo nodeInfo = null;
        List<String> fdnList = null;
        final List<CompareResponse> responseList = new ArrayList<CompareResponse>();

        try {
            utx.begin();
            nodeInfo = parseNodeInfoJson(nodeInfoJson);
            fdnList = netsimDataCacheBean.getListOfFdns(nodeInfo);

            for (final String fdn : fdnList) {
                nodeInfo.setFdn(fdn);
                responseList.add(verifyMo(nodeInfo));
            }
        } catch (final Exception e) {
            LOGGER.error("Problem comparing Node", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            try {
                utx.commit();
            } catch (final Exception e) {
                LOGGER.error("Problem running commit after comparing node", e);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        }
        return Response.status(200).entity(responseList).build();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/compareMO/")
    public Response compareMO(final String nodeInfoJson) {

        CompareResponse response = null;
        final NodeInfo nodeInfo;

        try {
            utx.begin();
            nodeInfo = parseNodeInfoJson(nodeInfoJson);
            response = verifyMo(nodeInfo);

        } catch (final Exception e) {
            LOGGER.error("Problem comparing complete MO", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();

        } finally {
            try {
                utx.commit();
            } catch (final Exception e) {
                LOGGER.error("Problem running commit after comparing complete MO", e);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        }
        return Response.status(200).entity(response).build();
    }

    private NodeInfo parseNodeInfoJson(final String nodeInfoJson) throws JsonParseException, JsonMappingException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(nodeInfoJson, NodeInfo.class);
    }

    private CompareResponse verifyMo(final NodeInfo nodeInfo) {
        final Map<String, Object> attributeNamesToNetimValues = netsimDataCacheBean.getMoAttributes(nodeInfo);

        LOGGER.debug("FDN currently processed: " + nodeInfo.getFdn());
        final MoVerifier attributeHelper = new MoVerifier();
        final Map<String, Long> fdnToMoRefMap = netsimDataCacheBean.getFdnToMoRefMap(nodeInfo);
        return attributeHelper.verify(fdnToMoRefMap, nodeInfo, attributeNamesToNetimValues, nodeInfo.getFdn());
    }

}
