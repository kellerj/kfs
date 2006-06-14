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
package org.kuali.module.gl.bo;

import org.kuali.core.bo.KualiCodeBase;

/**
 * @author jsissom
 * @version $Id: OriginEntrySource.java,v 1.7 2006-06-14 12:26:42 abyrne Exp $
 * 
 */

public class OriginEntrySource extends KualiCodeBase {

    static final private long serialVersionUID = 1l;

    public static final String SCRUBBER_VALID = "SCV";
    public static final String SCRUBBER_ERROR = "SCE";
    public static final String SCRUBBER_EXPIRED = "SCX";
    public static final String MAIN_POSTER_VALID = "MPV";
    public static final String MAIN_POSTER_ERROR = "MPE";
    public static final String REVERSAL_POSTER_VALID = "RPV";
    public static final String REVERSAL_POSTER_ERROR = "RPE";
    public static final String ICR_TRANSACTIONS = "ICR";
    public static final String ICR_POSTER_VALID = "ICRV";
    public static final String ICR_POSTER_ERROR = "ICRE";
    public static final String GENERATE_BY_EDOC = "EDOC";
    public static final String EXTERNAL = "EXT";
    public static final String YEAR_END_ENCUMBRANCE_CLOSING = "YEEC";
    public static final String ORG_REVERSION = "OR";

    // Code base has all the fields we need
}
