/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.module.ar.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.integration.cg.ContractsAndGrantsModuleBillingService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsOrganization;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.batch.service.ContractsGrantsInvoiceCreateDocumentService;
import org.kuali.kfs.module.ar.batch.service.VerifyBillingFrequencyService;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableDocumentHeaderService;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.KualiModuleService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


/**
 * This is the default implementation of the ContractsGrantsInvoiceDocumentCreateService interface.
 *
 * @see org.kuali.kfs.module.ar.batch.service.ContractsGrantsInvoiceDocumentCreateService
 */
@Transactional
public class ContractsGrantsInvoiceCreateDocumentServiceImpl implements ContractsGrantsInvoiceCreateDocumentService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ContractsGrantsInvoiceCreateDocumentServiceImpl.class);

    protected AccountService accountService;
    protected AccountingPeriodService accountingPeriodService;
    protected AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService;
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService;
    protected ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService;
    protected DocumentService documentService;
    protected FinancialSystemDocumentService financialSystemDocumentService;
    protected KualiModuleService kualiModuleService;
    protected VerifyBillingFrequencyService verifyBillingFrequencyService;
    protected WorkflowDocumentService workflowDocumentService;

    public static final String REPORT_LINE_DIVIDER = "--------------------------------------------------------------------------------------------------------------";

    /**
     * The default implementation of this service retrieves a collection of qualified Awards and create Contracts Grants Invoice
     * Documents by Awards.
     *
     * @see org.kuali.kfs.module.ar.batch.service.ContractsGrantsInvoiceDocumentCreateService#createCGInvoiceDocumentsByAwards(java.lang.String)
     */
    @Override
    public void createCGInvoiceDocumentsByAwards(Collection<ContractsAndGrantsBillingAward> awards, String errOutputFileName) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument;

        List<String> errLines = new ArrayList<String>();

        if (ObjectUtils.isNotNull(awards)) {
            // iterate through awards and create cgInvoice documents
            for (ContractsAndGrantsBillingAward awd : awards) {
                String invOpt = awd.getInvoicingOptions();
                final ContractsAndGrantsOrganization awardOrganization = awd.getPrimaryAwardOrganization();
                if (ObjectUtils.isNull(awardOrganization)) {
                    final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_ORGANIZATION_ON_AWARD);
                    errLines.add(MessageFormat.format(errorMessage, awd.getProposalNumber().toString()));
                } else {
                    if (invOpt.equals(ArPropertyConstants.INV_ACCOUNT)) { // case 1: create Contracts Grants Invoice by accounts
                        createInvoicesByAccounts(awd, errLines);
                    }
                    else if (invOpt.equals(ArPropertyConstants.INV_CONTRACT_CONTROL_ACCOUNT)) { // case 2: create Contracts Grants Invoices by contractControlAccounts
                        createInvoicesByContractControlAccounts(awd, errLines);
                    }
                    // case 3: create Contracts Grants Invoice by award
                    else if (invOpt.equals(ArPropertyConstants.INV_AWARD)) {
                        createInvoicesByAward(awd, errLines);
                    }
                }
            }
        }  else {
            final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_AWARD);
            errLines.add(errorMessage);
        }

        // print out the invalid awards which has not been used to create CINV edoc
        if (!CollectionUtils.isEmpty(errLines)) {
            File errOutPutfile = new File(errOutputFileName);
            PrintStream outputFileStream = null;

            try {
                outputFileStream = new PrintStream(errOutPutfile);
                for (String line : errLines) {
                    outputFileStream.printf("%s\n", line);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Could not write error entries for batch Contracts and Grants Invoice document creation", ex);
            } finally {
                if (outputFileStream != null) {
                    outputFileStream.close();
                }
            }
        }
    }


    /**
     * Generates and saves a single contracts and grants invoice document based on the given award
     * @param awd the award to generate a contracts and grants invoice document for
     * @param errLines a holder for error messages
     */
    protected void createInvoicesByAward(ContractsAndGrantsBillingAward awd, List<String> errLines) {
        // Check if awardaccounts has the same control account
        int accountNum = awd.getActiveAwardAccounts().size();
        Collection<Account> controlAccounts = contractsGrantsInvoiceDocumentService.getContractControlAccounts(awd);
        if (controlAccounts == null || controlAccounts.size() < accountNum) {
            final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.BILL_BY_CONTRACT_VALID_ACCOUNTS);
            errLines.add(MessageFormat.format(errorMessage, awd.getProposalNumber().toString()));
        }
        else {
            // check if control accounts of awardaccounts are the same
            boolean isValid = true;
            if (accountNum != 1) {
                Account tmpAcct1, tmpAcct2;

                Object[] awardAccounts = awd.getActiveAwardAccounts().toArray();
                for (int i = 0; i < awardAccounts.length - 1; i++) {
                    tmpAcct1 = ((ContractsAndGrantsBillingAwardAccount) awardAccounts[i]).getAccount().getContractControlAccount();
                    tmpAcct2 = ((ContractsAndGrantsBillingAwardAccount) awardAccounts[i + 1]).getAccount().getContractControlAccount();

                    if (ObjectUtils.isNull(tmpAcct1) || ObjectUtils.isNull(tmpAcct2) || !tmpAcct1.equals(tmpAcct2)) {
                        final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.DIFFERING_CONTROL_ACCOUNTS);
                        errLines.add(MessageFormat.format(errorMessage, awd.getProposalNumber().toString()));
                        isValid = false;
                        break;
                    }
                }
            }

            if (isValid) {
                String coaCode = null;
                String orgCode = null;
                for (ContractsAndGrantsBillingAwardAccount awardAccount : awd.getActiveAwardAccounts()) {
                    Account account = awardAccount.getAccount();
                    coaCode = awd.getPrimaryAwardOrganization().getChartOfAccountsCode();
                    orgCode = awd.getPrimaryAwardOrganization().getOrganizationCode();
                }
                // To get valid award accounts of amounts > zero$ and pass it to the create invoices method
                // To get valid award accounts of amounts > zero$ and pass it to the create invoices method
                if (!getValidAwardAccounts(awd.getActiveAwardAccounts(), awd).containsAll(awd.getActiveAwardAccounts())) {
                    final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NOT_ALL_BILLABLE_ACCOUNTS);
                    errLines.add(MessageFormat.format(errorMessage, awd.getProposalNumber().toString()));
                }
                generateAndSaveContractsAndGrantsInvoiceDocument(awd, getValidAwardAccounts(awd.getActiveAwardAccounts(), awd), coaCode, orgCode, errLines);
            }
        }
    }


    /**
     * Generates and saves contracts and grants invoice documents based on the given award's contract control accounts
     * @param awd the award with contract control accounts to build contracts and grants invoice documents from
     * @param errLines a holder for error messages
     */
    protected void createInvoicesByContractControlAccounts(ContractsAndGrantsBillingAward awd, List<String> errLines) {
        List<ContractsAndGrantsBillingAwardAccount> tmpAcctList = new ArrayList<ContractsAndGrantsBillingAwardAccount>();
        List<Account> controlAccounts = contractsGrantsInvoiceDocumentService.getContractControlAccounts(awd);
        List<Account> controlAccountsTemp = contractsGrantsInvoiceDocumentService.getContractControlAccounts(awd);

        if (controlAccounts == null || (controlAccounts.size() != awd.getActiveAwardAccounts().size())) {// to check if the number of contract control accounts is same as the number of accounts
            final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_CONTROL_ACCOUNT);
            errLines.add(MessageFormat.format(errorMessage, awd.getProposalNumber().toString()));
        }
        else {
            Set<Account> controlAccountSet = new HashSet<Account>();
            for (int i = 0; i < controlAccountsTemp.size(); i++) {
                if (ObjectUtils.isNotNull(controlAccountsTemp.get(i))) {
                    for (int j = i + 1; j < controlAccounts.size(); j++) {
                        if (controlAccountsTemp.get(i).equals(controlAccounts.get(j))) {
                            controlAccounts.set(j, null);
                        }
                    }
                }
                else {
                    break;
                }
            }
            for (Account ctrlAcct : controlAccounts) {
                if (ObjectUtils.isNotNull(ctrlAcct)) {
                    controlAccountSet.add(ctrlAcct);
                }

            }
            // control accounts are set correctly for award accounts

            if (controlAccountSet.size() != 0) {
                for (Account controlAccount : controlAccountSet) {
                    Account tmpCtrlAcct = null;

                    for (ContractsAndGrantsBillingAwardAccount awardAccount : awd.getActiveAwardAccounts()) {
                        if (!awardAccount.isFinalBilledIndicator()) {
                            tmpCtrlAcct = awardAccount.getAccount().getContractControlAccount();
                            if (tmpCtrlAcct.getChartOfAccountsCode().equals(controlAccount.getChartOfAccountsCode()) && tmpCtrlAcct.getAccountNumber().equals(controlAccount.getAccountNumber())) {
                                tmpAcctList.add(awardAccount);
                            }
                        }
                    }

                    // To get valid award accounts of amounts > zero$ and pass it to the create invoices method
                    if (!getValidAwardAccounts(tmpAcctList, awd).containsAll(tmpAcctList)) {
                        final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.CONTROL_ACCOUNT_NON_BILLABLE);
                        errLines.add(MessageFormat.format(errorMessage, controlAccount.getAccountNumber(), awd.getProposalNumber().toString()));
                    }
                    generateAndSaveContractsAndGrantsInvoiceDocument(awd, getValidAwardAccounts(tmpAcctList, awd), awd.getPrimaryAwardOrganization().getChartOfAccountsCode(), awd.getPrimaryAwardOrganization().getOrganizationCode(), errLines);
                }
            }
            else {
                final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.BILL_BY_CONTRACT_LACKS_CONTROL_ACCOUNT);
                errLines.add(MessageFormat.format(errorMessage, awd.getProposalNumber().toString()));
            }
        }
    }

    /**
     * Generates and saves contracts and grants invoice documens based on the award accounts of the passed in award
     * @param awd the award to build contracts and grants invoice documents from the award accounts on
     * @param errLines a holder for error messages
     */
    protected void createInvoicesByAccounts(ContractsAndGrantsBillingAward awd, List<String> errLines) {
        List<ContractsAndGrantsBillingAwardAccount> tmpAcctList = new ArrayList<ContractsAndGrantsBillingAwardAccount>();

        for (ContractsAndGrantsBillingAwardAccount awardAccount : awd.getActiveAwardAccounts()) {
            if (!awardAccount.isFinalBilledIndicator()) {
                // only one account is added into the list to create cgin
                tmpAcctList.add(awardAccount);

                // To get valid award accounts of amounts > zero$ and pass it to the create invoices method
                if (!getValidAwardAccounts(tmpAcctList, awd).containsAll(tmpAcctList)) {
                    final String errorMessage = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NON_BILLABLE);
                    errLines.add(MessageFormat.format(errorMessage, awardAccount.getAccountNumber(), awd.getProposalNumber().toString()));
                }

                generateAndSaveContractsAndGrantsInvoiceDocument(awd, getValidAwardAccounts(tmpAcctList, awd), awd.getPrimaryAwardOrganization().getChartOfAccountsCode(), awd.getPrimaryAwardOrganization().getOrganizationCode(), errLines);
            }
        }
    }

    /**
     * Generates and then saves a contracts and grants invoice document
     * @param awd the award for the document
     * @param validAwardAccounts the award accounts which should appear on the document
     * @param coaCode the chart code for the document
     * @param orgCode the organization code for the document
     * @param errLines a List of error messages, to be appended to if there are errors in document generation
     */
    protected void generateAndSaveContractsAndGrantsInvoiceDocument(ContractsAndGrantsBillingAward awd, List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts, final String coaCode, final String orgCode, List<String> errLines) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = createCGInvoiceDocumentByAwardInfo(awd, validAwardAccounts, coaCode, orgCode, errLines);
        if (ObjectUtils.isNotNull(cgInvoiceDocument)) {
            // Saving the document
            try {
                documentService.saveDocument(cgInvoiceDocument, DocumentSystemSaveEvent.class);
            }
            catch (WorkflowException ex) {
                LOG.error("Error creating cgin documents: " + ex.getMessage(), ex);
                throw new RuntimeException("Error creating cgin documents: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * This method retrieves create a ContractsGrantsInvoiceDocument by Award * @param awd
     *
     * @return ContractsGrantsInvoiceDocument
     */
    @Override
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(ContractsAndGrantsBillingAward awd, List<ContractsAndGrantsBillingAwardAccount> accounts, String chartOfAccountsCode, String organizationCode, List<String> errLines) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = null;
        if (ObjectUtils.isNotNull(accounts) && !accounts.isEmpty()) {
            if (chartOfAccountsCode != null && organizationCode != null) {
                try {
                    cgInvoiceDocument = (ContractsGrantsInvoiceDocument) documentService.getNewDocument(ContractsGrantsInvoiceDocument.class);
                    // Set description to the document created.
                    cgInvoiceDocument.getDocumentHeader().setDocumentDescription(ArConstants.BatchFileSystem.CGINVOICE_DOCUMENT_DESCRIPTION_OF_BATCH_PROCESS);
                    // setup several Default Values for CGInvoice document which extends from Customer Invoice Document

                    // a) set billing org and chart code
                    cgInvoiceDocument.setBillByChartOfAccountCode(chartOfAccountsCode);
                    cgInvoiceDocument.setBilledByOrganizationCode(organizationCode);

                    // b) set processing org and chart code
                    List<String> procCodes = contractsGrantsInvoiceDocumentService.getProcessingFromBillingCodes(chartOfAccountsCode, organizationCode);

                    AccountsReceivableDocumentHeader accountsReceivableDocumentHeader = new AccountsReceivableDocumentHeader();
                    accountsReceivableDocumentHeader.setDocumentNumber(cgInvoiceDocument.getDocumentNumber());

                    // Set processing chart and org codes
                    if (procCodes != null){
                        int procCodesSize = procCodes.size();

                        // Set processing chart
                        if (procCodesSize > 0){
                            accountsReceivableDocumentHeader.setProcessingChartOfAccountCode(procCodes.get(0));
                        }

                        // Set processing org code
                        if (procCodesSize > 1){
                            accountsReceivableDocumentHeader.setProcessingOrganizationCode(procCodes.get(1));
                        }
                    }

                    cgInvoiceDocument.setAccountsReceivableDocumentHeader(accountsReceivableDocumentHeader);

                    cgInvoiceDocument.setAward(awd);
                    contractsGrantsInvoiceDocumentService.populateInvoiceFromAward(awd, accounts,cgInvoiceDocument);
                    contractsGrantsInvoiceDocumentService.createSourceAccountingLinesAndGLPEs(cgInvoiceDocument);
                    if (ObjectUtils.isNotNull(cgInvoiceDocument.getAward())) {
                        contractsGrantsInvoiceDocumentService.updateSuspensionCategoriesOnDocument(cgInvoiceDocument);
                    }

                    LOG.info("Created Contracts and Grants invoice document " + cgInvoiceDocument.getDocumentNumber());
                }
                catch (WorkflowException ex) {
                    LOG.error("Error creating cgin documents: " + ex.getMessage(), ex);
                    throw new RuntimeException("Error creating cgin documents: " + ex.getMessage(), ex);
                }
            }
            else {
                // if chart of account code or organization code is not available, output the error
                errLines.add("Award/Proposal# " + awd.getProposalNumber().toString() + " has not set correctly organizaion code or chart of account code ");
            }
        }

        return cgInvoiceDocument;
    }

    /**
     * Retrieves the awards, validates them, and then creates documents for all valid awards
     * @see org.kuali.kfs.module.ar.batch.service.ContractsGrantsInvoiceCreateDocumentService#processBatchInvoiceDocumentCreation(java.lang.String, java.lang.String)
     */
    @Override
    public void processBatchInvoiceDocumentCreation(String validationErrorOutputFileName, String invoiceDocumentErrorOutputFileName) {
        final Collection<ContractsAndGrantsBillingAward> awards = retrieveAwards();
        final Collection<ContractsAndGrantsBillingAward> validAwards = validateAwards(awards, validationErrorOutputFileName);
        createCGInvoiceDocumentsByAwards(validAwards, invoiceDocumentErrorOutputFileName);
    }


    /**
     * Validates and parses the file identified by the given files name. If successful, parsed entries are stored.
     *
     * @param fileName Name of file to be uploaded and processed.
     * @return True if the file load and store was successful, false otherwise.
     */
    protected Collection<ContractsAndGrantsBillingAward> retrieveAwards() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(KFSPropertyConstants.ACTIVE, true);
        return kualiModuleService.getResponsibleModuleService(ContractsAndGrantsBillingAward.class).getExternalizableBusinessObjectsList(ContractsAndGrantsBillingAward.class, map);
    }

    /**
     * @see org.kuali.kfs.module.ar.batch.service.ContractsGrantsInvoiceCreateDocumentService#validateAwards(java.util.Collection, java.lang.String)
     */
    @Override
    public Collection<ContractsAndGrantsBillingAward> validateAwards(Collection<ContractsAndGrantsBillingAward> awards, String errOutputFile) {
        boolean invalid;
        Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup = new HashMap<ContractsAndGrantsBillingAward, List<String>>();
        List<String> errorList = new ArrayList<String>();
        List<ContractsAndGrantsBillingAward> qualifiedAwards = new ArrayList<ContractsAndGrantsBillingAward>();
        List<ContractsAndGrantsBillingAward> tmpAwards = new ArrayList<ContractsAndGrantsBillingAward>();

        // validation for billing frequency for the collection of awards
        Collection<AccountingPeriod> accPeriods = accountingPeriodService.getAllAccountingPeriods();

        for (ContractsAndGrantsBillingAward award : awards) {
            errorList = new ArrayList<String>();
            if (award.getAwardBeginningDate() != null) {
                if (award.getPreferredBillingFrequency() != null && contractsGrantsInvoiceDocumentService.isValueOfPreferredBillingFrequencyValid(award)) {
                    tmpAwards.add(award);
                }
                else {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_BILLING_FREQUENCY_MISSING_ERROR));
                    invalidGroup.put(award, errorList);
                }
            }
            else {
                // 1.Award start date is missing
                errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_START_DATE_MISSING_ERROR));
                invalidGroup.put(award, errorList);
            }
        }

        // To check for every award if its within the billing frequency.
        Collection<ContractsAndGrantsBillingAward> awardsToBill = new ArrayList<ContractsAndGrantsBillingAward>();
        boolean valid = true;
        for (ContractsAndGrantsBillingAward awd : tmpAwards) {
            errorList = new ArrayList<String>();
            valid = verifyBillingFrequencyService.validatBillingFrequency(awd);
            if (valid) {
                awardsToBill.add(awd);
            }
            else {
                errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_INVALID_BILLING_PERIOD));
                invalidGroup.put(awd, errorList);
            }
        }

        // filter out invalid awards from the awards collection
        if (!CollectionUtils.isEmpty(awardsToBill)) {
            String proposalId;
            for (ContractsAndGrantsBillingAward award : awardsToBill) {
                invalid = false;
                errorList = new ArrayList<String>();
                // use business rules to validate awards


                // 1. Award Invoicing suspended by user.
                if (contractsGrantsInvoiceDocumentService.isAwardInvoicingSuspendedByUser(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_USER_SUSPENDED_ERROR));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }
                // 2. Award is Inactive
                if (!award.isActive()) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_INACTIVE_ERROR));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 4. Award invoicing option is missing
                if (StringUtils.isEmpty(award.getInvoicingOptions())) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_INVOICING_OPTION_MISSING_ERROR));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 5. Award preferred billing frequency is not set correctly
                if (!contractsGrantsInvoiceDocumentService.isPreferredBillingFrequencySetCorrectly(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_SINGLE_ACCOUNT_ERROR));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 6. Award has no accounts assigned
                if (contractsGrantsInvoiceDocumentService.hasNoActiveAccountsAssigned(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_NO_ACCOUNT_ASSIGNED_ERROR));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 7. Award contains expired account or accounts
                Collection<Account> expAccounts = contractsGrantsInvoiceDocumentService.getExpiredAccountsOfAward(award);
                if (ObjectUtils.isNotNull(expAccounts) && !expAccounts.isEmpty()) {

                    StringBuilder line = new StringBuilder();
                    line.append(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_CONAINS_EXPIRED_ACCOUNTS_ERROR));

                    for (Account expAccount : expAccounts) {
                        line.append(" (expired account: " + expAccount.getAccountNumber() + " expiration date " + expAccount.getAccountExpirationDate() + ") ");
                    }
                    errorList.add(line.toString());
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }
                // 8. Award has final invoice Billed already
                if (contractsGrantsInvoiceDocumentService.isAwardFinalInvoiceAlreadyBuilt(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_FINAL_BILLED_ERROR));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }


                // 9. Award has no valid milestones to invoice
                if (contractsGrantsInvoiceDocumentService.hasNoMilestonesToInvoice(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_NO_VALID_MILESTONES));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }
                // 10. All has no valid bills to invoice
                if (contractsGrantsInvoiceDocumentService.hasNoBillsToInvoice(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_NO_VALID_BILLS));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }
                // 11. Agency has no matching Customer record
                if (contractsGrantsInvoiceDocumentService.owningAgencyHasNoCustomerRecord(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_AGENCY_NO_CUSTOMER_RECORD));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 12. All accounts of an Award have zero$ to invoice
                if (!CollectionUtils.isEmpty(award.getActiveAwardAccounts()) && CollectionUtils.isEmpty(getValidAwardAccounts(award.getActiveAwardAccounts(), award))) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_NO_VALID_ACCOUNTS));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }
                // 13. Award does not have appropriate Contract Control Accounts set based on Invoicing Options
                List<String> errorString = contractsGrantsInvoiceDocumentService.checkAwardContractControlAccounts(award);
                if (!CollectionUtils.isEmpty(errorString) && errorString.size() > 1) {
                    errorList.add(configurationService.getPropertyValueAsString(errorString.get(0)).replace("{0}", errorString.get(1)));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }
                // 14. System Information and ORganization Accounting Default not setup.
                if (contractsGrantsInvoiceDocumentService.isChartAndOrgNotSetupForInvoicing(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_SYS_INFO_OADF_NOT_SETUP));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 15. if there is no AR Invoice Account present when the GLPE is 3.
                if (!contractsGrantsInvoiceDocumentService.hasARInvoiceAccountAssigned(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_NO_AR_INV_ACCOUNT));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 16. If all accounts of award has invoices in progress.
                if ((award.getPreferredBillingFrequency().equalsIgnoreCase(ArConstants.MILESTONE_BILLING_SCHEDULE_CODE) || award.getPreferredBillingFrequency().equalsIgnoreCase(ArConstants.PREDETERMINED_BILLING_SCHEDULE_CODE)) && contractsGrantsInvoiceDocumentService.isInvoiceInProgress(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_INVOICES_IN_PROGRESS));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }

                // 17. Offset Definition is not available when the GLPE is 3.
                if (contractsGrantsInvoiceDocumentService.isOffsetDefNotSetupForInvoicing(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArConstants.BatchFileSystem.CGINVOICE_CREATION_AWARD_OFFSET_DEF_NOT_SETUP));
                    invalidGroup.put(award, errorList);

                    invalid = true;
                }


                // if invalid is true, the award is unqualified.
                // records the unqualified award with failed reasons.
                if (!invalid) {
                    qualifiedAwards.add(award);
                }
            }
        }
        // print out all failed reasons if they are present
        if (!CollectionUtils.isEmpty(invalidGroup)) {
            writeErrorToFile(invalidGroup, errOutputFile);
        }
        return qualifiedAwards;
    }

    protected void writeErrorToFile(Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup, String errOutputFile) {
        PrintStream outputFileStream = null;
        File errOutPutfile = new File(errOutputFile);
        try {
            outputFileStream = new PrintStream(errOutPutfile);
            writeReportHeader(outputFileStream);

            for (ContractsAndGrantsBillingAward award : invalidGroup.keySet()) {
                writeErrorEntryByAward(award, invalidGroup.get(award), outputFileStream);
            }
            // clean the error list for next iteration
            invalidGroup.clear();

            outputFileStream.printf("\r\n");
        } catch (IOException ioe) {
            LOG.error("Could not write errors in contracts and grants invoice document creation process to file" + ioe.getMessage());
            throw new RuntimeException("Could not write errors in contracts and grants invoice document creation process to file", ioe);
        } finally {
            if (outputFileStream != null) {
                outputFileStream.close();
            }
        }
    }

    /**
     * This method retrieves all the contracts grants invoice documents with a status of Saved and
     * routes them to the next step in the routing path.
     *
     * @return True if the routing was performed successfully. A runtime exception will be thrown if any errors occur while routing.
     * @see org.kuali.kfs.module.ar.batch.service.ContractsGrantsInvoiceDocumentCreateService#routeContractsGrantsInvoiceDocuments()
     */
    @Override
    public void routeContractsGrantsInvoiceDocuments() {
        final String currentUserPrincipalId = GlobalVariables.getUserSession().getPerson().getPrincipalId();
        List<String> documentIdList = retrieveContractsGrantsInvoiceDocumentsToRoute(DocumentStatus.SAVED, currentUserPrincipalId);

        if (LOG.isInfoEnabled()) {
            LOG.info("CGinvoice to Route: " + documentIdList);
        }

        for (String cgInvoiceDocId : documentIdList) {
            try {
                ContractsGrantsInvoiceDocument cgInvoicDoc = (ContractsGrantsInvoiceDocument) documentService.getByDocumentHeaderId(cgInvoiceDocId);
                // To route documents only if the user in the session is same as the initiator.
                if (LOG.isInfoEnabled()) {
                    LOG.info("Routing Contracts Grants Invoice document # " + cgInvoiceDocId + ".");
                }
                documentService.prepareWorkflowDocument(cgInvoicDoc);

                // calling workflow service to bypass business rule checks
                workflowDocumentService.route(cgInvoicDoc.getDocumentHeader().getWorkflowDocument(), "", null);
            } catch (WorkflowException e) {
                LOG.error("Error routing document # " + cgInvoiceDocId + " " + e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * Returns a list of all saved but not yet routed contracts grants invoice documents, using the KualiWorkflowInfo service.
     *
     * @return a list of contracts grants invoice documents to route
     */
    protected List<String> retrieveContractsGrantsInvoiceDocumentsToRoute(DocumentStatus statusCode, String initiatorPrincipalId) {
        List<String> documentIds = new ArrayList<String>();

        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put(KFSPropertyConstants.WORKFLOW_DOCUMENT_TYPE_NAME, ArConstants.ArDocumentTypeCodes.CONTRACTS_GRANTS_INVOICE);
        fieldValues.put(KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE, statusCode.getCode());
        fieldValues.put(KFSPropertyConstants.INITIATOR_PRINCIPAL_ID, initiatorPrincipalId);
        Collection<FinancialSystemDocumentHeader> docHeaders = businessObjectService.findMatching(FinancialSystemDocumentHeader.class, fieldValues);

        for (FinancialSystemDocumentHeader docHeader : docHeaders) {
            documentIds.add(docHeader.getDocumentNumber());
        }
        return documentIds;
    }

    protected void writeErrorEntryByAward(ContractsAndGrantsBillingAward award, List<String> validationCategory, PrintStream printStream) throws IOException {
        // %15s %18s %20s %19s %15s %18s %23s %18s
        boolean firstLineFlag = true;
        String awardBeginningDate;
        String awardEndingDate;
        String awardTotalAmount;

        String proposalNumber = award.getProposalNumber().toString();
        Date beginningDate = award.getAwardBeginningDate();
        Date endingDate = award.getAwardEndingDate();
        KualiDecimal totalAmount = award.getAwardTotalAmount();

        if (ObjectUtils.isNotNull(beginningDate)) {
            awardBeginningDate = award.getAwardBeginningDate().toString();
        } else {
            awardBeginningDate = "null award beginning date";
        }

        if (ObjectUtils.isNotNull(beginningDate)) {
            awardEndingDate = award.getAwardEndingDate().toString();
        } else {
            awardEndingDate = "null award ending date";
        }

        if (ObjectUtils.isNotNull(totalAmount)) {
            awardTotalAmount = award.getAwardTotalAmount().toString();
        } else {
            awardTotalAmount = "null award total amount";
        }

        KualiDecimal cumulativeExpenses = KualiDecimal.ZERO;
        // calculate cumulativeExpenses
        for (ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
            cumulativeExpenses = cumulativeExpenses.add(contractsGrantsInvoiceDocumentService.getBudgetAndActualsForAwardAccount(awardAccount, ArPropertyConstants.ACTUAL_BALANCE_TYPE, award.getAwardBeginningDate()));
        }

        if (ObjectUtils.isNotNull(award)){
            boolean isActiveAwardAccountsEmpty = CollectionUtils.isEmpty(award.getActiveAwardAccounts());

            if (isActiveAwardAccountsEmpty) {
                writeToReport(proposalNumber, "", awardBeginningDate, awardEndingDate, awardTotalAmount, cumulativeExpenses.toString(), printStream);
            }
            else {
                for (ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                    if (firstLineFlag) {
                        writeToReport(proposalNumber, awardAccount.getAccountNumber(), awardBeginningDate, awardEndingDate, awardTotalAmount, cumulativeExpenses.toString(), printStream);
                        firstLineFlag = false;
                    }
                    else {
                        writeToReport("", awardAccount.getAccountNumber(), "", "", "", "", printStream);
                    }
                }
            }
        }
        // To print all the errors from the validation category.
        for (String vCat : validationCategory) {
            printStream.printf("%s", "     " + vCat);
            printStream.printf("\r\n");
        }
        printStream.printf(REPORT_LINE_DIVIDER);
        printStream.printf("\r\n");
    }

    protected void writeToReport(String proposalNumber, String accountNumber, String awardBeginningDate, String awardEndingDate, String awardTotalAmount, String cumulativeExpenses, PrintStream printStream) throws IOException {
        printStream.printf("%15s", proposalNumber);
        printStream.printf("%18s", accountNumber);
        printStream.printf("%20s", awardBeginningDate);
        printStream.printf("%19s", awardEndingDate);
        printStream.printf("%15s", awardTotalAmount);
        printStream.printf("%23s", cumulativeExpenses);
        printStream.printf("\r\n");
    }

    /**
     * @param printStream
     * @throws IOException
     */
    protected void writeReportHeader(PrintStream printStream) throws IOException {
        printStream.printf("%15s%18s%20s%19s%15s%23s\r\n", "Proposal Number", "Account Number", "Award Start Date", "Award Stop Date", "Award Total", "Cumulative Expenses");
        printStream.printf("%23s", "Validation Category");
        printStream.printf("\r\n");
        printStream.printf(REPORT_LINE_DIVIDER);
        printStream.printf("\r\n");
    }


    /**
     * This method returns the valid award accounts based on evaluation of preferred billing frequency and invoice document status
     *
     * @param awardAccounts
     * @return valid awardAccounts
     */
    protected List<ContractsAndGrantsBillingAwardAccount> getValidAwardAccounts(List<ContractsAndGrantsBillingAwardAccount> awardAccounts, ContractsAndGrantsBillingAward award) {
        if (!award.getPreferredBillingFrequency().equalsIgnoreCase(ArConstants.MILESTONE_BILLING_SCHEDULE_CODE) && !award.getPreferredBillingFrequency().equalsIgnoreCase(ArConstants.PREDETERMINED_BILLING_SCHEDULE_CODE)) {
            List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts = new ArrayList<ContractsAndGrantsBillingAwardAccount>();
            final Set<Account> invalidAccounts = harvestAccountsFromContractsGrantsInvoices(getInProgressInvoicesForAward(award));
            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (!invalidAccounts.contains(awardAccount.getAccount())) {
                    validAwardAccounts.add(awardAccount);
                }
            }

            return validAwardAccounts;
        }
        else {
            return awardAccounts;
        }

    }

    /**
     * Pulls all the unique accounts from the source accounting lines on the given ContractsGrantsInvoiceDocument
     * @param contractsGrantsInvoices the invoices to pull unique accounts from
     * @return a Set of the unique accounts
     */
    protected Set<Account> harvestAccountsFromContractsGrantsInvoices(Collection<ContractsGrantsInvoiceDocument> contractsGrantsInvoices) {
       Set<Account> accounts = new HashSet<Account>();
       for (ContractsGrantsInvoiceDocument invoice : contractsGrantsInvoices) {
           for (InvoiceAccountDetail invoiceAccountDetail : invoice.getAccountDetails()) {
               final Account account = getAccountService().getByPrimaryId(invoiceAccountDetail.getChartOfAccountsCode(), invoiceAccountDetail.getAccountNumber());
               if (!ObjectUtils.isNull(account)) {
                   accounts.add(account);
               }
           }
       }
       return accounts;
    }

    /**
     * Looks up all the in progress contracts & grants invoices for the award
     * @param award the award to look up contracts & grants invoices for
     * @return a Collection matching in progress/pending Contracts & Grants Invoice documents
     */
    protected Collection<ContractsGrantsInvoiceDocument> getInProgressInvoicesForAward(ContractsAndGrantsBillingAward award) {
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put(KFSPropertyConstants.PROPOSAL_NUMBER, award.getProposalNumber());
        fieldValues.put(KFSPropertyConstants.DOCUMENT_HEADER+"."+KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE, financialSystemDocumentService.getPendingDocumentStatuses());

        return businessObjectService.findMatching(ContractsGrantsInvoiceDocument.class, fieldValues);
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Sets the accountingPeriodService attribute value.
     *
     * @param accountingPeriodService The accountingPeriodService to set.
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }


    /**
     * Sets the verifyBillingFrequencyService attribute value.
     *
     * @param verifyBillingFrequencyService The verifyBillingFrequencyService to set.
     */
    public void setVerifyBillingFrequencyService(VerifyBillingFrequencyService verifyBillingFrequencyService) {
        this.verifyBillingFrequencyService = verifyBillingFrequencyService;
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
     * Sets the workflowDocumentService attribute value.
     *
     * @param workflowDocumentService The workflowDocumentService to set.
     */
    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }


    /**
     * Sets the documentService attribute value.
     *
     * @param documentService The documentService to set.
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }


    /**
     * Sets the accountsReceivableDocumentHeaderService attribute value.
     *
     * @param accountsReceivableDocumentHeaderService The accountsReceivableDocumentHeaderService to set.
     */
    public void setAccountsReceivableDocumentHeaderService(AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService) {
        this.accountsReceivableDocumentHeaderService = accountsReceivableDocumentHeaderService;
    }

    /**
     * Sets the contractsGrantsInvoiceDocumentService attribute value.
     *
     * @param contractsGrantsInvoiceDocumentService The contractsGrantsInvoiceDocumentService to set.
     */
    public void setContractsGrantsInvoiceDocumentService(ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService) {
        this.contractsGrantsInvoiceDocumentService = contractsGrantsInvoiceDocumentService;
    }

    /**
     * Sets the kualiModuleService attribute value.
     *
     * @param kualiModuleService The kualiModuleService to set.
     */
    @NonTransactional
    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }


    public ConfigurationService getConfigurationService() {
        return configurationService;
    }


    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }


    public ContractsAndGrantsModuleBillingService getContractsAndGrantsModuleBillingService() {
        return contractsAndGrantsModuleBillingService;
    }


    public void setContractsAndGrantsModuleBillingService(ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService) {
        this.contractsAndGrantsModuleBillingService = contractsAndGrantsModuleBillingService;
    }


    public FinancialSystemDocumentService getFinancialSystemDocumentService() {
        return financialSystemDocumentService;
    }


    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }
}
