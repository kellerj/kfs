/*
 * Copyright 2006-2007 The Kuali Foundation.
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
package org.kuali.module.purap.web.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.Constants;
import org.kuali.core.service.BusinessObjectService;
import org.kuali.core.util.ObjectUtils;
import org.kuali.kfs.util.SpringServiceLocator;
import org.kuali.kfs.web.struts.action.KualiAccountingDocumentActionBase;
import org.kuali.module.purap.PurapConstants;
import org.kuali.module.purap.PurapPropertyConstants;
import org.kuali.module.purap.bo.PurchasingApItem;
import org.kuali.module.purap.document.PurchasingDocument;
import org.kuali.module.purap.document.PurchasingDocumentBase;
import org.kuali.module.purap.web.struts.form.PurchasingFormBase;
import org.kuali.module.vendor.VendorConstants;
import org.kuali.module.vendor.bo.VendorAddress;
import org.kuali.module.vendor.bo.VendorContract;
import org.kuali.module.vendor.bo.VendorDetail;
import org.kuali.module.vendor.service.PhoneNumberService;

/**
 * This class handles specific Actions requests for the Purchasing Ap.
 */
public class PurchasingActionBase extends KualiAccountingDocumentActionBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PurchasingActionBase.class);

    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingDocumentBase document = (PurchasingDocumentBase)baseForm.getDocument();
        String refreshCaller = baseForm.getRefreshCaller();
        BusinessObjectService businessObjectService = SpringServiceLocator.getBusinessObjectService();        
        PhoneNumberService phoneNumberService = SpringServiceLocator.getPhoneNumberService();
        
        // Format phone numbers        
        document.setInstitutionContactPhoneNumber(phoneNumberService.formatNumberIfPossible(document.getInstitutionContactPhoneNumber()));    
        document.setRequestorPersonPhoneNumber(phoneNumberService.formatNumberIfPossible(document.getRequestorPersonPhoneNumber()));    
        document.setDeliveryToPhoneNumber(phoneNumberService.formatNumberIfPossible(document.getDeliveryToPhoneNumber()));
               
        if( StringUtils.equals( refreshCaller, VendorConstants.VENDOR_LOOKUPABLE_IMPL ) &&
            document.getVendorDetailAssignedIdentifier() != null &&
            document.getVendorHeaderGeneratedIdentifier() != null)  {

            Integer vendorDetailAssignedId = document.getVendorDetailAssignedIdentifier();
            Integer vendorHeaderGeneratedId = document.getVendorHeaderGeneratedIdentifier();
            VendorDetail refreshVendorDetail = new VendorDetail();
            refreshVendorDetail.setVendorDetailAssignedIdentifier(vendorDetailAssignedId);
            refreshVendorDetail.setVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedId);
            refreshVendorDetail = (VendorDetail)businessObjectService.retrieve(refreshVendorDetail);
            document.templateVendorDetail(refreshVendorDetail);
        }

        if( StringUtils.equals( refreshCaller, Constants.KUALI_LOOKUPABLE_IMPL ) ) {
            
            if( StringUtils.isNotEmpty( request.getParameter( PurapPropertyConstants.VENDOR_CONTRACT_ID ) ) ) {
                Integer vendorContractGeneratedId = document.getVendorContractGeneratedIdentifier();
                VendorContract refreshVendorContract = new VendorContract();
                refreshVendorContract.setVendorContractGeneratedIdentifier(vendorContractGeneratedId);
                refreshVendorContract = (VendorContract)businessObjectService.retrieve(refreshVendorContract);
                document.templateVendorContract(refreshVendorContract);
            }
            if( StringUtils.isNotEmpty( request.getParameter( PurapPropertyConstants.VENDOR_ADDRESS_ID ) ) ) {
                Integer vendorAddressGeneratedId = document.getVendorAddressGeneratedIdentifier();
                VendorAddress refreshVendorAddress = new VendorAddress();
                refreshVendorAddress.setVendorAddressGeneratedIdentifier(vendorAddressGeneratedId);
                refreshVendorAddress = (VendorAddress)businessObjectService.retrieve(refreshVendorAddress);
                document.templateVendorAddress(refreshVendorAddress);
            }
        }
        return super.refresh(mapping, form, request, response);
    }
    
    public ActionForward refreshDeliveryBuilding(ActionMapping mapping, ActionForm form, 
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingDocumentBase document = (PurchasingDocumentBase)baseForm.getDocument();
        if( ObjectUtils.isNotNull( document.isDeliveryBuildingOther() ) ) {
            if (document.isDeliveryBuildingOther()) {
// TODO check in referenced constants
//                document.setDeliveryBuildingName( PurapConstants.DELIVERY_BUILDING_OTHER );
//                document.setDeliveryBuildingCode( PurapConstants.DELIVERY_BUILDING_OTHER_CODE );
                baseForm.setNotOtherDeliveryBuilding(false);
            }
            else {
                document.setDeliveryBuildingName(null);
                document.setDeliveryBuildingCode(null);
                baseForm.setNotOtherDeliveryBuilding(true);
            }
        }       
        return refresh(mapping, form, request, response);
    }

    /**
     * Add a new item to the document.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        // TODO: should call add line event/rules here
        PurchasingApItem item = purchasingForm.getAndResetNewPurchasingItemLine();
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        purDocument.addItem(item);
        return mapping.findForward(Constants.MAPPING_BASIC);
    }

    /**
     * Delete an item from the document.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        // TODO: should call delete line event/rules here

        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        purDocument.deleteItem(getSelectedLine(request));
        return mapping.findForward(Constants.MAPPING_BASIC);
    }

}