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
package org.kuali.kfs.module.tem.businessobject;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.kuali.kfs.integration.ar.AccountsReceivableModuleService;
import org.kuali.kfs.module.tem.TemConstants;
import org.kuali.kfs.module.tem.TemParameterConstants;
import org.kuali.kfs.module.tem.TemPropertyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.service.DataDictionaryService;

@Entity
@Table(name="TEM_GRP_TRVLR_T")
public class GroupTraveler extends PersistableBusinessObjectBase {

    private Integer id;
    private String documentNumber;

    private Integer financialDocumentLineNumber;
    private String name;
    private String groupTravelerTypeCode;
    private String groupTravelerEmpId;

    @Id
    @GeneratedValue(generator="TEM_GRP_TRVLR_ID_SEQ")
    @SequenceGenerator(name="TEM_GRP_TRVLR_ID_SEQ",sequenceName="TEM_GRP_TRVLR_ID_SEQ", allocationSize=5)
    @Column(name="id",nullable=false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the documentNumber attribute.
     *
     * @return Returns the documentNumber
     */
    @Column(name="FDOC_NBR")
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute.
     *
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Gets the financialDocumentLineNumber attribute.
     *
     * @return Returns the financialDocumentLineNumber
     */
    @Column(name="FDOC_LINE_NBR")
    public Integer getFinancialDocumentLineNumber() {
        return financialDocumentLineNumber;
    }

    /**
     * Sets the financialDocumentLineNumber attribute.
     *
     * @param financialDocumentLineNumber The financialDocumentLineNumber to set.
     */
    public void setFinancialDocumentLineNumber(Integer financialDocumentLineNumber) {
        this.financialDocumentLineNumber = financialDocumentLineNumber;
    }

    @Column(name="traveler_typ_cd",length=3,nullable=false)
    public String getGroupTravelerTypeCode() {
        return groupTravelerTypeCode;
    }

    public void setGroupTravelerTypeCode(String groupTravelerTypeCode) {
        this.groupTravelerTypeCode = groupTravelerTypeCode;
    }

    /**
     * Sets the group traveler type code, based on the name passed in (which can be any case, but which should be one of the standard
     * GroupTravelerType values (ie, "Student")
     * @see org.kuali.kfs.module.tem.TemConstants.GroupTravelerType
     * @param groupTravelerType the traveler type name
     */
    public void setGroupTravelerType(String groupTravelerType) {
        for (TemConstants.GroupTravelerType travelerType : TemConstants.GroupTravelerType.values()) {
            if (travelerType.toString().equalsIgnoreCase(groupTravelerType)) {
                this.groupTravelerTypeCode = travelerType.getCode();
            }
        }
    }

    @Column(name="name",length=100, nullable=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the groupTravelerEmpId attribute.
     * @return Returns the groupTravelerEmpId.
     */
    @Column(name="trvl_emp_id")
    public String getGroupTravelerEmpId() {
        return groupTravelerEmpId;
    }

    /**
     * Sets the groupTravelerEmpId attribute value.
     * @param groupTravelerEmpId The groupTravelerEmpId to set.
     */
    public void setGroupTravelerEmpId(String groupTravelerEmpId) {
        this.groupTravelerEmpId = groupTravelerEmpId;
    }

    @SuppressWarnings("rawtypes")
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap map = new LinkedHashMap();
        map.put("id", this.id);
        map.put("groupTravelerTypeCode", this.groupTravelerTypeCode);
        map.put("name", this.name);

        return map;
    }

    /**
     * Gets the travelerLabel attribute.
     * @return Returns the travelerLabel.
     */
    public String getTravelerLabel() {
        if(SpringContext.getBean(ParameterService.class).getParameterValuesAsString(TemParameterConstants.TEM_DOCUMENT.class, TemConstants.TravelParameters.NON_EMPLOYEE_TRAVELER_TYPE_CODES).contains(groupTravelerTypeCode)) {
            return SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(SpringContext.getBean(AccountsReceivableModuleService.class).createCustomer().getClass(), KFSPropertyConstants.CUSTOMER_NUMBER);
        } else {
            return SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(GroupTraveler.class, TemPropertyConstants.GROUP_TRAVELER_EMP_ID);
        }
    }

}
