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
package org.kuali.module.gl.service;

import java.io.InputStream;
import java.util.List;

public interface CollectorService {
    /**
     * Given an inputStream to the collector XML, load the data into the origin entry table. If there are errors loading the file,
     * throw a CollectorLoadException that has a list of all the issues.
     * 
     * @param inputStream inputStream of collector XML
     * @return origin entry group ID
     */
    public int loadCollectorFile(InputStream inputStream);

    /**
     * Given a group ID, scrub all of the transactions. If there are errors with the data, return a list of them. If there are no
     * errors, return an empty list.
     * 
     * @param groupId
     * @return list of errors. Empty list if there are no errors.
     */
    public List scrubCollectorGroup(int groupId);
}
