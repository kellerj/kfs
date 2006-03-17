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
import org.kuali.module.gl.bo.SufficientFundBalances;
import org.kuali.module.gl.dao.SufficientFundBalancesDao;
import org.springframework.orm.ojb.support.PersistenceBrokerDaoSupport;

/**
 * @author jsissom
 * 
 */
public class SufficientFundBalancesDaoOjb extends PersistenceBrokerDaoSupport implements SufficientFundBalancesDao {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SufficientFundBalancesDaoOjb.class);

    public SufficientFundBalancesDaoOjb() {
        super();
    }

    public SufficientFundBalances getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber,
            String financialObjectCode) {
        LOG.debug("getByPrimaryId() started");

        Criteria crit = new Criteria();
        crit.addEqualTo("universityFiscalYear", universityFiscalYear);
        crit.addEqualTo("chartOfAccountsCode", chartOfAccountsCode);
        crit.addEqualTo("accountNumber", accountNumber);
        crit.addEqualTo("financialObjectCode", financialObjectCode);

        QueryByCriteria qbc = QueryFactory.newQuery(SufficientFundBalances.class, crit);
        return (SufficientFundBalances) getPersistenceBrokerTemplate().getObjectByQuery(qbc);
    }

    public void save(SufficientFundBalances sfb) {
        LOG.debug("save() started");

        getPersistenceBrokerTemplate().store(sfb);
    }

    public Collection getByObjectCode(Integer universityFiscalYear, String chartOfAccountsCode, String financialObjectCode) {
        LOG.debug("getByObjectCode() started");

        Criteria crit = new Criteria();
        crit.addEqualTo("universityFiscalYear", universityFiscalYear);
        crit.addEqualTo("chartOfAccountsCode", chartOfAccountsCode);
        crit.addEqualTo("financialObjectCode", financialObjectCode);

        QueryByCriteria qbc = QueryFactory.newQuery(SufficientFundBalances.class, crit);
        return getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
    }

    public void deleteByAccountNumber(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber) {
        LOG.debug("deleteByAccountNumber() started");

        Criteria crit = new Criteria();
        crit.addEqualTo("universityFiscalYear", universityFiscalYear);
        crit.addEqualTo("chartOfAccountsCode", chartOfAccountsCode);
        crit.addEqualTo("accountNumber", accountNumber);

        QueryByCriteria qbc = QueryFactory.newQuery(SufficientFundBalances.class, crit);
        getPersistenceBrokerTemplate().deleteByQuery(qbc);
    }

    /**
     * This method should only be used in unit tests.  It loads all the 
     * gl_sf_balances_t rows in memory into a collection.  This won't 
     * sace for production.
     * 
     * @return
     */
    public Collection testingGetAllEntries() {
      LOG.debug("testingGetAllEntries() started");

      Criteria criteria = new Criteria();
      QueryByCriteria qbc = QueryFactory.newQuery(SufficientFundBalances.class, criteria);
      // TODO: what order should these be in?
      return getPersistenceBrokerTemplate().getCollectionByQuery(qbc);    
    }
}
