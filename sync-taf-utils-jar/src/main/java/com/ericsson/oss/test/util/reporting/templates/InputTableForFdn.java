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

public class InputTableForFdn {

    public InputTableForFdn(final CompareResponse resp) {
        this.nodeName = resp.getNodeName();
        this.version = resp.getVersion();
        this.namespace = resp.getNamespace();
        if (NodeVersionMapper.isValidCppVersion(version)) {
            this.ennVersion = NodeVersionMapper.getEnmReleaseVersionFromCppVersion(version);
        } else {
            this.ennVersion = NodeVersionMapper.getEnmReleaseVersionFromErbsVersion(version);
        }
        this.modelName = resp.getModelName();
        this.fdn = resp.getFdn();
    }

    String nodeName, version, ennVersion, modelName, fdn, namespace;
}