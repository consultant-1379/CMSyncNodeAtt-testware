package com.ericsson.oss.test.util.verifier.helper;

import java.util.*;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.ReadBehavior;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.WriteBehavior;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeAttributeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeSpecification;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.ModelServiceImpl;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.TypedModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeAttributeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.cdt.ComplexDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;

public class ModelServiceHelper {

    private static ModelServiceHelper msHelperInstance = null;
    public static final String FDN_NAME_URN_TEMPLATE = "/%s/%s/%s/%s";
    private final ModelService service;
    private final String ERBS_NODE_MODEL = "ERBS_NODE_MODEL";
    private final String CPP_NODE_MODEL = "CPP_NODE_MODEL";
    private final String schema = "dps_primarytype";

    private final TypedModelAccess typedAccess;

    private final ModelMetaInformation metaInfoAccess;

    private ModelServiceHelper() {
        service = new ModelServiceImpl();
        typedAccess = service.getTypedAccess();
        metaInfoAccess = service.getModelMetaInformation();
    }

    public static ModelServiceHelper getInstance() {
        if (msHelperInstance == null) {
            msHelperInstance = new ModelServiceHelper();
        }
        return msHelperInstance;
    }

    public ModelInfo getErbsModel(final String name, final String version) {
        final String modelurn = String.format(FDN_NAME_URN_TEMPLATE, schema, ERBS_NODE_MODEL, name, version);
        final Collection<ModelInfo> moInfos = metaInfoAccess.getModelsFromUrn(modelurn);
        if (moInfos.isEmpty()) {
            return null;
        }
        return moInfos.iterator().next();
    }

    public ModelInfo getCppModel(final String name, final String version) {
        final String modelurn = String.format(FDN_NAME_URN_TEMPLATE, schema, CPP_NODE_MODEL, name, version);
        final Collection<ModelInfo> moInfos = metaInfoAccess.getModelsFromUrn(modelurn);
        if (moInfos.isEmpty()) {
            return null;
        }
        return moInfos.iterator().next();
    }

    public Collection<ModelInfo> getModelsFromUrn(final String schema, final String nameSpace, final String name, final String version) {

        return metaInfoAccess.getModelsFromUrn(String.format(FDN_NAME_URN_TEMPLATE, schema, "*", name, version));

    }

    public Collection<PrimaryTypeAttributeSpecification> getAttributeData(final ModelInfo modelInfo) {

        final PrimaryTypeSpecification spec = typedAccess.getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);

        return spec.getNonInheritedAttributeSpecifications();
    }

    public Collection<PrimaryTypeAttributeSpecification> getSyncableAttributes(final ModelInfo modelInfo) {

        final Collection<PrimaryTypeAttributeSpecification> filteredList = new ArrayList<PrimaryTypeAttributeSpecification>();

        final PrimaryTypeSpecification eMobelSpecifications = typedAccess.getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);
        final Collection<PrimaryTypeAttributeSpecification> nonInheritedAttributeSpecifications = eMobelSpecifications.getNonInheritedAttributeSpecifications();

        final PrimaryTypeSpecification primaryTypeSpecification = typedAccess.getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);

        for (final PrimaryTypeAttributeSpecification attSpec : nonInheritedAttributeSpecifications) {

            if (isAttributeSyncable(attSpec, primaryTypeSpecification)) {
                filteredList.add(attSpec);
            }
        }

        return filteredList;
    }

    public Collection<PrimaryTypeAttributeSpecification> getNonSyncableAttributes(final ModelInfo modelInfo) {

        final Collection<PrimaryTypeAttributeSpecification> filteredList = new ArrayList<PrimaryTypeAttributeSpecification>();

        final PrimaryTypeSpecification eMobelSpecifications = typedAccess.getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);
        final Collection<PrimaryTypeAttributeSpecification> nonInheritedAttributeSpecifications = eMobelSpecifications.getNonInheritedAttributeSpecifications();

        final PrimaryTypeSpecification primaryTypeSpecification = typedAccess.getEModelSpecification(modelInfo, PrimaryTypeSpecification.class);

        for (final PrimaryTypeAttributeSpecification attSpec : nonInheritedAttributeSpecifications) {

            if (!isAttributeSyncable(attSpec, primaryTypeSpecification)) {
                filteredList.add(attSpec);
            }
        }

        return filteredList;
    }

    public Map<PrimaryTypeAttributeSpecification, Object> getDefaultValues(final Collection<PrimaryTypeAttributeSpecification> attributeSpecList) {
        for (final PrimaryTypeAttributeSpecification attSpec : attributeSpecList) {

            attSpec.getDefaultValue();
        }
        return null;
    }

    public Collection<ComplexDataTypeAttributeSpecification> getComplexTypeAttributes(final ModelInfo modelInfo) {

        final ComplexDataTypeSpecification complexSpec = typedAccess.getEModelSpecification(modelInfo, ComplexDataTypeSpecification.class);
        return complexSpec.getAllAttributeSpecifications();
    }

    public EnumDataTypeSpecification getEnumTypeAttributes(final ModelInfo modelInfo) {

        final EnumDataTypeSpecification eModelSpecification = typedAccess.getEModelSpecification(modelInfo, EnumDataTypeSpecification.class);
        return eModelSpecification;
    }

    private boolean isAttributeSyncable(final PrimaryTypeAttributeSpecification attSpec, final PrimaryTypeSpecification moInfoSpec) {

        final WriteBehavior writeBehToCheck = isAttributeInherited(attSpec.getWriteBehavior()) ? moInfoSpec.getWriteBehavior() : attSpec.getWriteBehavior();
        final ReadBehavior readBehToCheck = isAttributeInherited(attSpec.getReadBehavior()) ? moInfoSpec.getReadBehavior() : attSpec.getReadBehavior();
        return isAttributePersistant(writeBehToCheck, readBehToCheck);
    }

    private boolean isAttributeInherited(final WriteBehavior writeBehavior) {
        return writeBehavior.equals(WriteBehavior.INHERITED);
    }

    private boolean isAttributeInherited(final ReadBehavior readBehavior) {
        return readBehavior.equals(ReadBehavior.INHERITED);
    }

    private boolean isAttributePersistant(final WriteBehavior writeBehavior, final ReadBehavior readBehavior) {
        return (!readBehavior.equals(ReadBehavior.FROM_DELEGATE)) || (!writeBehavior.equals(WriteBehavior.NOT_ALLOWED) && !writeBehavior.equals(WriteBehavior.DELEGATE));
    }

}
