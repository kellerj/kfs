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
package org.kuali.kfs.fp.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.ObjectUtils;
import org.kuali.kfs.fp.batch.service.ProcurementCardLoadTransactionsService;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.InitiateDirectoryBase;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * This is the default implementation of the ProcurementCardLoadTransactionsService interface.
 * Handles loading, parsing, and storing of incoming procurement card batch files.
 * 
 * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService
 */
public class ProcurementCardLoadTransactionsServiceImpl extends InitiateDirectoryBase implements ProcurementCardLoadTransactionsService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardLoadTransactionsServiceImpl.class);

    protected BusinessObjectService businessObjectService;
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType procurementCardInputFileType;

    /**
     * @see org.kuali.kfs.fp.batch.service.ProcurementCardLoadTransactionsService#loadProcurementCardFile(java.lang.String, org.kuali.kfs.sys.service.ReportWriterService)
     */
    public boolean loadProcurementCardFile(String fileName, ReportWriterService reportWriterService) {
        
        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());
        
        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(fileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("file to parse not found " + fileName, e1);
            throw new RuntimeException("Cannot find the file requested to be parsed " + fileName + " " + e1.getMessage(), e1);
        }

        Collection pcardTransactions = new ArrayList();
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            pcardTransactions = (Collection) batchInputFileService.parse(procurementCardInputFileType, fileByteContent);
        }
        catch (IOException e) {
            LOG.error("Error while getting file bytes:  " + e.getMessage(), e);
            reportWriterService.writeFormattedMessageLine("%s cannot be processed. \n\tFile byptes error: %s", fileName, e.getMessage());
            return false;
        }
        catch (ParseException e) {
            LOG.error("Error parsing xml " + e.getMessage());
            reportWriterService.writeFormattedMessageLine("%s cannot be processed. \n\tXML parsing error: %s", fileName, e.getMessage());
            return false;
        }

        if (pcardTransactions.isEmpty()) {
            LOG.warn("No PCard transactions in input file " + fileName);
            reportWriterService.writeFormattedMessageLine("%s is processed. No PCard transactios in file. ", fileName);
        }else{
            loadTransactions((List) pcardTransactions);
            LOG.info("Total transactions loaded: " + String.valueOf(pcardTransactions.size()));
            reportWriterService.writeFormattedMessageLine("%s is processed. %d transaction(s) loaded. ", fileName, pcardTransactions.size());
        }
        
        return true;
    }

    /**
     * Calls businessObjectService to remove all the procurement card transaction rows from the transaction load table.
     */
    public void cleanTransactionsTable() {
        businessObjectService.deleteMatching(ProcurementCardTransaction.class, new HashMap());
    }

    /**
     * Loads all the parsed XML transactions into the temp transaction table.
     * 
     * @param transactions List of ProcurementCardTransactions to load.
     */
    protected void loadTransactions(List transactions) {
        businessObjectService.save(transactions);
    }

    /**
     * Sets the businessObjectService attribute value.
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the batchInputFileService attribute value.
     * @param batchInputFileService The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Sets the procurementCardInputFileType attribute value.
     * @param procurementCardInputFileType The procurementCardInputFileType to set.
     */
    public void setProcurementCardInputFileType(BatchInputFileType procurementCardInputFileType) {
        this.procurementCardInputFileType = procurementCardInputFileType;
    }

    /**
     * @see org.kuali.kfs.sys.batch.service.impl.InitiateDirectoryImpl#getRequiredDirectoryNames()
     */
    @Override
    public List<String> getRequiredDirectoryNames() {
        return new ArrayList<String>() {{add(procurementCardInputFileType.getDirectoryPath()); }};
    }

}
