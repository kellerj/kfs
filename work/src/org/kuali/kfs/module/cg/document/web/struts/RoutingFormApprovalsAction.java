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
package org.kuali.module.kra.routingform.web.struts.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.Constants;
import org.kuali.core.workflow.service.KualiWorkflowDocument;
import org.kuali.core.workflow.service.WorkflowDocumentService;
import org.kuali.kfs.util.SpringServiceLocator;
import org.kuali.module.kra.routingform.web.struts.form.RoutingForm;

public class RoutingFormApprovalsAction extends RoutingFormAction {
    
    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoutingForm routingForm = (RoutingForm) form;
        
        cacheAndLoad(mapping, form, request, response);
        ActionForward forward = super.save(mapping, form, request, response);
        
        routingForm.getRoutingFormDocument().populateDocumentForRouting();
        routingForm.getRoutingFormDocument().getDocumentHeader().getWorkflowDocument().saveRoutingData();
        
        return forward;
    }
    
    /**
     * @see org.kuali.core.web.struts.action.KualiDocumentActionBase#route(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoutingForm routingForm = (RoutingForm) form;
        
        if (!routingForm.isAuditErrorsPassed()) {
            return mapping.findForward(Constants.MAPPING_BASIC);
        }
        
        cacheAndLoad(mapping, form, request, response);
        
        ActionForward forward = super.route(mapping, form, request, response);
        return forward;
    }
    
    /**
     * @see org.kuali.core.web.struts.action.KualiDocumentActionBase#approve(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        cacheAndLoad(mapping, form, request, response);
        RoutingForm routingForm = (RoutingForm) form;
        
        KualiWorkflowDocument workflowDoc = routingForm.getDocument().getDocumentHeader().getWorkflowDocument();
        workflowDoc.refreshContent();
        if (workflowDoc.getDocRouteLevelName().equals("Project Director")) {
            routingForm.setAdHocRoutePersons(routingForm.getRoutingFormDocument().convertKraAdhocsToAdHocRoutePersons());
            routingForm.setAdHocRouteWorkgroups(routingForm.getRoutingFormDocument().convertKraAdhocsToAdHocRouteWorkgroups());
            // send FYIs, adhoc requests
        }
        
        ActionForward forward = super.approve(mapping, form, request, response);
        return forward;
    }
    
    private void cacheAndLoad(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoutingForm routingForm = (RoutingForm) form;
        
        List adhocPersons = routingForm.getRoutingFormDocument().getAdhocPersons();
        List adhocOrgs = routingForm.getRoutingFormDocument().getAdhocOrgs();
        List adhocWorkgroups = routingForm.getRoutingFormDocument().getAdhocWorkgroups();
        
        this.load(mapping, routingForm, request, response);

        routingForm.getRoutingFormDocument().setAdhocPersons(adhocPersons);
        routingForm.getRoutingFormDocument().setAdhocOrgs(adhocOrgs);
        routingForm.getRoutingFormDocument().setAdhocWorkgroups(adhocWorkgroups);
    }
}
