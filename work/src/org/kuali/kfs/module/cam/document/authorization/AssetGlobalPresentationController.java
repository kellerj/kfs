/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.cam.document.authorization;

import java.util.HashSet;
import java.util.Set;

import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail;
import org.kuali.kfs.module.cam.document.service.AssetGlobalService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentPresentationControllerBase;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.datadictionary.MaintainableCollectionDefinition;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.service.MaintenanceDocumentDictionaryService;

/**
 * Presentation Controller for Asset Global Maintenance Documents
 */
public class AssetGlobalPresentationController extends FinancialSystemMaintenanceDocumentPresentationControllerBase {

    @Override
    public Set<String> getConditionallyHiddenPropertyNames(BusinessObject businessObject) {
        Set<String> fields = super.getConditionallyHiddenPropertyNames(businessObject);

        MaintenanceDocument document = (MaintenanceDocument) businessObject;
        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();

        MaintainableCollectionDefinition maintCollDef = SpringContext.getBean(MaintenanceDocumentDictionaryService.class).getMaintainableCollection("AssetGlobalMaintenanceDocument", "assetPaymentDetails");
        if (assetGlobal.isCapitalAssetBuilderOriginIndicator() || SpringContext.getBean(AssetGlobalService.class).isAssetSeparateDocument(assetGlobal)) {
            // do not include payment add section within the payment details collection
            maintCollDef.setIncludeAddLine(false);
        }
        else {
            maintCollDef.setIncludeAddLine(true);
        }

        if (!SpringContext.getBean(AssetGlobalService.class).isAssetSeparateByPaymentDocument(assetGlobal)) {
            // Show payment sequence number field only if a separate by payment was selected
            fields.add(CamsPropertyConstants.AssetGlobal.SEPERATE_SOURCE_PAYMENT_SEQUENCE_NUMBER);
        }
        if (!SpringContext.getBean(AssetGlobalService.class).isAssetSeparateDocument(assetGlobal)) {
            fields.addAll(getAssetGlobalLocationHiddenFields(assetGlobal));

            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_YEAR);
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_MONTH);

