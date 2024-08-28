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
package com.ericsson.oss.test.util.netsim.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class NetsimMMLDeepScopeOutputParser {

    public static Map<String, Map<String, Object>> parseMMLOutput(final String input, final String nodeName) throws NetsimMMLOutputParseException {

        final String newLineString = Utils.getCorrectNewLineString(input);
        final String[] attLines = input.split(newLineString);

        final Map<String, Map<String, Object>> mos = new HashMap<>();
        final List<String> storedLines = new LinkedList<>();

        final Stack<Attribute> stack = new Stack<>();
        String idLine = null;

        boolean lineIsIdLine = true;
        int lastIndentationCount = -1;
        String currentFdn = null;
        for (final String attLine : attLines) {

            if (attLine.startsWith(">> dumpmotree:")) {
                continue;
            }

            if (Utils.isEmpty(attLine)) {
                lineIsIdLine = true;
                final String dataLine = removeIndentation(storedLines, newLineString);
                final Map<String, Object> data = NetsimMMLOutputParser.parseMMLOutput(dataLine, true);
                mos.put("MeContext=" + nodeName + "," + currentFdn, data);
            } else {

                if (lineIsIdLine) {
                    final int indentation = NetsimMMLOutputParser.countIndentation(attLine);
                    storedLines.clear();
                    storedLines.add(attLine);

                    idLine = attLine.trim();

                    final Attribute currAtt = new Attribute(indentation, idLine.trim(), null);
                    lineIsIdLine = false;
                    if (indentation > lastIndentationCount) {
                        stack.push(currAtt);
                    } else if (indentation <= lastIndentationCount) {

                        while (true) {
                            final Attribute top = getStackTop(stack);
                            if (top == null) {
                                break;
                            }

                            if (top != null) {
                                if (top.getIndentation() >= indentation) {
                                    stack.pop();
                                } else {
                                    break;
                                }
                            }
                        }
                        stack.push(currAtt);
                    }

                    currentFdn = buildFdn(stack, currAtt);
                    lastIndentationCount = indentation;
                } else {
                    storedLines.add(attLine);
                }
            }
        }
        return mos;
    }

    /**
     * @param lines
     * @return
     */
    private static String removeIndentation(final List<String> lines, final String eof) {
        final StringBuilder sb = new StringBuilder();
        final String first = lines.get(0);
        final int maxIndex = NetsimMMLOutputParser.countIndentation(first);
        for (final String line : lines) {
            int index = NetsimMMLOutputParser.countIndentation(line);
            if (index > maxIndex) {
                index = maxIndex;
            }
            sb.append(line.substring(index));
            sb.append(eof);
        }
        return sb.toString();
    }

    private static Attribute getStackTop(final Stack<Attribute> stack) {
        if (!stack.isEmpty()) {
            return stack.peek();
        }
        return null;
    }

    private static String buildFdn(final Stack<Attribute> stack, final Attribute currentAtt) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            final Attribute a = stack.get(i);
            if (a.getIndentation() < currentAtt.getIndentation()) {
                sb.append(a.getName());
                if (i < stack.size() - 1) {
                    sb.append(NetsimMMLOutputParser.COMMA);
                }
            } else {
                break;
            }
        }

        if (sb.toString().lastIndexOf(NetsimMMLOutputParser.COMMA) != (sb.length() - 1)) {
            sb.append(NetsimMMLOutputParser.COMMA);
        }
        sb.append(currentAtt.getName());
        return sb.toString();
    }
}
