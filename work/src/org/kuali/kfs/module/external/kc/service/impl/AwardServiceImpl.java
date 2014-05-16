/*
 * Copyright 2010 The Kuali Foundation.
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
package org.kuali.kfs.module.external.kc.service.impl;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.integration.ar.AccountsReceivableModuleService;
import org.kuali.kfs.module.external.kc.KcConstants;
import org.kuali.kfs.module.external.kc.businessobject.AccountAutoCreateDefaults;
import org.kuali.kfs.module.external.kc.businessobject.Agency;
import org.kuali.kfs.module.external.kc.businessobject.Award;
import org.kuali.kfs.module.external.kc.businessobject.AwardFundManager;
import org.kuali.kfs.module.external.kc.businessobject.AwardOrganization;
import org.kuali.kfs.module.external.kc.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.external.kc.businessobject.LetterOfCreditFund;
import org.kuali.kfs.module.external.kc.businessobject.Proposal;
import org.kuali.kfs.module.external.kc.dto.AwardDTO;
import org.kuali.kfs.module.external.kc.dto.AwardFieldValuesDto;
import org.kuali.kfs.module.external.kc.dto.AwardSearchCriteriaDto;
import org.kuali.kfs.module.external.kc.service.AccountDefaultsService;
import org.kuali.kfs.module.external.kc.service.BillingFrequencyService;
import org.kuali.kfs.module.external.kc.service.ExternalizableBusinessObjectService;
import org.kuali.kfs.module.external.kc.service.KfsService;
import org.kuali.kfs.module.external.kc.util.GlobalVariablesExtractHelper;
import org.kuali.kfs.module.external.kc.webService.AwardWebSoapService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kra.external.award.AwardWebService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.bo.ExternalizableBusinessObject;

/**
 * This class was generated by Apache CXF 2.2.10
 * Thu Sep 30 05:29:28 HST 2010
 * Generated source version: 2.2.10
 *
 */

