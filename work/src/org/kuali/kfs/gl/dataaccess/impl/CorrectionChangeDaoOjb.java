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
package org.kuali.module.gl.dao.ojb;

import java.util.Collection;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.module.gl.bo.CorrectionChange;
import org.kuali.module.gl.dao.CorrectionChangeDao;
import org.springframework.orm.ojb.PersistenceBrokerTemplate;

public class CorrectionChangeDaoOjb extends PersistenceBrokerTemplate implements CorrectionChangeDao {

    public CorrectionChangeDaoOjb() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kuali.module.gl.dao.CorrectionReplacementSpecificationDao#delete(org.kuali.module.gl.bo.CorrectionReplacementSpecification)
     */
    public void delete(CorrectionChange spec) {
        super.delete(spec);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kuali.module.gl.dao.CorrectionChangeDao#findByDocumentHeaderIdAndCorrectionGroupNumber(java.lang.Integer,
     *      java.lang.Integer)
     */
    public Collection findByDocumentHeaderIdAndCorrectionGroupNumber(Integer documentNumber, Integer correctionGroupNumber) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("DOC_HDR_ID", documentNumber);
        criteria.addEqualTo("GL_COR_CHG_GRP_LN_NBR", correctionGroupNumber);

        Class clazz = CorrectionChange.class;
        QueryByCriteria query = QueryFactory.newQuery(clazz, criteria);

        Collection specifications = getCollectionByQuery(query);

        return specifications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kuali.module.gl.dao.CorrectionChangeDao#save(org.kuali.module.gl.bo.CorrectionChange)
     */
    public void save(CorrectionChange spec) {
        super.store(spec);
    }

}
