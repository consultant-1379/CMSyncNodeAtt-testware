package com.ericsson.oss.test.util.verifier;

import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.BOOLEAN;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.BYTE;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.COMPLEX_REF;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.DOUBLE;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.ENUM_REF;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.INTEGER;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.LIST;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.LONG;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.MO_REF;
import static com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType.STRING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeAttributeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.test.util.common.NodeVersionMapper;
import com.ericsson.oss.test.util.common.dto.AttributeComparison;
import com.ericsson.oss.test.util.common.dto.CompareResponse;
import com.ericsson.oss.test.util.common.dto.NodeInfo;
import com.ericsson.oss.test.util.verifier.attribute.CdtAttributeVerifier;
import com.ericsson.oss.test.util.verifier.attribute.EnumAttributeVerifier;
import com.ericsson.oss.test.util.verifier.attribute.ListAttributeVerifier;
import com.ericsson.oss.test.util.verifier.attribute.MoRefAttributeVerifier;
import com.ericsson.oss.test.util.verifier.attribute.SimpleAttributeVerifier;
import com.ericsson.oss.test.util.verifier.helper.DpsHelper;
import com.ericsson.oss.test.util.verifier.helper.ModelServiceHelper;

public class MoVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoVerifier.class);

    public CompareResponse verify(final Map<String, Long> moRef, final NodeInfo nodeInfo, final Map<String, Object> netsimData, final String fdn) {
        final DpsHelper helper = new DpsHelper();
        final ManagedObject mo = helper.getMo(fdn);

        //TODO Throw exception and catch and return error response
        if (mo == null) {
            final CompareResponse resp = new CompareResponse();
            resp.setFdn(fdn);
            resp.setErrorMessage("MO does not exist in DPS. Hence attribute comparison was skipped for this MO.");
            resp.setModelAttributesFound(-1);
            resp.setDpsAttributesFound(-1);
            resp.setNetsimAttributesFound(-1);
            resp.setNonSyncableAttributesFound(-1);
            resp.setNodeName(nodeInfo.getNodeName());
            return resp;
        }
        final Map<String, Object> dpsAttributesFoFDN = mo.getAllAttributes();
        final ModelInfo modelInfo = getModelInfo(nodeInfo, fdn);

        final CompareResponse response = compareAttributes(moRef, fdn, netsimData, modelInfo, dpsAttributesFoFDN);
        response.setFdn(fdn);
        response.setNamespace(modelInfo.getNamespace());
        response.setVersion(modelInfo.getVersion().toString());
        response.setNodeName(nodeInfo.getNodeName());

        return response;
    }

    private CompareResponse compareAttributes(final Map<String, Long> moRef, final String fdn, final Map<String, Object> netSimData,
                                              final ModelInfo modelInfo, final Map<String, Object> dpsAttributesFoFDN) {

        final CompareResponse response = new CompareResponse();
        response.setFdn(fdn);

        final Collection<PrimaryTypeAttributeSpecification> allAttributeSpecs = getModelServiceHelper().getAttributeData(modelInfo);
        final Collection<PrimaryTypeAttributeSpecification> syncableAttributeSpecs = getModelServiceHelper().getSyncableAttributes(modelInfo);
        final Collection<PrimaryTypeAttributeSpecification> nonSyncableAttributeSpecs = getModelServiceHelper().getNonSyncableAttributes(modelInfo);

        final int totalMSAttributeCount = allAttributeSpecs.size();
        response.setModelAttributesFound(totalMSAttributeCount);
        response.setDpsAttributesFound(dpsAttributesFoFDN.size());
        response.setNetsimAttributesFound(netSimData.size());
        response.setModelName(getModelName(fdn));

        response.setNonSyncableAttributesFound(totalMSAttributeCount - syncableAttributeSpecs.size());

        compareAttributes(moRef, response, netSimData, dpsAttributesFoFDN, syncableAttributeSpecs);

        final Set<String> nonSyncablemsAttributeNames = getModelServiceAttributeNames(nonSyncableAttributeSpecs);
        final Set<String> msAttributeNames = getModelServiceAttributeNames(syncableAttributeSpecs);
        final Set<String> dpsAttributeNames = getAttributeNames(dpsAttributesFoFDN.keySet());
        final Set<String> netsimAttributeNames = getAttributeNames(netSimData.keySet());
        msAttributeNames.removeAll(nonSyncablemsAttributeNames);
        dpsAttributeNames.removeAll(nonSyncablemsAttributeNames);
        netsimAttributeNames.removeAll(nonSyncablemsAttributeNames);

        final Set<String> errors = new HashSet<String>();
        errors.addAll(compareAttributeNameSets(response, netsimAttributeNames, "NetSim", dpsAttributeNames, "DPS"));
        errors.addAll(compareAttributeNameSets(response, netsimAttributeNames, "NetSim", msAttributeNames, "ModelService"));
        errors.addAll(compareAttributeNameSets(response, msAttributeNames, "ModelService", dpsAttributeNames, "DPS"));

        addErrorsToResponseMsg(response, errors);

        return response;
    }

    private void addErrorsToResponseMsg(final CompareResponse responseMsg, final Set<String> errors) {
        final List<String> errorsWithoutDuplicates = removeDuplicates(errors);
        for (final String error : errorsWithoutDuplicates) {
            responseMsg.addError(new AttributeComparison(error, "", "", "", "", false));
        }
    }

    private List<String> removeDuplicates(final Set<String> listContainingDuplicates) {
        final List<String> setToReturn = new ArrayList<String>();
        final Set<String> set1 = new HashSet<String>();

        for (final String error : listContainingDuplicates) {
            final String errorMinusLastWord = error.replaceAll(" [^ ]+$", "");
            if (set1.add(errorMinusLastWord)) {
                setToReturn.add(error);
            } else {
                updateResultList(setToReturn, error);
            }
        }
        return setToReturn;
    }

    private void updateResultList(final List<String> resultList, final String error) {
        for (int i = 0; i < resultList.size(); i++) {

            final String resultEntry = resultList.get(i);
            final String errorMinusLastWord = error.replaceAll(" [^ ]+$", "");

            if (resultEntry.contains(errorMinusLastWord)) {
                final String[] words = error.split("\\s");
                final String lastWord = words[words.length - 1];
                final String oldEntry = resultList.get(i);
                resultList.set(i, oldEntry + " and " + lastWord);
            }
        }

    }

    private Set<String> getAttributeNames(final Set<String> attributeNames) {
        final Set<String> names = new HashSet<>();
        if (attributeNames != null) {
            for (final String name : attributeNames) {
                final String lowerName = name.toLowerCase();
                names.add(lowerName.toLowerCase());
            }
        }
        return names;
    }

    private Set<String> getModelServiceAttributeNames(final Collection<PrimaryTypeAttributeSpecification> attributeSpecs) {
        final Set<String> names = new HashSet<>();
        if (attributeSpecs != null) {
            for (final PrimaryTypeAttributeSpecification attributeSpec : attributeSpecs) {
                final String attName = attributeSpec.getName().toLowerCase();
                names.add(attName);
            }
        }
        return names;
    }

    private void compareAttributes(final Map<String, Long> moRef, final CompareResponse response, final Map<String, Object> netSimData,
                                   final Map<String, Object> dpsAttributesFoFDN, final Collection<PrimaryTypeAttributeSpecification> attributeSpecs) {
        for (final PrimaryTypeAttributeSpecification attributeSpec : attributeSpecs) {
            try {
                final Object dpsValue = dpsAttributesFoFDN.get(attributeSpec.getName());
                compareAttribute(response, attributeSpec, dpsValue, netSimData, moRef);
            } catch (final Exception e) {
                LOGGER.error("Problem compering attributes", e);
                response.setErrorMessage(String.format("Error while performing sync attribute comparison for attribute [%s] and error message[%s]:",
                        attributeSpec.getName(), e.getMessage()));
            }
        }
    }

    private void compareAttribute(final CompareResponse response, final PrimaryTypeAttributeSpecification attributeSpec, final Object dpsValue,
                                  final Map<String, Object> netSimData, final Map<String, Long> moRefs) {

        final DataType dataType = attributeSpec.getDataTypeSpecification().getDataType();
        final String attributeName = attributeSpec.getName();
        final Object netsimValue = netSimData.get(attributeSpec.getName().toLowerCase());
        final ModelInfo modelInfo = attributeSpec.getDataTypeSpecification().getReferencedDataType();

        if (dataType.equals(STRING) || dataType.equals(BYTE) || dataType.equals(INTEGER) || dataType.equals(LONG) || dataType.equals(DOUBLE)
                || dataType.equals(BOOLEAN)) {
            final SimpleAttributeVerifier attributeVerifier = new SimpleAttributeVerifier();
            attributeVerifier.compareSimpleDataTypeValue(response, attributeName, dataType, dpsValue, netsimValue);

        } else if (dataType.equals(ENUM_REF)) {
            final EnumAttributeVerifier attributeVerifier = new EnumAttributeVerifier();
            attributeVerifier.checkEnumAttributes(response, attributeName, modelInfo, dpsValue, netsimValue, null);

        } else if (dataType.equals(LIST)) {
            final ListAttributeVerifier attributeVerifier = new ListAttributeVerifier();
            attributeVerifier.compareListDataTypeValue(response, attributeSpec, dpsValue, netSimData, moRefs);

        } else if (dataType.equals(COMPLEX_REF)) {
            final CdtAttributeVerifier attributeVerifier = new CdtAttributeVerifier();
            attributeVerifier.checkComplexAttributes(response, attributeName, modelInfo, dpsValue, null, netSimData, moRefs, null);

        } else if (dataType.equals(MO_REF)) {
            final MoRefAttributeVerifier attributeVerifier = new MoRefAttributeVerifier();
            attributeVerifier.compareMoRef(response, attributeName, dpsValue, netsimValue, moRefs, dataType.toString());

        } else {
            throw new RuntimeException("Failed to compare because of unkown datatype " + dataType.toString());
        }
    }

    private Set<String> compareAttributeNameSets(final CompareResponse response, final Set<String> setA, final String nameA, final Set<String> setB,
                                                 final String nameB) {
        final Set<String> result = new HashSet<String>();
        if (!setA.isEmpty()) {
            for (final String name : setA) {
                if (!setB.contains(name)) {
                    result.add(name + " found in '" + nameA + "' but not in '" + nameB + "'");
                }
            }
        }
        if (!setB.isEmpty()) {
            for (final String name : setB) {
                if (!setA.contains(name)) {
                    result.add(name + " found in '" + nameB + "' but not in '" + nameA + "'");
                }
            }
        }
        return result;
    }

    private ModelInfo getModelInfo(final NodeInfo nodeInfo, final String fdn) {
        final String modelName = getModelName(fdn);
        String version = nodeInfo.getEnmVersion();
        final Map<String, String> nodeVersions = NodeVersionMapper.getNodeVersions(version);
        final ModelInfo modelInfo = getModelServiceHelper().getErbsModel(modelName, nodeVersions.get("ERBS"));
        if (modelInfo != null) {
            return modelInfo;
        }
        version = nodeVersions.get("CPP");
        return getModelServiceHelper().getCppModel(modelName, version);
    }

    private ModelServiceHelper getModelServiceHelper() {
        return ModelServiceHelper.getInstance();
    }

    private String getModelName(final String fdn) {
        final String[] fdnNames = fdn.split(",");
        String name = "";
        if (fdnNames != null && fdnNames.length > 0) {
            final String[] keyValue = fdnNames[fdnNames.length - 1].split("=");
            if (keyValue != null && keyValue.length > 0) {
                name = keyValue[0];
            }
        }
        return name;
    }
}
