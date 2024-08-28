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
package com.ericsson.oss.cm.sync.test.operators;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.http.HttpTool;

public class SyncNodeCallable implements Callable<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncNodeCallable.class);
    private String synchFdn = "";
    private long startTime;
    private Long timeTaken;
    private HttpTool secureHttpTool;

    public SyncNodeCallable(final String synchFdn, final HttpTool secureHttpTool) {
        this.synchFdn = synchFdn;
        this.secureHttpTool = secureHttpTool;
    }

    @Override
    public Boolean call() throws Exception {
        LOGGER.debug("{} is starting to Synch on ThreadId[{}]", synchFdn, Thread.currentThread().getId());
        final ScriptEngineRestOperator scriptEngineRestOperator = new ScriptEngineRestOperator(secureHttpTool);
        scriptEngineRestOperator.syncNode(synchFdn);
        startTime = System.currentTimeMillis();
        try {
            while (!scriptEngineRestOperator.isNodeSynced(synchFdn)) {
                Thread.sleep(1000);
                timeTaken = calculateTimeTaken(startTime);
                if (timeTaken > (45 * 1000)) {
                    LOGGER.error("Synch of node {} has taken longer than 45 seconds, presuming failure.", synchFdn);
                    return false;
                }
            }

        } catch (final RuntimeException e) {
            LOGGER.error("Error occurred in SyncNodeCallable", e);
            return false;
        }

        return true;
    }

    private Long calculateTimeTaken(final Long startTime) {
        final Long stopTime = System.currentTimeMillis();
        final Long timeTaken = stopTime - startTime;
        return timeTaken;
    }
}