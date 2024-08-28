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
package com.ericsson.oss.cm.sync.test.operators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

public class SyncStatusOperator {

    private final Host hostSc1 = DataHandler.getHostByName("sc1");
    private final Host hostSc2 = DataHandler.getHostByName("sc2");
    private final CLICommandHelper cmdHelperSc1 = new CLICommandHelper(hostSc1);
    private final CLICommandHelper cmdHelperSc2 = new CLICommandHelper(hostSc2);

    public String getSyncTimesFromLogs(final Date startDate, final String nodeName) {

        final String statesStringSc1 = parseInfoFromLogs(cmdHelperSc1, nodeName, 0, startDate);
        final String statesStringSc2 = parseInfoFromLogs(cmdHelperSc2, nodeName, 1, startDate);

        final String[] singleLines = combineLogLines(statesStringSc1, statesStringSc2);
        final TreeMap<Date, String> dateToSyncStatusMap = createSyncStatusToDateMap(singleLines);
        if (dateToSyncStatusMap.size() != 4) {
            return "[***All Sync states not found***] - found = " + dateToSyncStatusMap;
        } else {
            final long totalSyncTime = dateToSyncStatusMap.lastKey().getTime() - dateToSyncStatusMap.firstKey().getTime();
            final Map<String, Long> statusToTimeTakenMap = createStatusToTimeTakenMap(dateToSyncStatusMap);
            return convertFromMapToString(statusToTimeTakenMap, totalSyncTime);
        }
    }

    private String[] combineLogLines(final String statesStringSc1, final String statesStringSc2) {
        final String[] sc1SingleLines = statesStringSc1.split("\\r?\\n");
        final String[] sc2SingleLines = statesStringSc2.split("\\r?\\n");
        return ArrayUtils.addAll(sc1SingleLines, sc2SingleLines);
    }

    private String parseInfoFromLogs(final CLICommandHelper cliServer, final String nodeName, final int server, final Date startDate) {

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String endDateString = createFormattedEndDate();

        final String REDIRECT_CMD = "cd /var/ericsson/log/jboss/MSCM_su_" + server + "_jee_cfg/ ";
        final String FILTER_TIME_RANGE_CMD = "sed -n '/" + dateFormat.format(startDate) + "/,/" + endDateString + "/p' server.log";
        final String FILTER_NODENAME_CMD = "grep -E 'syncStatus.*" + nodeName + "'";
        final String FILTER_MSG_CMD = "sed -e 's/DEBUG.*\\sto\\s//'";

        return cliServer.simpleExec(REDIRECT_CMD, FILTER_TIME_RANGE_CMD + " | " + FILTER_NODENAME_CMD + " | " + FILTER_MSG_CMD);
    }

    private TreeMap<Date, String> createSyncStatusToDateMap(final String[] singleLines) {
        final TreeMap<Date, String> dateToSyncStatusMap = new TreeMap<Date, String>();
        String status, dateAsString;
        int indexOfSyncState;
        Date date;

        for (final String line : singleLines) {
            indexOfSyncState = line.indexOf("'");
            if (indexOfSyncState != -1) {
                status = line.substring(indexOfSyncState + 1, line.length() - 1);
                dateAsString = line.substring(0, indexOfSyncState);
                date = parseDate(dateAsString);
                dateToSyncStatusMap.put(date, status);
            }
        }

        return dateToSyncStatusMap;
    }

    private Map<String, Long> createStatusToTimeTakenMap(final TreeMap<Date, String> dateToSyncStatusMap) {

        final Map<String, Long> statusToTimeTakenMap = new HashMap<String, Long>();
        final SortedSet<Date> dates = dateToSyncStatusMap.navigableKeySet();
        Date currentHead, newHead;
        String currentState;
        long timetaken, currentTime;

        while (dates.size() > 1) {
            currentHead = dates.first();
            currentState = dateToSyncStatusMap.get(currentHead);
            currentTime = currentHead.getTime();

            dates.remove(currentHead);
            newHead = dates.first();

            timetaken = newHead.getTime() - currentTime;
            statusToTimeTakenMap.put(currentState, timetaken);

        }
        return statusToTimeTakenMap;
    }

    private Date parseDate(final String dateAsString) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        try {
            return dateFormat.parse(dateAsString);
        } catch (final ParseException e) {
            throw new RuntimeException("Error parsing sync status from MSCM logs", e);
        }
    }

    private String convertFromMapToString(final Map<String, Long> mapOfStatesAndTimes, final long totalSyncTime) {
        final String toReturn = "Total Sync Time: " + totalSyncTime + "ms / PENDING state Time: " + mapOfStatesAndTimes.get("PENDING") + "ms / "
                + "TOPOLOGY state Time: " + mapOfStatesAndTimes.get("TOPOLOGY") + "ms / " + "ATTRIBUTE state Time: "
                + mapOfStatesAndTimes.get("ATTRIBUTE") + "ms.";
        return toReturn;

    }

    private String createFormattedEndDate() {

        final Date endDate = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        final String endDateString = dateFormat.format(endDate);
        final Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormat.parse(endDateString));
        } catch (final ParseException e) {
            throw new RuntimeException("Error parsing sync status from MSCM logs", e);
        }
        c.add(Calendar.MINUTE, 1);
        return dateFormat.format(c.getTime());
    }

}
