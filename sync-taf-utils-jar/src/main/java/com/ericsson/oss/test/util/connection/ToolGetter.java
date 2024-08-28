/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.test.util.connection;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.nms.host.HostConfigurator;
import com.ericsson.oss.test.util.common.constants.CommonConstants;

public class ToolGetter implements CommonConstants {

    private static Logger LOGGER = LoggerFactory.getLogger(ToolGetter.class);
    final private static ConcurrentMap<String, HttpTool> httpToolList = new ConcurrentHashMap<String, HttpTool>();

    /**
     * Returns an unsecure <code>HttpTool</code> object. A new <code>HttpTool</code> object will only be created if one does not already exist for the
     * host identified by the <code>restServiceHostPropertyName</code> parameter.
     * 
     * @param restServiceHostPropertyName
     *            - name of the system property that identifies the DNS name or ipAddress of the host to be used to access the CM Editor REST endpoint
     *            (JBOSS container or Apache server).
     * 
     * @return Instance of <code>HttpTool</code> object.
     * 
     */
    public synchronized HttpTool getUnsecureHttpTool(final String restServiceHostPropertyName) {

        LOGGER.info("Fetching HttpTool instance for: {}", restServiceHostPropertyName);
        HttpTool httpTool = httpToolList.get(restServiceHostPropertyName);
        if (httpTool == null) {
            LOGGER.debug("Host not cached");
            final Host host = getHost(restServiceHostPropertyName);
            httpTool = unsecureHttpToolBuilder(host);
            httpToolList.putIfAbsent(restServiceHostPropertyName, httpTool);
        }
        return httpTool;
    }

    /**
     * Returns an <code>Host</code> object using the <code>HostConfigurator</code> TAF Operator. A new <code>Host</code> object is created identified
     * by the <code>hostPropertyName</code> parameter.
     * 
     * Supports SC1, SC2, Apache Server (httpd_su0), and CMServ (cmserv_su0)
     * 
     * @param hostPropertyName
     *            - name of the system property that identifies the DNS name or ipAddress of the host to be used to access the CM Editor REST endpoint
     *            (JBOSS container or Apache server).
     * 
     * @return Instance of <code>Host</code> object.
     * 
     */
    public Host getHost(final String hostPropertyName) {
        LOGGER.debug("Trying to get host ... {}", hostPropertyName);

        Host host;

        switch (hostPropertyName) {
            case SC1_PROPERTY:
                host = HostConfigurator.getSC1();
                break;
            case SC2_PROPERTY:
                host = HostConfigurator.getSC2();
                break;
            case APACHE_JBOSS_INSTANCE:
                host = HostConfigurator.getApache();
                break;
            case CMSERV_0_JBOSS_INSTANCE:
                host = HostConfigurator.getCmService();
                break;
            default:
                host = DataHandler.getHostByName(hostPropertyName);
                break;
        }
        LOGGER.debug("{} host ip retrieved = {}, internal ip (if any) = {}", hostPropertyName, host.getIp(), host.getOriginalIp());
        return host;
    }

    private HttpTool unsecureHttpToolBuilder(final Host host) {
        LOGGER.info("Creating new unsecure HTTP session for: {}", host.getIp());
        return HttpToolBuilder.newBuilder(host).timeout(500).build();
    }

    public HttpTool secureHttpToolBuilder(final Host host) {
        LOGGER.info("Creating new secure HTTP session for {}", host.getIp());

        return HttpToolBuilder.newBuilder(host).useHttpsIfProvided(true).trustSslCertificates(true).followRedirect(true).build();
    }

    public void closeAllHttpTools() {
        LOGGER.debug("Closing All Http tools.");
        for (final Entry<String, HttpTool> httpToolListElement : httpToolList.entrySet()) {
            httpToolListElement.getValue().close();
        }
    }

}
