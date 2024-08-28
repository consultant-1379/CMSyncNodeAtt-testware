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
package com.ericsson.oss.test.util.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.test.util.common.constants.CommonConstants;
import com.ericsson.oss.test.util.common.dto.CompareResponse;
import com.ericsson.oss.test.util.reporting.templates.InputTableForFdn;
import com.ericsson.oss.test.util.reporting.templates.InputTableForNode;
import com.ericsson.oss.test.util.reporting.templates.MoCompareTable;
import com.ericsson.oss.test.util.reporting.templates.NodeCompareTable;
import com.ericsson.oss.test.util.reporting.templates.PageStart;
import com.ericsson.oss.test.util.reporting.templates.TotalAttributesTable;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class DisplayResultsUtility implements CommonConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayResultsUtility.class);

    public void logNodeComparisionResult(final String nodeName, final List<CompareResponse> compareResponseList) {
        LOGGER.info("Starting generation of attribute comparison report");
        setupFolderStructure();
        String result;

        final List<CompareResponse> failedResponses = new ArrayList<CompareResponse>();
        final List<CompareResponse> successResponses = new ArrayList<CompareResponse>();
        for (final CompareResponse resp : compareResponseList) {
            if (resp.isSuccess()) {
                successResponses.add(resp);
            } else {
                failedResponses.add(resp);
            }
        }
        for (final CompareResponse response : successResponses) {
            result = moComparisonResult(response);
            writePage(result, response.getId());
        }
        for (final CompareResponse response : failedResponses) {
            result = moComparisonResult(response);
            writePage(result, response.getId());
        }

        final String containerLog = createContainerTablePage(successResponses, failedResponses);
        final String fileName = PATH_TO_JCAT + "/" + nodeName + "-report.html";
        final File file = new File(fileName);
        try {
            final Writer writer = new FileWriter(file);
            writer.write(containerLog);
            writer.close();
        } catch (final Exception e) {

        }
        LOGGER.info("Report saved to {}   (<a href=\"{}-report.html\">link</a>)", fileName, nodeName);
    }

    private String moComparisonResult(final CompareResponse response) {
        final StringWriter result = new StringWriter();

        final MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("html/InputTableForFdn.mustache");
        final InputTableForFdn testInput = new InputTableForFdn(response);
        mustache.execute(result, testInput);

        mustache = mf.compile("html/TotalAttributesTable.mustache");
        final TotalAttributesTable totalAttributesFoundTable = new TotalAttributesTable(response);
        mustache.execute(result, totalAttributesFoundTable);

        mustache = mf.compile("html/MoCompareTable.mustache");
        final MoCompareTable resultAttributeCompareTable = new MoCompareTable(response);
        mustache.execute(result, resultAttributeCompareTable);

        return result.toString();
    }

    private String createContainerTablePage(final List<CompareResponse> successResponses, final List<CompareResponse> failedResponses) {

        final StringWriter result = new StringWriter();
        result.append(getCss());
        result.append("<div class=res-container>");

        final MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("html/InputTableForNode.mustache");
        CompareResponse responseData = null;
        if (!successResponses.isEmpty()) {
            responseData = successResponses.get(0);
        } else if (!failedResponses.isEmpty()) {
            responseData = failedResponses.get(0);
        }

        final int failedFdnComparisons = failedResponses.size();
        final int totalFdnsCompared = successResponses.size() + failedFdnComparisons;
        final InputTableForNode testInput = new InputTableForNode(responseData, totalFdnsCompared, failedFdnComparisons);
        mustache.execute(result, testInput);

        mustache = mf.compile("html/TotalAttributesTable.mustache");
        final int[] attributeTotalsArray = getAttributeTotals(successResponses, failedResponses);
        final TotalAttributesTable totalAttributesFoundTable = new TotalAttributesTable(attributeTotalsArray);
        mustache.execute(result, totalAttributesFoundTable);

        mustache = mf.compile("html/NodeCompareTable.mustache");
        final NodeCompareTable resultsComparisonContainer = new NodeCompareTable(successResponses, failedResponses);
        mustache.execute(result, resultsComparisonContainer);

        result.append("</div>");
        return result.toString();
    }

    private void writePage(final String data, final String pageId) {

        final String fileName = getFileNameAndPath(pageId);
        final File file = new File(fileName);
        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            final StringWriter result = new StringWriter();

            final MustacheFactory mf = new DefaultMustacheFactory();
            final Mustache mustache = mf.compile("html/PageStart.mustache");
            final PageStart pageStart = new PageStart();
            mustache.execute(result, pageStart);

            result.append(getCss());

            result.append(data);
            result.append("</body></html>");

            final Writer writer = new FileWriter(file);
            writer.write(result.toString());
            writer.close();

        } catch (final Exception e) {
            throw new RuntimeException("Exception thrown:" + e.getMessage());
        }
    }

    private void setupFolderStructure() {
        new File(PATH_TO_JCAT + "/compare/").mkdirs();
    }

    private String getCss() {

        final StringBuilder result = new StringBuilder();
        final InputStream in = this.getClass().getResourceAsStream("/html/style.css");
        result.append(getStringFromInputStream(in));
        return result.toString();
    }

    private String getStringFromInputStream(final InputStream is) {

        BufferedReader br = null;
        final StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (final IOException e) {
            LOGGER.error("Problem displaying results.", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException e) {
                    LOGGER.error("Problem closing Buffer Reader while working on displaying results.", e);
                }
            }
        }

        return sb.toString();

    }

    private String getFileNameAndPath(final String pageId) {
        return (PATH_TO_JCAT + "/compare/" + pageId + ".html");
    }

    private int[] getAttributeTotals(final Iterable<CompareResponse> successList, final Iterable<CompareResponse> failureList) {
        int pm = 0, model = 0, netsim = 0, dps = 0;

        for (final CompareResponse resp : successList) {
            dps += resp.getDpsAttributesFound();
            model += resp.getModelAttributesFound();
            netsim += resp.getNetsimAttributesFound();
            pm += resp.getNotSyncableAttributesFound();
        }
        for (final CompareResponse resp : failureList) {
            dps += resp.getDpsAttributesFound();
            model += resp.getModelAttributesFound();
            netsim += resp.getNetsimAttributesFound();
            pm += resp.getNotSyncableAttributesFound();
        }
        return new int[] { model, pm, netsim, dps };
    }

}
