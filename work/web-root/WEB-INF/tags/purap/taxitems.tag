<%--
 Copyright 2007 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/kfs/kfsTldHeader.jsp"%>

<%@ attribute name="overrideTitle" required="false"
	description="The title to be used for this section." %>
<%@ attribute name="documentAttributes" required="false" type="java.util.Map" 
	description="The DataDictionary entry containing attributes for this row's fields." %>
<%@ attribute name="itemAttributes" required="true" type="java.util.Map"
	description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="accountingLineAttributes" required="true"
	type="java.util.Map"
	description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="descriptionFirst" required="false" type="java.lang.Boolean"
    description="Whether or not to show item description before extended price." %>
<%@ attribute name="mainColumnCount" required="true" %>
<%@ attribute name="colSpanDescription" required="true" %>
<%@ attribute name="colSpanExtendedPrice" required="true" %>
<%@ attribute name="colSpanItemType" required="true" %>
<%@ attribute name="colSpanBlank" required="true" %>

<c:if test="${empty overrideTitle}">
	<c:set var="overrideTitle" value="Tax Withholding Charges"/>
</c:if>

<c:set var="currentTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
<c:set var="topLevelTabIndex" value="${KualiForm.currentTabIndex}" scope="request" />
<c:set var="tabKey" value="${kfunc:generateTabKey(overrideTitle)}" />
<!--  hit form method to increment tab index -->
<c:set var="dummyIncrementer" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
<c:set var="currentTab" value="${kfunc:getTabState(KualiForm, tabKey)}" />

<%-- default to closed --%>
<c:choose>
	<c:when test="${empty currentTab}">
		<c:set var="isOpen" value="false" />
	</c:when>
	<c:when test="${!empty currentTab}">
		<c:set var="isOpen" value="${currentTab == 'OPEN'}" />
	</c:when>
</c:choose>
	
<tr>
	<td colspan="${mainColumnCount}" class="subhead">
		<span class="subhead-left"><c:out value="${overrideTitle}" /> &nbsp;</span>
		<c:if test="${isOpen == 'true' || isOpen == 'TRUE'}">
			<html:image property="methodToCall.toggleTab.tab${tabKey}" src="${ConfigProperties.kr.externalizable.images.url}tinybutton-hide.gif" alt="hide" title="toggle" styleClass="tinybutton" styleId="tab-${tabKey}-imageToggle"
				onclick="javascript: return toggleTab(document, '${tabKey}'); " />
		</c:if>
		<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
			<html:image property="methodToCall.toggleTab.tab${tabKey}" src="${ConfigProperties.kr.externalizable.images.url}tinybutton-show.gif" alt="show" title="toggle" styleClass="tinybutton" styleId="tab-${tabKey}-imageToggle"
				onclick="javascript: return toggleTab(document, '${tabKey}'); " />
		</c:if>
	</td>
</tr>

<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
	<tbody style="display: none;" id="tab-${tabKey}-div">
</c:if>

<tr>
	<kul:htmlAttributeHeaderCell colspan="${colSpanItemType}"
		attributeEntry="${itemAttributes.itemTypeCode}" />	
	<c:choose>
		<c:when test="${descriptionFirst}">
			<kul:htmlAttributeHeaderCell colspan="${colSpanDescription}"
				attributeEntry="${itemAttributes.itemDescription}" />
			<kul:htmlAttributeHeaderCell colspan="${colSpanExtendedPrice}"
				attributeEntry="${itemAttributes.extendedPrice}" />
			<th colspan="${colSpanBlank}">&nbsp;</th>
		</c:when>
	    <c:otherwise>
			<kul:htmlAttributeHeaderCell colspan="${colSpanExtendedPrice}"
				attributeEntry="${itemAttributes.extendedPrice}" />
			<kul:htmlAttributeHeaderCell colspan="${colSpanDescription}"
				attributeEntry="${itemAttributes.itemDescription}" />
			<th colspan="${colSpanBlank}">&nbsp;</th>
		</c:otherwise>
	</c:choose>	
</tr>

<logic:iterate indexId="ctr" name="KualiForm" property="document.items"	id="itemLine">
	<%-- to ensure order this should pull out items from APC instead of this--%>
	<c:if test="${itemLine.itemType.isTaxCharge}">
		<tr>
			<td colspan="${mainColumnCount}" class="tab-subhead" style="border-right: none;">
			<kul:htmlControlAttribute
				attributeEntry="${itemAttributes.itemTypeCode}"
				property="document.item[${ctr}].itemType.itemTypeDescription"
				readOnly="true" /> 
		</tr>
		<tr>
			<td class="infoline" colspan="${colSpanItemType}">
			    <div align="right">
			        <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemTypeCode}" property="document.item[${ctr}].itemType.itemTypeDescription" readOnly="true" />:&nbsp;
			    </div>
			</td>
			<c:choose>
				<c:when test="${descriptionFirst}">
					<td class="infoline" colspan="${colSpanDescription}">
						<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemDescription}" property="document.item[${ctr}].itemDescription" readOnly="true" />
					</td>
					<td class="infoline" colspan="${colSpanExtendedPrice}">
						<div align="right">
							<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitPrice}" property="document.item[${ctr}].itemUnitPrice" readOnly="true" styleClass="amount" />
						</div>
					</td>
					<td class="infoline" colspan="${colSpanBlank}">
						&nbsp;
					</td>
				</c:when>
    			<c:otherwise>
					<td class="infoline" colspan="${colSpanExtendedPrice}">
						<div align="right">
							<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitPrice}" property="document.item[${ctr}].itemUnitPrice" readOnly="true" styleClass="amount" />
						</div>
					</td>
					<td class="infoline" colspan="${colSpanDescription}">
						<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemDescription}" property="document.item[${ctr}].itemDescription" readOnly="true" />
					</td>
					<td class="infoline" colspan="${colSpanBlank}">
						&nbsp;
					</td>
				</c:otherwise>
			</c:choose>
		</tr>
		
		<c:if test="${empty KualiForm.editingMode['allowItemEntry'] || !empty itemLine.itemExtendedPrice}">
		    <purap:purapGeneralAccounting 
			    accountPrefix="document.item[${ctr}]."
			    itemColSpan="${mainColumnCount}" />
		</c:if>
	</c:if>
</logic:iterate>

<c:if test="${isOpen != 'true' && isOpen != 'TRUE'}">
	</tbody>
</c:if>

