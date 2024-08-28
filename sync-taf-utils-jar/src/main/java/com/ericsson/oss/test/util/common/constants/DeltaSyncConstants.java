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
package com.ericsson.oss.test.util.common.constants;

public interface DeltaSyncConstants {

    String MANAGED_ELEMENT_FDN = "ManagedElement=1";
    String ENODEB_FUNCTION_FDN = MANAGED_ELEMENT_FDN + ",ENodeBFunction=1";
    String TRANSPORT_NETWORK_FDN = MANAGED_ELEMENT_FDN + ",TransportNetwork=1";
    String CPP_CONN_INFO_MO_NAME = "CppConnectivityInformation";
    String CM_NODE_HB_SUPERVISION_MO_NAME = "CmNodeHeartbeatSupervision";

    String GEN_COUNTER_ATTR_NAME = "generationCounter";
    String RESTART_DATE_ATTR_NAME = "restartTimestamp";
    String ACTIVE_ATTR_NAME = "active";

    String RIVENDELL_MO_NAME = "Rivendell";
    String SCTP = "Sctp";
    String USER_LABEL = "userLabel";
    String GANDALF = "Gandalf";

    String EUTRANCELLFDD = "EUtranCellFDD";
    String SECTOR_CARR_REF_ATTRIBUTE_NAME = "sectorCarrierRef";
    String SECTOR_CARR_REF_ATTRIBUTE_VALUE = "[379,380,381,382]";

    String EUTRAN_CELL_POLYGON_ATTRIBUTE_NAME = "eutranCellPolygon ([struct], EutranCellCorner)";
    String EUTRAN_CELL_POLYGON_ATTRIBUTE_VALUE = "[[" + 1 + "," + 2 + "],[" + 3 + "," + 4 + "]]";

}
