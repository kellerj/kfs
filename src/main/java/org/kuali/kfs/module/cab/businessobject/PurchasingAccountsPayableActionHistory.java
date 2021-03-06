/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.cab.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * @author Kuali Nervous System Team (kualidev@oncourse.iu.edu)
 */
public class PurchasingAccountsPayableActionHistory extends PersistableBusinessObjectBase {

    private Long actionIdentifier;
    private String actionTypeCode;
    private String fromDocumentNumber;
    private Integer fromPurApLineItemIdentifier;
    private Integer fromCabLineNumber;
    private String toDocumentNumber;
    private Integer toPurApLineItemIdentifier;
    private Integer toCabLineNumber;
    private Long generalLedgerAccountIdentifier;
    private KualiDecimal itemAccountTotalAmount;
    private KualiDecimal accountsPayableItemQuantity;
    private boolean active;

    public PurchasingAccountsPayableActionHistory() {
        
    }
    
    public PurchasingAccountsPayableActionHistory(PurchasingAccountsPayableItemAsset fromItem, PurchasingAccountsPayableItemAsset toItem, String actionType) {
        this.actionTypeCode = actionType;
        this.fromDocumentNumber = fromItem.getDocumentNumber();
        this.fromPurApLineItemIdentifier = fromItem.getAccountsPayableLineItemIdentifier();
        this.fromCabLineNumber = fromItem.getCapitalAssetBuilderLineNumber();
        if (toItem != null) {
            this.toDocumentNumber = toItem.getDocumentNumber();
            this.toPurApLineItemIdentifier = toItem.getAccountsPayableLineItemIdentifier();
            this.toCabLineNumber = toItem.getCapitalAssetBuilderLineNumber();
        }
        this.active = true;
    }

    
    /**
     * Gets the actionIdentifier attribute.
     * 
     * @return Returns the actionIdentifier.
     */
    public Long getActionIdentifier() {
        return actionIdentifier;
    }


    /**
     * Sets the actionIdentifier attribute value.
     * 
     * @param actionIdentifier The actionIdentifier to set.
     */
    public void setActionIdentifier(Long actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }


    /**
     * Gets the actionTypeCode attribute.
     * 
     * @return Returns the actionTypeCode.
     */
    public String getActionTypeCode() {
        return actionTypeCode;
    }


    /**
     * Sets the actionTypeCode attribute value.
     * 
     * @param actionTypeCode The actionTypeCode to set.
     */
    public void setActionTypeCode(String actionTypeCode) {
        this.actionTypeCode = actionTypeCode;
    }


    /**
     * Gets the fromDocumentNumber attribute.
     * 
     * @return Returns the fromDocumentNumber.
     */
    public String getFromDocumentNumber() {
        return fromDocumentNumber;
    }


    /**
     * Sets the fromDocumentNumber attribute value.
     * 
     * @param fromDocumentNumber The fromDocumentNumber to set.
     */
    public void setFromDocumentNumber(String fromDocumentNumber) {
        this.fromDocumentNumber = fromDocumentNumber;
    }


    /**
     * Gets the fromPurApLineItemIdentifier attribute.
     * 
     * @return Returns the fromPurApLineItemIdentifier.
     */
    public Integer getFromPurApLineItemIdentifier() {
        return fromPurApLineItemIdentifier;
    }


    /**
     * Sets the fromPurApLineItemIdentifier attribute value.
     * 
     * @param fromPurApLineItemIdentifier The fromPurApLineItemIdentifier to set.
     */
    public void setFromPurApLineItemIdentifier(Integer fromPurApLineItemIdentifier) {
        this.fromPurApLineItemIdentifier = fromPurApLineItemIdentifier;
    }


    /**
     * Gets the fromCabLineNumber attribute.
     * 
     * @return Returns the fromCabLineNumber.
     */
    public Integer getFromCabLineNumber() {
        return fromCabLineNumber;
    }


    /**
     * Sets the fromCabLineNumber attribute value.
     * 
     * @param fromCabLineNumber The fromCabLineNumber to set.
     */
    public void setFromCabLineNumber(Integer fromCabLineNumber) {
        this.fromCabLineNumber = fromCabLineNumber;
    }


    /**
     * Gets the toDocumentNumber attribute.
     * 
     * @return Returns the toDocumentNumber.
     */
    public String getToDocumentNumber() {
        return toDocumentNumber;
    }


    /**
     * Sets the toDocumentNumber attribute value.
     * 
     * @param toDocumentNumber The toDocumentNumber to set.
     */
    public void setToDocumentNumber(String toDocumentNumber) {
        this.toDocumentNumber = toDocumentNumber;
    }


    /**
     * Gets the toPurApLineItemIdentifier attribute.
     * 
     * @return Returns the toPurApLineItemIdentifier.
     */
    public Integer getToPurApLineItemIdentifier() {
        return toPurApLineItemIdentifier;
    }


    /**
     * Sets the toPurApLineItemIdentifier attribute value.
     * 
     * @param toPurApLineItemIdentifier The toPurApLineItemIdentifier to set.
     */
    public void setToPurApLineItemIdentifier(Integer toPurApLineItemIdentifier) {
        this.toPurApLineItemIdentifier = toPurApLineItemIdentifier;
    }


    /**
     * Gets the toCabLineNumber attribute.
     * 
     * @return Returns the toCabLineNumber.
     */
    public Integer getToCabLineNumber() {
        return toCabLineNumber;
    }


    /**
     * Sets the toCabLineNumber attribute value.
     * 
     * @param toCabLineNumber The toCabLineNumber to set.
     */
    public void setToCabLineNumber(Integer toCabLineNumber) {
        this.toCabLineNumber = toCabLineNumber;
    }


    /**
     * Gets the generalLedgerAccountIdentifier attribute.
     * 
     * @return Returns the generalLedgerAccountIdentifier.
     */
    public Long getGeneralLedgerAccountIdentifier() {
        return generalLedgerAccountIdentifier;
    }


    /**
     * Sets the generalLedgerAccountIdentifier attribute value.
     * 
     * @param generalLedgerAccountIdentifier The generalLedgerAccountIdentifier to set.
     */
    public void setGeneralLedgerAccountIdentifier(Long generalLedgerAccountIdentifier) {
        this.generalLedgerAccountIdentifier = generalLedgerAccountIdentifier;
    }


    /**
     * Gets the itemAccountTotalAmount attribute.
     * 
     * @return Returns the itemAccountTotalAmount.
     */
    public KualiDecimal getItemAccountTotalAmount() {
        return itemAccountTotalAmount;
    }


    /**
     * Sets the itemAccountTotalAmount attribute value.
     * 
     * @param itemAccountTotalAmount The itemAccountTotalAmount to set.
     */
    public void setItemAccountTotalAmount(KualiDecimal itemAccountTotalAmount) {
        this.itemAccountTotalAmount = itemAccountTotalAmount;
    }


    /**
     * Gets the accountsPayableItemQuantity attribute.
     * 
     * @return Returns the accountsPayableItemQuantity.
     */
    public KualiDecimal getAccountsPayableItemQuantity() {
        return accountsPayableItemQuantity;
    }


    /**
     * Sets the accountsPayableItemQuantity attribute value.
     * 
     * @param accountsPayableItemQuantity The accountsPayableItemQuantity to set.
     */
    public void setAccountsPayableItemQuantity(KualiDecimal accountsPayableItemQuantity) {
        this.accountsPayableItemQuantity = accountsPayableItemQuantity;
    }


    /**
     * Gets the active attribute.
     * 
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Sets the active attribute value.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }


    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("actionIdentifier", this.actionIdentifier);
        return m;
    }

}
