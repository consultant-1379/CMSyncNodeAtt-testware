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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.http.HttpTool;

public class ParallelSyncNodeOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelSyncNodeOperator.class);
    List<String> synchFdnList;
    ExecutorService executor;
    HttpTool secureHttpTool;
    Map<String, Future<Boolean>> synchJobsFutureMap;
    List<String> successfulSyncList = new ArrayList<String>();
    List<String> unSuccessfulSyncList = new ArrayList<String>();

    public ParallelSyncNodeOperator() {

    }

    public ParallelSyncNodeOperator(final List<String> synchFdnList, final HttpTool secureHttpTool) {
        this.executor = Executors.newFixedThreadPool(synchFdnList.size());
        this.synchFdnList = synchFdnList;
        this.secureHttpTool = secureHttpTool;
    }

    public boolean startSynchNodeProcess() throws Exception {
        final Map<String, Future<Boolean>> synchJobsFutureMap = startSyncJobsinParallel(executor, synchFdnList, secureHttpTool);
        return monitorSyncStatus(executor, synchJobsFutureMap);

    }

    private boolean monitorSyncStatus(final ExecutorService executor, final Map<String, Future<Boolean>> synchJobsFutureMap) throws Exception {
        executor.shutdown();
        return checkStatus(synchJobsFutureMap);
    }

    private boolean checkStatus(final Map<String, Future<Boolean>> synchJobFutureMap) throws Exception {
        final int totalSynchRequests = synchJobFutureMap.size();
        waitUntilAllFuturesDone(synchJobFutureMap);

        LOGGER.info("************************************************************************************");
        LOGGER.info("The total number of synch requests were [{}]", totalSynchRequests);
        LOGGER.info("The number of completed synch's are     [{}]", successfulSyncList.size());
        LOGGER.info("The number of incompleted synch's are   [{}]", unSuccessfulSyncList.size());

        boolean result = false;
        if (unSuccessfulSyncList.isEmpty()) {
            result = true;
        } else {
            LOGGER.info("The following nodes did not synch [{}] ", unSuccessfulSyncList);
            result = false;
        }

        LOGGER.info("************************************************************************************");
        return result;
    }

    /**
     * @param synchJobFutureMap
     */
    private void waitUntilAllFuturesDone(final Map<String, Future<Boolean>> synchJobFutureMap) {
        try {
            final int numberOfSynchActions = synchJobFutureMap.size();
            LOGGER.info("The number of synchs started is [{}]", numberOfSynchActions);
            while (!synchJobFutureMap.isEmpty()) {
                final Iterator<String> it = synchJobFutureMap.keySet().iterator();
                while (it.hasNext()) {
                    final String fdnKey = it.next();
                    final Future<Boolean> fut = synchJobFutureMap.get(fdnKey);
                    if (fut.isDone()) {
                        if (fut.get()) {
                            LOGGER.debug("{} did synch", fdnKey);
                            it.remove();
                            successfulSyncList.add(fdnKey);
                        } else {
                            LOGGER.debug("{} did not synch", fdnKey);
                            it.remove();
                            unSuccessfulSyncList.add(fdnKey);
                        }
                    } else {
                        LOGGER.debug("Fut key is not done {} ", fdnKey);
                    }
                }
                LOGGER.info("The number of synch requests        is   [{}]", numberOfSynchActions);
                LOGGER.info("The number of successful synch's    is   [{}]", successfulSyncList.size());
                LOGGER.info("The number of un-successful synch's is   [{}]", unSuccessfulSyncList.size());
                LOGGER.info("The number of outstanding synch's   is   [{}]", synchJobFutureMap.size());
                LOGGER.info("************************************************************************************");
                sleep(10000);
            }
        } catch (final Exception e) {
            LOGGER.error("Exception caught waiting for all synchs to complete : " + e.getLocalizedMessage());
            e.printStackTrace();

        }
    }

    private Map<String, Future<Boolean>> startSyncJobsinParallel(final ExecutorService executor, final List<String> synchFdnList,
                                                                 final HttpTool secureHttpTool) {

        LOGGER.info("**************************************************************************************************************");
        LOGGER.info("STARTING SYNC FOR {} NODE(s)", synchFdnList.size());
        LOGGER.info("**************************************************************************************************************");
        final Map<String, Future<Boolean>> map = new HashMap<String, Future<Boolean>>();

        for (int i = 0; i < synchFdnList.size(); i++) {
            final SyncNodeCallable callable = new SyncNodeCallable(synchFdnList.get(i), secureHttpTool);
            final Future<Boolean> future = executor.submit(callable);
            map.put(synchFdnList.get(i), future);
            sleep(610);
        }

        return map;

    }

    private void sleep(final long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
            LOGGER.error("Problem calling Thread sleep.", e);
        }
    }

}