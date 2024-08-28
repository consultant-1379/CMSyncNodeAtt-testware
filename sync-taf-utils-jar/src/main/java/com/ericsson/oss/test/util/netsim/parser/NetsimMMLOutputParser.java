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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetsimMMLOutputParser {

    // dosnt work, need to be careful & check what happens in Linux env
    //private static final String NEW_LINE   = System.lineSeparator();
	private static final Logger LOGGER = LoggerFactory.getLogger(NetsimMMLOutputParser.class);
	
    private static final String NEW_LINE_LINUX = "\n";
    private static final String NEW_LINE_WIN = "\r\n";

    private static final String SEQ_OF_STRUCTS = "[{";

    private static final String EQUALS = "=";
    static final String COMMA = ",";
    private static final char SPACE_CHAR = ' ';

    /**
     * Returns a parse interpretation of MML output.
     * 
     * @param MML
     *            text output representing attributes of an MO
     * @return Map<String, Object> keys are the attribute names. Currently the values in the Map may be: 1) String if the attribute is a simple attribute; regardless of attribute type. 2) Map<String,
     *         String> is the attribute is a struct 3) List<String> if the attribute is a sequence; regardles of attribute type. 4) List<Map<String,String>> if the attribute is a sequence of structs
     * 
     */
    public static Map<String, Object> parseMMLOutput(final String input, final boolean namesInLowerCase) throws NetsimMMLOutputParseException {

        final Map<String, Object> allAtts = new HashMap<>();
        final Stack<Attribute> stack = new Stack<>();

        final String newLineString = getCorrectNewLineString(input);
        final List<String> attLines = cleanUpInput(input, newLineString);
        
        int stackTopAttIndentationCount = -1;
        Attribute top = null;

        final Iterator<String> it = attLines.iterator();
        while(it.hasNext()) {
        	final String attLine = it.next();
        	
            if (Utils.isEmpty(attLine) || attLine.startsWith(">> dumpmotree:moid=") || attLine.startsWith("Number of MOs:") || !attLine.startsWith(" ")) {
            	continue;
            }

            final int currentIndentationCount = countIndentation(attLine);

            LOGGER.debug("processing line: '{}'", attLine);
            
            final int equalsPosition = attLine.indexOf(EQUALS);
            if (equalsPosition < 1) {
                throw new NetsimMMLOutputParseException("attribute line should be in format 'name=value', got: '" + attLine + "'");
            }
            final String nameBit = attLine.substring(0, equalsPosition);
            final String valueBit = attLine.substring(equalsPosition + 1);

            final String name = cleanName(nameBit.trim(), namesInLowerCase);
            final Object value = getValue(valueBit, namesInLowerCase);

            final Attribute att = new Attribute(currentIndentationCount, name, value);

            // only for first attribute
            if (!stack.isEmpty()) {
                top = stack.peek();
                stackTopAttIndentationCount = top.getIndentation();
            }

            if (top == null) {
                stack.push(att);
            } else {

                if (currentIndentationCount > stackTopAttIndentationCount) {
                    stack.push(att);
                } else if (currentIndentationCount <= stackTopAttIndentationCount) {

                    // have left a struct, pop everything with a higher or equal indentation and attach ur self to the new top then push onto stack
                	Attribute topAtt = null;
                	while (true) {

                        if (stack.isEmpty()) {
                            break;
                        }

                        topAtt = stack.peek();
                        if (topAtt.getIndentation() < currentIndentationCount) {
                            topAtt.addValueToMap(att.getName(), att.getValue()); // add the current att as the stack top is the parent
                            //temp.setValue(att.getValue());
                            break;
                        } else if (topAtt.getIndentation() == 1) {
                            // only top level attributes i.e. indentation of one are independent
                            allAtts.put(topAtt.getName(), topAtt.getValue());
                        }

                        stack.pop();  // popping struct member off the stack

                        if (!stack.isEmpty()) {
                            final Attribute newTop = stack.peek(); // actual struct
                            if (topAtt.getIndentation() > newTop.getIndentation()) {
                                newTop.addValueToMap(topAtt.getName(), topAtt.getValue());  // add what was popped off four lines ago to the struct
                            }
                        }
                    }
                    
                    // the attribute is on the stack in case it self is a struct root 
                    if(it.hasNext() || ( (topAtt != null && att.getIndentation() == topAtt.getIndentation())||(att.getIndentation() == 1))) {
                    	stack.push(att);
                    }
                }
            }
        }

        // just empty out the rest of the stack
        while (true) {

            if (stack.isEmpty()) {
                break;
            }
            final Attribute temp = stack.pop();
            LOGGER.debug("--> stack not empty... popping: {}", temp.getName());
            allAtts.put(temp.getName(), temp.getValue());
        }

        return allAtts;
    }

    /**
     * some attributes are split over multiple lines, so far these lines end with a ','
     * 
     * @param input
     * @return
     */
    public static List<String> cleanUpInput(final String input, final String newLineString) {
        final String[] bits = input.split(newLineString);
        final List<String> lines = new ArrayList<>();
        for (final String line : bits) {
        	if(!Utils.isEmpty(line)) {
        		lines.add(line);
        	}
        }
        
        final List<String> toBeRemoved = new LinkedList<>();
        boolean haveCommaEndingLine = false;
        for (final String line : lines) {
            if (line.endsWith(",")) {
                toBeRemoved.add(line);
                haveCommaEndingLine = true;
            } else if (haveCommaEndingLine) {
                toBeRemoved.add(line);
                haveCommaEndingLine = false;
            }
        }

        final StringBuilder newAttLine = new StringBuilder();
        for (final String line : toBeRemoved) {
            newAttLine.append(line);
        }
        lines.removeAll(toBeRemoved);
        
        String newAttributeLine = newAttLine.toString();
        if(!Utils.isEmpty(newAttributeLine)) {
        	lines.add(newAttributeLine);
        }
        return lines;
    }

    private static String getCorrectNewLineString(final String input) {
        if (input.indexOf(NEW_LINE_WIN) > 0) {
            return NEW_LINE_WIN;
        } else {
            return NEW_LINE_LINUX;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object getValue(final String value, final boolean namesInLowerCase) {
        if (value == null) {
            return "";
        }
        final String val = Utils.isEmpty(value) ? null : value;
        List list = null;
        if (val != null && val.indexOf(COMMA) > 0) {
            list = new LinkedList<>();
            if (val.startsWith(SEQ_OF_STRUCTS)) {
                list = convertToSeqOffStructs(val, namesInLowerCase);
            } else {
                final String[] arr = val.split(COMMA);
                for (final String v : arr) {
                    list.add(cleanValue(v));
                }
            }
        }
        if (list != null) {
            return list;
        } else {
            return cleanValue(val);
        }
    }

    private static List<Map<String, String>> convertToSeqOffStructs(final String val, final boolean namesInLowerCase) {
        final String data = val.substring(1, val.length() - 1);
        final String[] bits = data.split("\\]\\,\\[");
        final List<Map<String, String>> list = new LinkedList<>();

        for (final String bit : bits) {

            final String subData = bit.substring(1, bit.length() - 1);
            final String subBits[] = subData.split("\\}\\,[ ]*\\{");

            final Map<String, String> struct = new HashMap<>();
            for (final String subBit : subBits) {
                final int equalsPosition = subBit.indexOf(COMMA);

                final String nameBit = subBit.substring(0, equalsPosition);
                final String valueBit = subBit.substring(equalsPosition + 1);
                final String name = cleanName(nameBit, namesInLowerCase);

                struct.put(name, cleanValue(valueBit));
            }
            list.add(struct);
        }
        return list;
    }

    static int countIndentation(final String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (SPACE_CHAR == line.charAt(i)) {
                count++;
            } else {
                return count;
            }
        }
        return count;
    }

    private static String cleanName(final String name, final boolean namesInLowerCase) {
        return (namesInLowerCase) ? name.toLowerCase() : name;
    }

    private static String cleanValue(final String value) {
        if (value == null || "null".equals(value) || "\"\"".equals(value)) {
            return "";
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
