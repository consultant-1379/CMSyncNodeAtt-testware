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
package com.ericsson.oss.test.util.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NodeVersionMapper {

    public final static Map<String, HashMap<String, String>> versionsMapper = new HashMap<String, HashMap<String, String>>();

    static {
        HashMap<String, String> nodeVersions = new HashMap<String, String>();
        nodeVersions.put("CPP", "3.12.0");
        nodeVersions.put("ERBS", "3.1.72");
        versionsMapper.put("Legacy", nodeVersions);

        nodeVersions = new HashMap<String, String>();
        nodeVersions.put("CPP", "13.98.16");
        nodeVersions.put("ERBS", "4.1.44");
        versionsMapper.put("13A", nodeVersions);

        nodeVersions = new HashMap<String, String>();
        nodeVersions.put("CPP", "13.112.4");
        nodeVersions.put("ERBS", "4.1.189");
        versionsMapper.put("13B", nodeVersions);

        nodeVersions = new HashMap<String, String>();
        nodeVersions.put("CPP", "14.125.5");
        nodeVersions.put("ERBS", "5.1.63");
        versionsMapper.put("14A", nodeVersions);

        nodeVersions = new HashMap<String, String>();
        nodeVersions.put("CPP", "14.139.5");
        nodeVersions.put("ERBS", "5.1.200");
        versionsMapper.put("14B", nodeVersions);
    }

    public static String getCppVersionFromErbsVersion(final String erbsVersion) {
        for (final Entry<String, HashMap<String, String>> entry : versionsMapper.entrySet()) {
            final Map<String, String> nodeVersions = entry.getValue();
            if (nodeVersions.get("ERBS").equals(erbsVersion)) {
                return nodeVersions.get("CPP");
            }
        }
        return null;
    }

    public static String getErbsVersionFromCppVersion(final String cppVersion) {
        for (final Entry<String, HashMap<String, String>> entry : versionsMapper.entrySet()) {
            final Map<String, String> nodeVersions = entry.getValue();
            if (nodeVersions.get("CPP").equals(cppVersion)) {
                return nodeVersions.get("ERBS");
            }
        }
        return null;
    }

    public static String getEnmReleaseVersionFromCppVersion(final String cppVersion) {
        for (final Entry<String, HashMap<String, String>> entry : versionsMapper.entrySet()) {
            final Map<String, String> nodeVersions = entry.getValue();
            if (nodeVersions.get("CPP").equals(cppVersion)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getEnmReleaseVersionFromErbsVersion(final String erbsVersion) {
        for (final Entry<String, HashMap<String, String>> entry : versionsMapper.entrySet()) {
            final Map<String, String> nodeVersions = entry.getValue();
            if (nodeVersions.get("ERBS").equals(erbsVersion)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map<String, String> getNodeVersions(final String enmReleaseVersion) {
        return versionsMapper.get(enmReleaseVersion);
    }

    public static boolean isValidCppVersion(final String cppVersion) {
        for (final Entry<String, HashMap<String, String>> entry : versionsMapper.entrySet()) {
            final Map<String, String> nodeVersions = entry.getValue();
            if (nodeVersions.get("CPP").equals(cppVersion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidErbsVersion(final String erbsVersion) {
        for (final Entry<String, HashMap<String, String>> entry : versionsMapper.entrySet()) {
            final Map<String, String> nodeVersions = entry.getValue();
            if (nodeVersions.get("ERBS").equals(erbsVersion)) {
                return true;
            }
        }
        return false;
    }
}
