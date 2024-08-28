/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.test.util.verifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.oss.test.util.common.dto.CompareResponse;
import com.ericsson.oss.test.util.common.dto.NodeInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MoVerifierRestClient {

    private HttpTool httpTool;
    private String validateMoRestUri;
    private String validateNodeRestUri;
    private List<CompareResponse> previousNodeComparison;

    private static final Logger LOGGER = LoggerFactory.getLogger(MoVerifierRestClient.class);

    public MoVerifierRestClient(final HttpTool httpTool, final String validateMoRestUri, final String validateNodeRestUri) {
        this.httpTool = httpTool;
        this.validateMoRestUri = validateMoRestUri;
        this.validateNodeRestUri = validateNodeRestUri;
    }

    public CompareResponse validateMo(final NodeInfo nodeInfo) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonString = objectMapper.writeValueAsString(nodeInfo);
        final HttpResponse resp = httpTool.request().contentType(ContentType.TEXT_PLAIN).body(jsonString).post(validateMoRestUri);
        return new Gson().fromJson(resp.getBody(), CompareResponse.class);
    }

    public boolean validateNode(final NodeInfo nodeInfo) throws IOException {

        LOGGER.info("Sending REST request to verify all attributes synced for node {} ", nodeInfo.getNodeName());
        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonString = objectMapper.writeValueAsString(nodeInfo);
        final HttpResponse resp = httpTool.request().contentType(ContentType.TEXT_PLAIN).body(jsonString).post(validateNodeRestUri);
        LOGGER.info("Received REST response to verify all attributes synced for node {} ", nodeInfo.getNodeName());
        previousNodeComparison = deSerializeCompareResponseList(resp);
        return hasTestPassed(previousNodeComparison);
    }

    public List<CompareResponse> getPreviousNodeComparisonList() {
        return previousNodeComparison;
    }

    private List<CompareResponse> deSerializeCompareResponseList(final HttpResponse resp) {

        final String json = resp.getBody();
        LOGGER.debug("Extract Fdn List json response: {}", json);
        final Type listType = new TypeToken<List<CompareResponse>>() {
        }.getType();

        return new Gson().fromJson(json, listType);
    }

    private boolean hasTestPassed(final List<CompareResponse> compareResponseList) {
        for (final CompareResponse resp : compareResponseList) {
            if (resp == null) {
                LOGGER.error("Received a null response");
                return false;
            } else if (!resp.isSuccess() && !isManagedElementComparison(resp)) {
                return false;
            }
        }
        return true;

    }

    // The ManagedElement model has two more attributes than netsim. So is allowed to fail 
    private boolean isManagedElementComparison(final CompareResponse resp) {
        String modelName = resp.getModelName(); 
    	if(modelName == null) {
    		LOGGER.warn("failed to retreive model from compare response for fdn: {}", resp.getFdn());
    		return false;
    	}
    	return modelName.equals("ManagedElement");
    }
}
