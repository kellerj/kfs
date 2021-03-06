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
package org.kuali.kfs.module.tem.web;

import static java.lang.Class.forName;

import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.kuali.kfs.module.tem.businessobject.AccountingDocumentRelationship;
import org.kuali.kfs.module.tem.document.service.AccountingDocumentRelationshipService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.keyvalues.KeyValuesFinder;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Full of static methods for JSTL function access.
 *
 */
public final class JstlFunctions {
    protected static final String SETTING_PARAMS_PROLOG = "Setting params ";
    protected static final String PROPERTY_SETTING_EXC_PROLOG = "Could not set property ";
    protected static final String IN_PREPOSITION = " in ";
    protected static final String VALUES_FINDER_CLASS_EXC_PROLOG = "Could not find valuesFinder class ";
    protected static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(JstlFunctions.class);

    private JstlFunctions() {}

    /**
     * Returns a list of key/value pairs for displaying in an HTML option for a select list. This is a customized approach to retrieving
     * key/value data from database based on criteria specified in the <code>params {@link Map}</code><br/>
     * <br/>
     * Here is an example of how the code is used from a JSP:<br/>
     * <code>
     * <jsp:useBean id="paramMap" class="java.util.HashMap"/>
                    <c:set target="${paramMap}" property="forAddedPerson" value="true" />
                    <kul:checkErrors keyMatch="${proposalPerson}.proposalPersonRoleId" auditMatch="${proposalPerson}.proposalPersonRoleId"/>
                    <c:set var="roleStyle" value=""/>
                    <c:if test="${hasErrors==true}">
                        <c:set var="roleStyle" value="background-color:#FFD5D5"/>
                    </c:if>
                    <html:select property="${proposalPerson}.proposalPersonRoleId" tabindex="0" style="${roleStyle}">
                    <c:forEach items="${krafn:getOptionList('org.kuali.kra.proposaldevelopment.lookup.keyvalue.ProposalPersonRoleValuesFinder', paramMap)}" var="option">
                    <c:choose>
                        <c:when test="${KualiForm.document.proposalPersons[personIndex].proposalPersonRoleId == option.key}">
                        <option value="${option.key}" selected>${option.label}</option>
                        </c:when>
                        <c:otherwise>
                        <option value="${option.key}">${option.label}</option>
                        </c:otherwise>
                    </c:choose>
                    </c:forEach>
                    </html:select>
       </code>
     *
     *
     * @param valuesFinderClassName
     * @param params mapped parameters
     * @return List of key values
     */
    @SuppressWarnings("unchecked")
    public static List getOptionList(String valuesFinderClassName, Map params) {
        return setupValuesFinder(valuesFinderClassName, params).getKeyValues();
    }

    /**
     * Initiates the values finder by its <code>valuesFinderClassName</code>. First locates the class in the class path. Then,
     * creates an instance of it. A <code>{@link Map}</code> of key/values <code>{@link String}</code> instances a is used
     * to set properties on the values finder instance. Uses the apache <code>{@link PropertyUtils}</code> class to set properties
     * by the name of the key in the <code>{@link Map}</code>.<br/>
     * <br/>
     * Basically, a new values finder is created. the <code>params</code> parameter is a <code>{@link Map}</code> of arbitrary values
     * mapped to properties of the values finder class.<br/>
     * <br/>
     * Since this is so flexible and the ambiguity of properties referenced in the <code>{@link Map}</code>, a number of exceptions are caught
     * if a property cannot be set or if the values finder cannot be instantiated. All of these exceptions are handled within the method. None
     * of these exceptions are thrown back.
     *
     *
     * @param valuesFinderClassName
     * @param params
     * @return KeyValuesFinder
     * @see PropertyUtils#setProperty(Object, String, Object)
     */
    private static KeyValuesFinder setupValuesFinder(String valuesFinderClassName, Map<String, Object> params) {
        KeyValuesFinder retval = getKeyFinder(valuesFinderClassName);

        if(LOG.isDebugEnabled()) {
            LOG.debug(SETTING_PARAMS_PROLOG + params);
        }

        addParametersToFinder(params, retval);

        return retval;
    }

    private static void addParametersToFinder(Map<String, Object> params, KeyValuesFinder finder) {
        if (finder != null && params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                try {
                    BeanUtils.setProperty(finder, entry.getKey(), entry.getValue());
//                    setProperty(finder, entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    warn(PROPERTY_SETTING_EXC_PROLOG + entry.getKey(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    private static KeyValuesFinder getKeyFinder(String valuesFinderClassName) {
        KeyValuesFinder retval = null;
        try {
            retval = (KeyValuesFinder) forName(valuesFinderClassName).newInstance();
        } catch (ClassNotFoundException e) {
            warnAboutValueFinderClassExceptions(valuesFinderClassName, e);
        } catch (InstantiationException e) {
            warnAboutValueFinderClassExceptions(valuesFinderClassName, e);
        } catch (IllegalAccessException e) {
            warnAboutValueFinderClassExceptions(valuesFinderClassName, e);
        }
        return retval;
    }

    private static void warnAboutValueFinderClassExceptions(String valuesFinderClassName, Exception e) {
        warn(VALUES_FINDER_CLASS_EXC_PROLOG + valuesFinderClassName, e);
    }

    private static void warn(String message, Exception e) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(new StringBuilder(message).append(IN_PREPOSITION).append(buildTraceMessage(e)));
        }
    }

    /**
     * Get the stack trace from a <code>{@link Throwable}</code> and create a log message from it for tracing purposes
     *
     * @param thrownObj
     * @return String log message
     */
    private static String buildTraceMessage(Throwable thrownObj) {
        StackTraceElement stackTraceElement = thrownObj.getStackTrace()[0];
        return new StringBuilder(stackTraceElement.getClassName())
                        .append("#")
                        .append(stackTraceElement.getMethodName())
                        .append(":")
                        .append(stackTraceElement.getLineNumber())
                        .append(" ")
                        .append(thrownObj.getClass().getSimpleName())
                        .append("\n")
                        .append(thrownObj.getMessage())
                        .toString();
    }

    public static Boolean canDeleteDocumentRelationship(String documentNumber, String relDocumentNumber){
        AccountingDocumentRelationshipService accountingDocumentRelationshipService = SpringContext.getBean(AccountingDocumentRelationshipService.class);
        List<AccountingDocumentRelationship> adrList = accountingDocumentRelationshipService.find(new AccountingDocumentRelationship(documentNumber, relDocumentNumber));

        if(adrList != null && adrList.size() == 1){
            return adrList.get(0).getPrincipalId().equals(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        }

        return false;
    }

    public static KualiDecimal add(Object a, Object b){
        KualiDecimal tempA = new KualiDecimal(0);
        KualiDecimal tempB = new KualiDecimal(0);
        if (a instanceof Double){
            tempA = new KualiDecimal((Double)a);
        }
        else if (a instanceof Integer){
            tempA = new KualiDecimal((Integer)a);
        }
        else if (a instanceof String){
            tempA = new KualiDecimal((String)a);
        }
        else if (a instanceof KualiDecimal){
            tempA = (KualiDecimal)a;
        }

        if (b instanceof Double){
            tempB = new KualiDecimal((Double)b);
        }
        else if (b instanceof Integer){
            tempB = new KualiDecimal((Integer)b);
        }
        else if (a instanceof String){
            tempB = new KualiDecimal((String)b);
        }
        else if (b instanceof KualiDecimal){
            tempB = (KualiDecimal)b;
        }
        return tempA.add(tempB);
    }
}