            if (SpringContext.getBean(AssetGlobalService.class).existsInGroup(CamsConstants.AssetGlobal.NON_NEW_ACQUISITION_CODE_GROUP, assetGlobal.getAcquisitionTypeCode())) {
                // Fields in the add section
                fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_DATE);
                fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_NUMBER);
                fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_TYPE_CODE);
                fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "." + CamsPropertyConstants.AssetPaymentDetail.ORIGINATION_CODE);

                // Hiding some fields when the status of the document is not final.
                if (!document.getDocumentHeader().getWorkflowDocument().stateIsFinal()) {
                    fields.addAll(getAssetGlobalPaymentsHiddenFields(assetGlobal));
                }
            }
        }

        return fields;
    }

    @Override
    public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
        Set<String> fields = super.getConditionallyReadOnlyPropertyNames(document);

        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();

        // "Asset Separate" document functionality
        if (SpringContext.getBean(AssetGlobalService.class).isAssetSeparateDocument(assetGlobal)) {
            fields.addAll(getAssetGlobalDetailsReadOnlyFields());
            fields.addAll(getAssetGlobalPaymentsReadOnlyFields(assetGlobal));
        }
        else if (assetGlobal.isCapitalAssetBuilderOriginIndicator()) {
            // If asset global document is created from CAB, disallow add payment to collection.
            fields.addAll(getAssetGlobalPaymentsReadOnlyFields(assetGlobal));
        }

        return fields;
    }

    @Override
    public Set<String> getConditionallyHiddenSectionIds(BusinessObject businessObject) {
        Set<String> fields = super.getConditionallyHiddenSectionIds(businessObject);

        MaintenanceDocument document = (MaintenanceDocument) businessObject;
        AssetGlobal assetGlobal = (AssetGlobal) document.getNewMaintainableObject().getBusinessObject();

        // hide "Asset Information", "Recalculate Total Amount" tabs if not "Asset Separate" doc
        if (!SpringContext.getBean(AssetGlobalService.class).isAssetSeparateDocument(assetGlobal)) {
            fields.add(CamsConstants.AssetGlobal.SECTION_ID_ASSET_INFORMATION);
            fields.add(CamsConstants.AssetGlobal.SECTION_ID_RECALCULATE_SEPARATE_SOURCE_AMOUNT);
        }

        return fields;
    }

    /**
     * @param assetGlobal
     * @return Asset Location fields with index that are present on AssetGlobal. Includes add line. Useful for hiding them.
     */
    protected Set<String> getAssetGlobalLocationHiddenFields(AssetGlobal assetGlobal) {
        Set<String> fields = new HashSet<String>();

        // hide it for the add line
        int i = 0;
        for (AssetGlobalDetail assetSharedDetail : assetGlobal.getAssetSharedDetails()) {
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.REPRESENTATIVE_UNIVERSAL_IDENTIFIER); // representativeUniversalIdentifier
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.CAPITAL_ASSET_TYPE_CODE); // capitalAssetTypeCode
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.CAPITAL_ASSET_DESCRIPTION); // capitalAssetDescription
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.MANUFACTURER_NAME); // manufacturerName
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.ORGANIZATION_TEXT); // organizationText
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.MANUFACTURER_MODEL_NUMBER); // manufacturerModelNumber
            fields.add(KFSConstants.ADD_PREFIX + "." + CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "." + CamsPropertyConstants.AssetGlobalDetail.SEPARATE_SOURCE_AMOUNT); // separateSourceAmount
            // (Long)

            // hide it for the existing lines
            int j = 0;
            for (AssetGlobalDetail assetGlobalUniqueDetail : assetSharedDetail.getAssetGlobalUniqueDetails()) {
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.REPRESENTATIVE_UNIVERSAL_IDENTIFIER); // representativeUniversalIdentifier
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.CAPITAL_ASSET_TYPE_CODE); // capitalAssetTypeCode
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.CAPITAL_ASSET_DESCRIPTION); // capitalAssetDescription
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.MANUFACTURER_NAME); // manufacturerName
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.ORGANIZATION_TEXT); // organizationText
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.MANUFACTURER_MODEL_NUMBER); // manufacturerModelNumber
                fields.add(CamsPropertyConstants.AssetGlobal.ASSET_SHARED_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetGlobalDetail.ASSET_UNIQUE_DETAILS + "[" + j + "]." + CamsPropertyConstants.AssetGlobalDetail.SEPARATE_SOURCE_AMOUNT); // separateSourceAmount
                // (Long)
                j++;
            }

            i++;
        }

        return fields;
    }

    /**
     * @param assetGlobal
     * @return posting year and fiscal month on every payment. Useful for hiding them.
     */
    protected Set<String> getAssetGlobalPaymentsHiddenFields(AssetGlobal assetGlobal) {
        Set<String> fields = new HashSet<String>();

        int i = 0;
        for (AssetPaymentDetail assetPaymentDetail : assetGlobal.getAssetPaymentDetails()) {
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_DATE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_YEAR);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_MONTH);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_NUMBER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_TYPE_CODE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.ORIGINATION_CODE);
            i++;
        }

        return fields;
    }

    /**
     * @return Asset Global Detail fields that should be read only
     */
    protected Set<String> getAssetGlobalDetailsReadOnlyFields() {
        Set<String> fields = new HashSet<String>();

        fields.add(CamsPropertyConstants.Asset.ORGANIZATION_OWNER_CHART_OF_ACCOUNTS_CODE);
        fields.add(CamsPropertyConstants.Asset.ORGANIZATION_OWNER_ACCOUNT_NUMBER);
        fields.add(CamsPropertyConstants.Asset.AGENCY_NUMBER); // owner
        fields.add(CamsPropertyConstants.Asset.ACQUISITION_TYPE_CODE);
        fields.add(CamsPropertyConstants.Asset.INVENTORY_STATUS_CODE);
        fields.add(CamsPropertyConstants.Asset.CONDITION_CODE);
        fields.add(CamsPropertyConstants.AssetGlobal.CAPITAL_ASSET_DESCRIPTION);
        fields.add(CamsPropertyConstants.Asset.CAPITAL_ASSET_TYPE_CODE);
        fields.add(CamsPropertyConstants.Asset.VENDOR_NAME);
        fields.add(CamsPropertyConstants.Asset.MANUFACTURER_NAME);
        fields.add(CamsPropertyConstants.Asset.MANUFACTURER_MODEL_NUMBER);
        fields.add(CamsPropertyConstants.AssetGlobal.ORGANIZATION_TEXT);
        fields.add(CamsPropertyConstants.Asset.REP_USER_AUTH_ID);
        fields.add(CamsPropertyConstants.Asset.LAST_INVENTORY_DATE);
        fields.add(CamsPropertyConstants.Asset.CREATE_DATE);
        fields.add(CamsPropertyConstants.Asset.ASSET_DATE_OF_SERVICE);
        fields.add(CamsPropertyConstants.AssetGlobal.CAPITAL_ASSET_DEPRECIATION_DATE);
        fields.add(CamsPropertyConstants.Asset.LAND_COUNTRY_NAME);
        fields.add(CamsPropertyConstants.Asset.LAND_ACREAGE_SIZE);
        fields.add(CamsPropertyConstants.Asset.LAND_PARCEL_NUMBER);

        return fields;
    }

    /**
     * @param assetGlobal
     * @return Asset Global Payment lines with index that should be set to read only.
     */
    protected Set<String> getAssetGlobalPaymentsReadOnlyFields(AssetGlobal assetGlobal) {
        Set<String> fields = new HashSet<String>();

        // set all payment detail fields to read only
        int i = 0;
        for (AssetPaymentDetail assetPaymentDetail : assetGlobal.getAssetPaymentDetails()) {
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.SEQUENCE_NUMBER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.CHART_OF_ACCOUNTS_CODE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.ACCOUNT_NUMBER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.SUB_ACCOUNT_NUMBER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.FINANCIAL_OBJECT_CODE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.SUB_OBJECT_CODE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.PROJECT_CODE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.ORGANIZATION_REFERENCE_ID);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_NUMBER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_TYPE_CODE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.PURCHASE_ORDER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.REQUISITION_NUMBER);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_DATE);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_YEAR);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.DOCUMENT_POSTING_FISCAL_MONTH);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.AMOUNT);
            fields.add(CamsPropertyConstants.AssetGlobal.ASSET_PAYMENT_DETAILS + "[" + i + "]." + CamsPropertyConstants.AssetPaymentDetail.ORIGINATION_CODE);

            i++;
        }

        return fields;
    }
}