public class AwardServiceImpl implements ExternalizableBusinessObjectService {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwardServiceImpl.class);

    protected AccountDefaultsService accountDefaultsService;
    protected AccountsReceivableModuleService accountsReceivableModuleService;
    protected BillingFrequencyService billingFrequencyService;
    protected ParameterService parameterService;
    protected PersonService personService;

    protected AwardWebService getWebService() {
        // first attempt to get the service from the KSB - works when KFS & KC share a Rice instance
        AwardWebService awardWebService = (AwardWebService) GlobalResourceLoader.getService(KcConstants.Award.SERVICE);

        // if we couldn't get the service from the KSB, get as web service - for when KFS & KC have separate Rice instances
        if (awardWebService == null) {
            LOG.warn("Couldn't get AwardWebService from KSB, setting it up as SOAP web service - expected behavior for bundled Rice, but not when KFS & KC share a standalone Rice instance.");
            AwardWebSoapService ss =  null;
            try {
                ss = new AwardWebSoapService();
            }
            catch (MalformedURLException ex) {
                LOG.error("Could not intialize AwardWebSoapService: " + ex.getMessage());
                throw new RuntimeException("Could not intialize AwardWebSoapService: " + ex.getMessage());
            }
            awardWebService = ss.getAwardWebServicePort();
        }

        return awardWebService;
    }

    @Override
    public ExternalizableBusinessObject findByPrimaryKey(Map primaryKeys) {
        //use the proposal number as its the awardId on the KC side.
        AwardDTO dto  = this.getWebService().getAward((Long)primaryKeys.get("proposalNumber"));
        return awardFromDTO(dto);
    }

    @Override
    public Collection findMatching(Map fieldValues) {
        List<AwardDTO> result = null;
        AwardFieldValuesDto criteria = new AwardFieldValuesDto();
        criteria.setAwardId((Long) fieldValues.get(KFSPropertyConstants.PROPOSAL_NUMBER));
        criteria.setAwardNumber((String) fieldValues.get("awardNumber"));
        criteria.setChartOfAccounts((String) fieldValues.get(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE));
        criteria.setAccountNumber((String) fieldValues.get(KFSPropertyConstants.ACCOUNT_NUMBER));
        criteria.setPrincipalInvestigatorId((String) fieldValues.get("principalId"));

        try {
          result  = this.getWebService().getMatchingAwards(criteria);
        } catch (WebServiceException ex) {
            GlobalVariablesExtractHelper.insertError(KcConstants.WEBSERVICE_UNREACHABLE, KfsService.getWebServiceServerName());
        }

        if (result == null) {
            return new ArrayList();
        } else {
            List<Award> awards = new ArrayList<Award>();
            for (AwardDTO dto : result) {
                awards.add(awardFromDTO(dto));
            }
            return awards;
        }
    }

    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        List<AwardDTO> result = null;
        AwardSearchCriteriaDto criteria = new AwardSearchCriteriaDto();
        criteria.setAwardId(fieldValues.get(KFSPropertyConstants.PROPOSAL_NUMBER));
        criteria.setAwardNumber(fieldValues.get("awardNumber"));
        criteria.setChartOfAccounts(fieldValues.get(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE));
        criteria.setAccountNumber(fieldValues.get(KFSPropertyConstants.ACCOUNT_NUMBER));
        criteria.setPrincipalInvestigatorId(fieldValues.get("principalId"));
        criteria.setSponsorCode(fieldValues.get("agencyNumber"));
        //the below should only be passed in from the lookup framework, meaning they will all be strings
        criteria.setStartDate(fieldValues.get("awardBeginningDate"));
        criteria.setStartDateLowerBound(fieldValues.get("rangeLowerBoundKeyPrefix_awardBeginningDate"));
        criteria.setEndDate(fieldValues.get("awardEndingDate"));
        criteria.setEndDateLowerBound(fieldValues.get("rangeLowerBoundKeyPrefix_awardEndingDate"));
        criteria.setBillingFrequency(fieldValues.get("awardBillingFrequency"));
        criteria.setAwardTotal(fieldValues.get("awardTotal"));
        try {
          result  = this.getWebService().searchAwards(criteria);
        } catch (WebServiceException ex) {
            GlobalVariablesExtractHelper.insertError(KcConstants.WEBSERVICE_UNREACHABLE, KfsService.getWebServiceServerName());
        }

        if (result == null) {
            return new ArrayList();
        } else {
            List<Award> awards = new ArrayList<Award>();
            for (AwardDTO dto : result) {
                awards.add(awardFromDTO(dto));
            }
            return awards;
        }
    }

    protected Award awardFromDTO(AwardDTO kcAward) {
        Award award = new Award();
        award.setProposalNumber(kcAward.getAwardId());
        award.setAwardNumber(kcAward.getAwardNumber());
        award.setAwardBeginningDate(kcAward.getAwardStartDate() == null ? null : new java.sql.Date(kcAward.getAwardStartDate().getDate()));
        award.setAwardEndingDate(kcAward.getAwardEndDate() == null ? null : new java.sql.Date(kcAward.getAwardEndDate().getDate()));
        award.setAwardTotalAmount(kcAward.getAwardTotalAmount());
        award.setAwardDirectCostAmount(kcAward.getAwardDirectCostAmount());
        award.setAwardIndirectCostAmount(kcAward.getAwardIndirectCostAmount());
        award.setAwardDocumentNumber(kcAward.getAwardDocumentNumber());
        award.setAwardLastUpdateDate(kcAward.getAwardLastUpdateDate() == null ? null : new java.sql.Timestamp(kcAward.getAwardLastUpdateDate().getDate()));
        award.setAwardCreateTimestamp(kcAward.getAwardCreateTimestamp() == null ? null : new java.sql.Timestamp(kcAward.getAwardCreateTimestamp().getDate()));
        award.setProposalAwardTypeCode(kcAward.getProposalAwardTypeCode());
        award.setAwardStatusCode(kcAward.getAwardStatusCode());
        award.setAgencyNumber(kcAward.getSponsorCode());
        award.setAwardTitle(kcAward.getTitle());
        award.setAwardCommentText(kcAward.getAwardCommentText());
        award.setAgency(new Agency(kcAward.getSponsor()));
        if (kcAward.getProposal() != null) {
            award.setProposal(new Proposal(kcAward.getProposal()));
            award.getProposal().setAward(award);
        }
        award.setAdditionalFormsRequiredIndicator(kcAward.isAdditionalFormsRequired());
        award.setAutoApproveIndicator(kcAward.isAutoApproveInvoice());
        award.setMinInvoiceAmount(kcAward.getMinInvoiceAmount());
        award.setAdditionalFormsDescription(kcAward.getAdditionalFormsDescription());
        award.setStopWorkIndicator(kcAward.isStopWork());
        award.setCommentText(kcAward.getStopWorkReason());
        award.setInvoicingOptions(kcAward.getInvoicingOption());
        award.setDunningCampaign(kcAward.getDunningCampaignId());
        if (StringUtils.isNotEmpty(kcAward.getFundManagerId())) {
            award.setAwardPrimaryFundManager(new AwardFundManager(award.getProposalNumber(), kcAward.getFundManagerId()));
        }
        AccountAutoCreateDefaults defaults = getAccountDefaultsService().getAccountDefaults(kcAward.getUnitNumber());
        if (defaults != null) {
            AwardOrganization awardOrg = new AwardOrganization();
            awardOrg.setActive(true);
            awardOrg.setAwardPrimaryOrganizationIndicator(true);
            awardOrg.setChartOfAccountsCode(defaults.getChartOfAccountsCode());
            awardOrg.setChartOfAccounts(defaults.getChartOfAccounts());
            awardOrg.setOrganization(defaults.getOrganization());
            awardOrg.setOrganizationCode(defaults.getOrganizationCode());
            awardOrg.setProposalNumber(award.getProposalNumber());
            award.setPrimaryAwardOrganization(awardOrg);
        }
        if (kcAward.getMethodOfPayment() != null) {
            award.setLetterOfCreditFundCode(kcAward.getMethodOfPayment().getMethodOfPaymentCode());
            award.setLetterOfCreditFund(new LetterOfCreditFund(kcAward.getMethodOfPayment().getMethodOfPaymentCode(), kcAward.getMethodOfPayment().getDescription()));
        }
        award.setBillingFrequency(getBillingFrequencyService().createBillingFrequency(kcAward.getInvoiceBillingFrequency()));
        award.setSuspendInvoicingIndicator(getDoNotInvoiceStatuses().contains(kcAward.getAwardStatusCode()));
        award.setAwardPrimaryProjectDirector(getProjectDirector(kcAward));
        return award;
    }

    protected AwardProjectDirector getProjectDirector(AwardDTO kcAward) {
        AwardProjectDirector director = new AwardProjectDirector();
        director.setPrincipalId(kcAward.getPrincipalInvestigatorId());
        director.setProjectDirector(getPersonService().getPerson(kcAward.getPrincipalInvestigatorId()));
        director.setProposalNumber(kcAward.getAwardId());
        return director;
    }

    protected Collection<String> getDoNotInvoiceStatuses() {
        return accountsReceivableModuleService.getDoNotInvoiceStatuses();
    }

    protected AccountDefaultsService getAccountDefaultsService() {
        return accountDefaultsService;
    }

    public void setAccountDefaultsService(AccountDefaultsService accountDefaultsService) {
        this.accountDefaultsService = accountDefaultsService;
    }

    public AccountsReceivableModuleService getAccountsReceivableModuleService() {
        return accountsReceivableModuleService;
    }

    public void setAccountsReceivableModuleService(AccountsReceivableModuleService accountsReceivableModuleService) {
        this.accountsReceivableModuleService = accountsReceivableModuleService;
    }

    protected ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    protected BillingFrequencyService getBillingFrequencyService() {
        return billingFrequencyService;
    }

    public void setBillingFrequencyService(BillingFrequencyService billingFrequencyService) {
        this.billingFrequencyService = billingFrequencyService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

 }
