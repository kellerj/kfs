/*
 * Copyright (c) 2004, 2005 The National Association of College and University Business Officers,
 * Cornell University, Trustees of Indiana University, Michigan State University Board of Trustees,
 * Trustees of San Joaquin Delta College, University of Hawai'i, The Arizona Board of Regents on
 * behalf of the University of Arizona, and the r*smart group.
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License"); By obtaining,
 * using and/or copying this Original Work, you agree that you have read, understand, and will
 * comply with the terms and conditions of the Educational Community License.
 * 
 * You may obtain a copy of the License at:
 * 
 * http://kualiproject.org/license.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.kuali.module.financial.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.Constants;
import org.kuali.PropertyConstants;
import org.kuali.Constants.CashDrawerConstants;
import org.kuali.Constants.DepositConstants;
import org.kuali.core.authorization.DocumentAuthorizer;
import org.kuali.core.bo.user.KualiUser;
import org.kuali.core.document.DocumentHeader;
import org.kuali.core.exceptions.DocumentTypeAuthorizationException;
import org.kuali.core.exceptions.InfrastructureException;
import org.kuali.core.service.BusinessObjectService;
import org.kuali.core.service.DateTimeService;
import org.kuali.core.service.DocumentService;
import org.kuali.core.util.GlobalVariables;
import org.kuali.core.util.KualiDecimal;
import org.kuali.core.util.SpringServiceLocator;
import org.kuali.module.financial.bo.Bank;
import org.kuali.module.financial.bo.BankAccount;
import org.kuali.module.financial.bo.CashDrawer;
import org.kuali.module.financial.bo.CashReceiptHeader;
import org.kuali.module.financial.bo.Deposit;
import org.kuali.module.financial.bo.DepositCashReceiptControl;
import org.kuali.module.financial.document.CashManagementDocument;
import org.kuali.module.financial.document.CashReceiptDocument;
import org.kuali.module.financial.exceptions.CashDrawerStateException;
import org.kuali.module.financial.exceptions.InvalidCashReceiptState;
import org.kuali.module.financial.service.CashDrawerService;
import org.kuali.module.financial.service.CashManagementService;
import org.kuali.module.financial.web.struts.form.CashDrawerStatusCodeFormatter;

import edu.iu.uis.eden.exception.WorkflowException;

public class CashManagementServiceImpl implements CashManagementService {
    private BusinessObjectService businessObjectService;
    private CashDrawerService cashDrawerService;
    private DateTimeService dateTimeService;
    private DocumentService documentService;


    /**
     * If a CMD is found that is associated with the CR document, then that CMD is returned; otherwise null is returned. Currently
     * the relationships are:
     * <ul>
     * <li>(CashReceipt to CashReceiptHeader) is (1 to 1)
     * <li>(CashReceiptHeader to DepositCashReceiptControl) is (1 to 1)
     * <li>(DepositCashReceiptControl to Deposit) is (many to 1)
     * <li>(Deposit to CashManagementDocument) is (many to 1)
     * </ul>
     * 
     * @see org.kuali.module.financial.service.CashManagementService#getCashManagementDocumentForCashReceiptId(java.lang.String)
     */
    public CashManagementDocument getCashManagementDocumentForCashReceiptId(String documentId) {
        CashManagementDocument cmdoc = null;

        // get CashReceiptHeader for the CashReceipt, if any
        HashMap primaryKeys = new HashMap();
        primaryKeys.put(PropertyConstants.FINANCIAL_DOCUMENT_NUMBER, documentId);
        CashReceiptHeader crh = (CashReceiptHeader) businessObjectService.findByPrimaryKey(CashReceiptHeader.class, primaryKeys);

        // get the DepositCashReceiptControl for the CashReceiptHeader
        if (crh != null) {
            List crcList = crh.getDepositCashReceiptControl();
            if (!crcList.isEmpty()) {
                DepositCashReceiptControl dpcrc = (DepositCashReceiptControl) crcList.get(0);

                // get the Deposit and follow it to the CashManagementDocument
                Deposit d = (Deposit) dpcrc.getDeposit();
                cmdoc = d.getCashManagementDocument();
            }
        }

        return cmdoc;
    }


    /**
     * @see org.kuali.module.financial.service.CashManagementService#createCashManagementDocument(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public CashManagementDocument createCashManagementDocument(String unitName, String docDescription, String annotation) {
        if (StringUtils.isBlank(unitName)) {
            throw new IllegalArgumentException("invalid (blank) unitName");
        }
        if (StringUtils.isBlank(docDescription)) {
            throw new IllegalArgumentException("invalid (blank) docDescription");
        }

        CashManagementDocument cmDoc = null;

        // check user authorization
        KualiUser user = GlobalVariables.getUserSession().getKualiUser();
        String documentTypeName = SpringServiceLocator.getDataDictionaryService().getDocumentTypeNameByClass(CashManagementDocument.class);
        DocumentAuthorizer documentAuthorizer = SpringServiceLocator.getDocumentAuthorizationService().getDocumentAuthorizer(documentTypeName);
        if (!documentAuthorizer.canInitiate(documentTypeName, user)) {
            throw new DocumentTypeAuthorizationException(user.getPersonUserIdentifier(), "initiate", documentTypeName);
        }

        // check cash drawer
        CashDrawer cd = cashDrawerService.getByWorkgroupName(unitName, true);
        if (cd.isClosed()) {
            try {
                // create the document
                cmDoc = (CashManagementDocument) documentService.getNewDocument(CashManagementDocument.class);
                cmDoc.getDocumentHeader().setFinancialDocumentDescription(docDescription);
                cmDoc.setWorkgroupName(unitName);

                // open cash drawer
                cashDrawerService.openCashDrawer(unitName, cmDoc.getFinancialDocumentNumber());

                // persist everything
                documentService.saveDocument(cmDoc, annotation, null);
            }
            catch (RuntimeException e) {
                // reclose the drawer if creation failed (without trapping the failure-to-close)
                cashDrawerService.closeCashDrawer(unitName);

                throw e;
            }
            catch (WorkflowException e) {
                // reclose the drawer if creation failed (without trapping the failure-to-close)
                cashDrawerService.closeCashDrawer(unitName);

                throw new InfrastructureException("unable to create CashManagementDocument ", e);
            }
        }
        else {
            CashDrawerStatusCodeFormatter f = new CashDrawerStatusCodeFormatter();
            String controllingDocId = cd.getReferenceFinancialDocumentNumber();
            throw new CashDrawerStateException(unitName, controllingDocId, (String) f.format(CashDrawerConstants.STATUS_CLOSED), (String) f.format(cd.getStatusCode()));
        }

        return cmDoc;
    }


    /**
     * @see org.kuali.module.financial.service.CashManagementService#addInterimDeposit(org.kuali.module.financial.document.CashManagementDocument,
     *      int, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @SuppressWarnings("deprecation")
    public void addInterimDeposit(CashManagementDocument cashManagementDoc, String depositTicketNumber, BankAccount bankAccount, List selectedCashReceipts) {
        //
        // validate parameters

        if (cashManagementDoc == null) {
            throw new IllegalArgumentException("invalid (null) cashManagementDoc");
        }
        else if (!cashManagementDoc.getDocumentHeader().getWorkflowDocument().stateIsSaved()) {
            throw new IllegalStateException("cashManagementDoc '" + cashManagementDoc.getFinancialDocumentNumber() + "' is not in 'saved' state");
        }
        else if (cashManagementDoc.hasFinalDeposit()) {
            throw new IllegalStateException("cashManagementDoc '" + cashManagementDoc.getFinancialDocumentNumber() + "' hasFinalDeposit");
        }
        if (bankAccount == null) {
            throw new IllegalArgumentException("invalid (null) bankAccount");
        }

        validateInterimCashReceipts(selectedCashReceipts);


        //
        // create the Deposit

        // lock the cashDrawer
        cashDrawerService.lockCashDrawer(cashManagementDoc.getWorkgroupName(), cashManagementDoc.getFinancialDocumentNumber());

        // derive the line number
        int lineNumber = cashManagementDoc.getNextDepositLineNumber();

        // create the deposit
        Deposit deposit = new Deposit();
        deposit.setFinancialDocumentNumber(cashManagementDoc.getFinancialDocumentNumber());
        deposit.setFinancialDocumentDepositLineNumber(new Integer(lineNumber));
        deposit.setCashManagementDocument(cashManagementDoc);

        deposit.setDepositTypeCode(DepositConstants.DEPOSIT_TYPE_INTERIM);

        deposit.setDepositDate(dateTimeService.getCurrentSqlDate());

        // trim to empty, because the field is optional
        deposit.setDepositTicketNumber(StringUtils.trimToEmpty(depositTicketNumber));

        deposit.setBankAccount(bankAccount);
        deposit.setDepositBankCode(bankAccount.getBank().getFinancialDocumentBankCode());
        deposit.setDepositBankAccountNumber(bankAccount.getFinDocumentBankAccountNumber());

        // total up the cash receipts
        KualiDecimal total = KualiDecimal.ZERO;
        for (Iterator i = selectedCashReceipts.iterator(); i.hasNext();) {
            CashReceiptDocument crDoc = (CashReceiptDocument) i.next();
            total = total.add(crDoc.getSumTotalAmount());
        }
        deposit.setDepositAmount(total);


        //
        // attach it to the document
        List deposits = cashManagementDoc.getDeposits();
        deposits.add(deposit);
        documentService.updateDocument(cashManagementDoc);


        //
        // associate the CashReceipts with the Deposit
        List dccList = new ArrayList();
        for (Iterator i = selectedCashReceipts.iterator(); i.hasNext();) {
            CashReceiptDocument crDoc = (CashReceiptDocument) i.next();
            DocumentHeader dh = crDoc.getDocumentHeader();
            dh.setFinancialDocumentStatusCode(Constants.DocumentStatusCodes.CashReceipt.INTERIM);
            documentService.updateDocument(crDoc);

            CashReceiptHeader crHeader = new CashReceiptHeader();
            crHeader.setFinancialDocumentNumber(crDoc.getFinancialDocumentNumber());
            crHeader.setCashReceiptDocument(crDoc);
            crHeader.setWorkgroupName(cashManagementDoc.getWorkgroupName());

            DepositCashReceiptControl dcc = new DepositCashReceiptControl();
            dcc.setFinancialDocumentCashReceiptNumber(crHeader.getFinancialDocumentNumber());
            dcc.setFinancialDocumentDepositNumber(deposit.getFinancialDocumentNumber());
            dcc.setFinancialDocumentDepositLineNumber(deposit.getFinancialDocumentDepositLineNumber());

            dcc.setCashReceiptHeader(crHeader);
            dcc.setDeposit(deposit);

            dccList.add(dcc);
        }
        // crHeaders get saved as side-effect of saving dccs
        businessObjectService.save(dccList);

        // unlock the cashDrawer
        cashDrawerService.unlockCashDrawer(cashManagementDoc.getWorkgroupName(), cashManagementDoc.getFinancialDocumentNumber());
    }

    private void validateInterimCashReceipts(List cashReceipts) {
        if (cashReceipts == null) {
            throw new IllegalArgumentException("invalid (null) cashReceipts list");
        }
        else if (cashReceipts.isEmpty()) {
            throw new IllegalArgumentException("invalid (empty) cashReceipts list");
        }
        else {
            for (Iterator i = cashReceipts.iterator(); i.hasNext();) {
                CashReceiptDocument cashReceipt = (CashReceiptDocument) i.next();

                String statusCode = cashReceipt.getDocumentHeader().getFinancialDocumentStatusCode();
                if (!StringUtils.equals(statusCode, Constants.DocumentStatusCodes.CashReceipt.VERIFIED)) {
                    throw new InvalidCashReceiptState("cash receipt document " + cashReceipt.getFinancialDocumentNumber() + " has a status other than 'verified' ");
                }
            }
        }
    }

    private Bank lookupBank(String bankCode) {
        Map keyMap = new HashMap();
        keyMap.put("financialDocumentBankCode", bankCode);

        Bank bank = (Bank) businessObjectService.findByPrimaryKey(Bank.class, keyMap);
        return bank;
    }

    private BankAccount lookupBankAccount(String bankCode, String accountNumber) {
        Map keyMap = new HashMap();
        keyMap.put("financialDocumentBankCode", bankCode);
        keyMap.put("finDocumentBankAccountNumber", accountNumber);

        BankAccount bankAccount = (BankAccount) businessObjectService.findByPrimaryKey(BankAccount.class, keyMap);
        return bankAccount;
    }


    /**
     * Method should only be called after the appropriate CashManagementDocumentRule has been successfully passed
     * 
     * @see org.kuali.module.financial.service.CashManagementService#cancelCashManagementDocument(org.kuali.module.financial.document.CashManagementDocument)
     */
    public void cancelCashManagementDocument(CashManagementDocument cmDoc) {
        if (cmDoc == null) {
            throw new IllegalArgumentException("invalid (null) CashManagementDocument");
        }

        // cancel each deposit (which also deletes the records connecting the Deposit to a CashManagementDoc
        List deposits = cmDoc.getDeposits();
        for (Iterator i = deposits.iterator(); i.hasNext();) {
            Deposit deposit = (Deposit) i.next();

            cancelDeposit(deposit);
        }
        cmDoc.setDeposits(new ArrayList());

        // reclose the cashDrawer
        String unitName = cmDoc.getWorkgroupName();
        cashDrawerService.closeCashDrawer(cmDoc.getWorkgroupName());
    }


    /**
     * @see org.kuali.module.financial.service.CashManagementService#cancelDeposit(org.kuali.module.financial.bo.Deposit)
     */
    public void cancelDeposit(Deposit deposit) {
        if (deposit == null) {
            throw new IllegalArgumentException("invalid (null) deposit");
        }

        // reload it, to forestall OptimisticLockExceptions
        deposit.refresh();

        // update every CashReceipt associated with this Deposit
        List depositCashReceiptControls = deposit.getDepositCashReceiptControl();
        for (Iterator j = depositCashReceiptControls.iterator(); j.hasNext();) {
            DepositCashReceiptControl dcc = (DepositCashReceiptControl) j.next();
            CashReceiptHeader crHeader = dcc.getCashReceiptHeader();

            // reset each CashReceipt status
            CashReceiptDocument crDoc = crHeader.getCashReceiptDocument();
            DocumentHeader crdh = crDoc.getDocumentHeader();
            crdh.setFinancialDocumentStatusCode(Constants.DocumentStatusCodes.CashReceipt.VERIFIED);
            documentService.updateDocument(crDoc);
        }

        // delete the Deposit from the database
        businessObjectService.delete(deposit);

        // remove the Deposit from the cashManagementDocument
        // (which unfortunately means that the line number isn't the index any more, for any deposit after this one)
    }


    /**
     * Method should only be called after the appropriate CashManagementDocumentRule has been successfully passed
     * 
     * @see org.kuali.module.financial.service.CashManagementService#finalizeCashManagementDocument(org.kuali.module.financial.document.CashManagementDocument)
     */
    public void finalizeCashManagementDocument(CashManagementDocument cmDoc) {
        if (cmDoc == null) {
            throw new IllegalArgumentException("invalid (null) CashManagementDocument");
        }


        // UNF: finalize the Deposits?

        throw new UnsupportedOperationException();
    }


    /**
     * @see org.kuali.module.financial.service.CashManagementService#retrieveCashReceipts(org.kuali.module.financial.bo.Deposit)
     */
    public List retrieveCashReceipts(Deposit deposit) {
        List cashReceiptDocuments = null;

        // retrieve CashReceiptHeaders
        Map criteriaMap = new HashMap();
        criteriaMap.put("depositCashReceiptControl.financialDocumentDepositNumber", deposit.getFinancialDocumentNumber());
        criteriaMap.put("depositCashReceiptControl.financialDocumentDepositLineNumber", deposit.getFinancialDocumentDepositLineNumber());

        List crHeaders = new ArrayList(businessObjectService.findMatching(CashReceiptHeader.class, criteriaMap));
        if (!crHeaders.isEmpty()) {
            List idList = new ArrayList();
            for (Iterator i = crHeaders.iterator(); i.hasNext();) {
                CashReceiptHeader crHeader = (CashReceiptHeader) i.next();
                idList.add(crHeader.getFinancialDocumentNumber());
            }

            try {
                cashReceiptDocuments = documentService.getDocumentsByListOfDocumentHeaderIds(CashReceiptDocument.class, idList);
            }
            catch (WorkflowException e) {
                throw new InfrastructureException("unable to retrieve cashReceipts", e);
            }
        }
        else {
            cashReceiptDocuments = new ArrayList();
        }

        return cashReceiptDocuments;
    }


    // injected dependencies
    /**
     * @return current value of businessObjectService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService attribute value.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * @return current value of cashDrawerService.
     */
    public CashDrawerService getCashDrawerService() {
        return cashDrawerService;
    }

    /**
     * Sets the cashDrawerService attribute value.
     * 
     * @param cashDrawerService The cashDrawerService to set.
     */
    public void setCashDrawerService(CashDrawerService cashDrawerService) {
        this.cashDrawerService = cashDrawerService;
    }

    /**
     * @return current value of documentService.
     */
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Sets the documentService attribute value.
     * 
     * @param documentService
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * @return current value of dateTimeService.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
