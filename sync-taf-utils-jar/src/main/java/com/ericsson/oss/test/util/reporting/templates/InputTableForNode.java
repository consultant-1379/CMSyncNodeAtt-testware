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
package com.ericsson.oss.test.util.reporting.templates;

import com.ericsson.oss.test.util.common.NodeVersionMapper;
import com.ericsson.oss.test.util.common.dto.CompareResponse;

public class InputTableForNode {

    public InputTableForNode(final CompareResponse resp, final int totalFdnCount, final int fdnFailureCount) {
        this.nodeName = resp.getNodeName();

        final String version = resp.getVersion();
        if (NodeVersionMapper.isValidErbsVersion(version)) {
            this.erbsVersion = version;
            this.cppVersion = NodeVersionMapper.getCppVersionFromErbsVersion(erbsVersion);
        } else {
            this.cppVersion = version;
            this.erbsVersion = NodeVersionMapper.getErbsVersionFromCppVersion(cppVersion);
        }

        this.enmVersion = NodeVersionMapper.getEnmReleaseVersionFromCppVersion(cppVersion);
        this.modelName = resp.getModelName();
        this.fdn = resp.getFdn();
        this.totalFdnCount = totalFdnCount;
        this.fdnfailureCount = fdnFailureCount;
    }

    final String nodeName, erbsVersion, cppVersion, modelName, fdn, enmVersion;
    final int totalFdnCount, fdnfailureCount;
}