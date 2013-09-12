/*
 * Copyright 2007-2008 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.pdp.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.batch.ProcessPdPCancelsAndPaidStep;
import org.kuali.kfs.pdp.batch.service.ProcessPdpCancelPaidService;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProcessPdpCancelPaidService
 */
@Transactional
public class ProcessPdpCancelPaidServiceImpl implements ProcessPdpCancelPaidService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcessPdpCancelPaidServiceImpl.class);

    protected PaymentGroupService paymentGroupService;
    protected PaymentDetailService paymentDetailService;
    protected ParameterService parameterService;
    protected DateTimeService dateTimeService;
    protected PurchasingAccountsPayableModuleService purchasingAccountsPayableModuleService;
    protected PaymentSourceHelperService paymentSourceHelperService;
    protected DocumentService documentService;

    protected volatile Set<String> paymentSourceCheckACHDocumentTypes;

    /**
     * @see org.kuali.kfs.module.purap.service.ProcessPdpCancelPaidService#processPdpCancels()
     */
    @Override
    public void processPdpCancels() {
        LOG.debug("processPdpCancels() started");

        Date processDate = dateTimeService.getCurrentSqlDate();

        String organization = parameterService.getParameterValueAsString(KfsParameterConstants.PURCHASING_BATCH.class, KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_ORG_CODE);
        String purapSubUnit = parameterService.getParameterValueAsString(KfsParameterConstants.PURCHASING_BATCH.class, KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_SUB_UNIT_CODE);
        String dvSubUnit = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class, KFSParameterKeyConstants.PdpExtractBatchParameters.PDP_SBUNT_CODE);

        List<String> subUnits = new ArrayList<String>();
        subUnits.add(purapSubUnit);
        subUnits.add(dvSubUnit);

        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedCancelledDetails(organization, subUnits);
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();

            String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
            String documentNumber = paymentDetail.getCustPaymentDocNbr();

            boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
            boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT.equals(paymentDetail.getPaymentGroup().getPaymentStatusCode());

            if(purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
                purchasingAccountsPayableModuleService.handlePurchasingBatchCancels(documentNumber, documentTypeCode, primaryCancel, disbursedPayment);
            }
            else if (getPaymentSourceCheckACHDocumentTypes().contains(documentTypeCode)) {
                try {
                    PaymentSource dv = (PaymentSource)getDocumentService().getByDocumentHeaderId(documentNumber);
                    if (dv != null) {
                        if (disbursedPayment || primaryCancel) {
                            getPaymentSourceHelperService().cancelExtractedPaymentSource(dv, processDate);
                        } else {
                            getPaymentSourceHelperService().resetExtractedPaymentSource(dv, processDate);
                        }
                    }
                } catch (WorkflowException we) {
                    throw new RuntimeException("Could not retrieve document #"+documentNumber, we);
                }
            }
            else {
                LOG.warn("processPdpCancels() Unknown document type (" + documentTypeCode + ") for document ID: " + documentNumber);
                continue;
            }

            paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
        }
    }

    /**
     * @see org.kuali.kfs.module.purap.service.ProcessPdpCancelPaidService#processPdpPaids()
     */
    @Override
    public void processPdpPaids() {
        LOG.debug("processPdpPaids() started");

        Date processDate = dateTimeService.getCurrentSqlDate();

        String organization = parameterService.getParameterValueAsString(KfsParameterConstants.PURCHASING_BATCH.class, KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_ORG_CODE);
        String purapSubUnit = parameterService.getParameterValueAsString(KfsParameterConstants.PURCHASING_BATCH.class, KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_SUB_UNIT_CODE);
        String dvSubUnit = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class, KFSParameterKeyConstants.PdpExtractBatchParameters.PDP_SBUNT_CODE);

        List<String> subUnits = new ArrayList<String>();
        subUnits.add(purapSubUnit);
        subUnits.add(dvSubUnit);

        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedPaidDetails(organization, subUnits);
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();

            String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
            String documentNumber = paymentDetail.getCustPaymentDocNbr();

            if(purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
                purchasingAccountsPayableModuleService.handlePurchasingBatchPaids(documentNumber, documentTypeCode, processDate);
            }
            else if (getPaymentSourceCheckACHDocumentTypes().contains(documentTypeCode)) {
                try {
                    PaymentSource dv = (PaymentSource)getDocumentService().getByDocumentHeaderId(documentNumber);
                    getPaymentSourceHelperService().markPaymentSourceAsPaid(dv, processDate);
                } catch (WorkflowException we) {
                    throw new RuntimeException("Could not retrieve document #"+documentNumber, we);
                }
            }
            else {
                LOG.warn("processPdpPaids() Unknown document type (" + documentTypeCode + ") for document ID: " + documentNumber);
                continue;
            }

            paymentGroupService.processPaidGroup(paymentDetail.getPaymentGroup(), processDate);
        }
    }

    /**
     * @see org.kuali.kfs.module.purap.service.ProcessPdpCancelPaidService#processPdpCancelsAndPaids()
     */
    @Override
    public void processPdpCancelsAndPaids() {
        LOG.debug("processPdpCancelsAndPaids() started");

        processPdpCancels();
        processPdpPaids();
    }

    /**
     * @return a List of all of the FSLO-parented document types which represent ACH or Checks created by PaymentSources
     */
    public Set<String> getPaymentSourceCheckACHDocumentTypes() {
        if (paymentSourceCheckACHDocumentTypes == null) {
            paymentSourceCheckACHDocumentTypes = new HashSet<String>(parameterService.getParameterValuesAsString(ProcessPdPCancelsAndPaidStep.class, PdpParameterConstants.PAYMENT_SOURCE_DOCUMENT_TYPES));
        }
        return paymentSourceCheckACHDocumentTypes;
    }

    public void setPaymentDetailService(PaymentDetailService paymentDetailService) {
        this.paymentDetailService = paymentDetailService;
    }

    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPurchasingAccountsPayableModuleService(PurchasingAccountsPayableModuleService purchasingAccountsPayableModuleService) {
        this.purchasingAccountsPayableModuleService = purchasingAccountsPayableModuleService;
    }

    public void setDateTimeService(DateTimeService dts) {
        this.dateTimeService = dts;
    }

    /**
     * @return the implementation of the DocumentService to use
     */
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Sets the implementation of the DocumentService to use
     * @param documentService the implementation of the DocumentService to use
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * @return an implementation of the PaymentSourceHelperService
     */
    public PaymentSourceHelperService getPaymentSourceHelperService() {
        return paymentSourceHelperService;
    }

    /**
     * Sets the implementation of the PaymentSourceHelperService for this service to use
     * @param paymentSourceHelperService an implementation of PaymentSourceHelperService
     */
    public void setPaymentSourceHelperService(PaymentSourceHelperService paymentSourceHelperService) {
        this.paymentSourceHelperService = paymentSourceHelperService;
    }
}
