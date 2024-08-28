package com.ericsson.oss.test.util.verifier.helper;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;

public class DpsHelper {

    private static DataPersistenceService dps = null;

    public DpsHelper() {
        if (dps == null) {
            dps = getDps();
        }
    }

    private DataPersistenceService getDps() {
        try {
            final InitialContext ctx = new InitialContext();
            return (DataPersistenceService) ctx.lookup("java:/datalayer/DataPersistenceService");
        } catch (final NamingException e) {
            throw new RuntimeException("Exception caught when looking up the DPS, stack trace: " + e);
        }
    }

    private DataBucket getLiveBucket() {
        return dps.getLiveBucket();
    }

    public ManagedObject getMo(final String fdn) {
        return getLiveBucket().findMoByFdn(fdn);
    }
}
