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
package org.kuali.kfs.module.bc.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionAppointmentFunding;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;

public class SalarySettingCalculator {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SalarySettingCalculator.class);

    /**
     * calculate the standard working hours through the given time percent
     * 
     * @param timePercent the given time percent
     * @return the standard working hour calculated from the given time percent
     */
    public static BigDecimal getStandarHours(BigDecimal timePercent) {
        BigDecimal standarHours = timePercent.multiply(BudgetParameterFinder.getWeeklyWorkingHoursAsDecimal()).divide(BCConstants.ONE_HUNDRED, 2, KualiDecimal.ROUND_BEHAVIOR);
        return standarHours;
    }

    /**
     * calcaulte the total requested csf amount for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested csf amount for the given appointment funding lines
     */
    public static KualiInteger getAppointmentRequestedCsfAmountTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        KualiInteger appointmentRequestedCsfAmountTotal = KualiInteger.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            KualiInteger requestedCsfAmount = appointmentFunding.getAppointmentRequestedCsfAmount();

            if (requestedCsfAmount != null) {
                appointmentRequestedCsfAmountTotal = appointmentRequestedCsfAmountTotal.add(requestedCsfAmount);
            }
        }

        return appointmentRequestedCsfAmountTotal;
    }

    /**
     * calcaulte the total requested csf time percent for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested csf time percent for the given appointment funding lines
     */
    public static BigDecimal getAppointmentRequestedCsfTimePercentTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        BigDecimal appointmentRequestedCsfTimePercentTotal = BigDecimal.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BigDecimal requestedCsfTimePercent = appointmentFunding.getAppointmentRequestedCsfTimePercent();

            if (requestedCsfTimePercent != null) {
                appointmentRequestedCsfTimePercentTotal = appointmentRequestedCsfTimePercentTotal.add(requestedCsfTimePercent);
            }
        }

        return appointmentRequestedCsfTimePercentTotal;
    }

    /**
     * calcaulte the total requested csf standard hours for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested csf standard hours for the given appointment funding lines
     */
    public static BigDecimal getAppointmentRequestedCsfStandardHoursTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        return getStandarHours(getAppointmentRequestedCsfTimePercentTotal(AppointmentFundings));
    }

    /**
     * calcaulte the total requested csf full time employee quantity for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested csf full time employee quantity for the given appointment funding lines
     */
    public static BigDecimal getAppointmentRequestedCsfFteQuantityTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        BigDecimal appointmentRequestedCsfFteQuantityTotal = BigDecimal.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BigDecimal requestedCsfFteQuantity = appointmentFunding.getAppointmentRequestedCsfFteQuantity();

            if (requestedCsfFteQuantity != null) {
                appointmentRequestedCsfFteQuantityTotal = appointmentRequestedCsfFteQuantityTotal.add(requestedCsfFteQuantity);
            }
        }

        return appointmentRequestedCsfFteQuantityTotal;
    }

    /**
     * calcaulte the total requested amount for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested amount for the given appointment funding lines
     */
    public static KualiInteger getAppointmentRequestedAmountTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        KualiInteger appointmentRequestedAmountTotal = KualiInteger.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            KualiInteger requestedAmount = appointmentFunding.getAppointmentRequestedAmount();

            if (requestedAmount != null) {
                appointmentRequestedAmountTotal = appointmentRequestedAmountTotal.add(requestedAmount);
            }
        }
        return appointmentRequestedAmountTotal;
    }

    /**
     * calcaulte the total requested time percent for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested time percent for the given appointment funding lines
     */
    public static BigDecimal getAppointmentRequestedTimePercentTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        BigDecimal appointmentRequestedTimePercentTotal = BigDecimal.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BigDecimal requestedTimePercent = appointmentFunding.getAppointmentRequestedTimePercent();

            if (requestedTimePercent != null) {
                appointmentRequestedTimePercentTotal = appointmentRequestedTimePercentTotal.add(requestedTimePercent);
            }
        }
        return appointmentRequestedTimePercentTotal;
    }

    /**
     * calcaulte the total requested standard hours for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested standard hours for the given appointment funding lines
     */
    public static BigDecimal getAppointmentRequestedStandardHoursTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        return getStandarHours(getAppointmentRequestedTimePercentTotal(AppointmentFundings));
    }

    /**
     * calcaulte the total requested full time employee quantity for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total requested full time employee quantity for the given appointment funding lines
     */
    public static BigDecimal getAppointmentRequestedFteQuantityTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        BigDecimal appointmentRequestedFteQuantityTotal = BigDecimal.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BigDecimal requestedFteQuantity = appointmentFunding.getAppointmentRequestedFteQuantity();

            if (requestedFteQuantity != null) {
                appointmentRequestedFteQuantityTotal = appointmentRequestedFteQuantityTotal.add(requestedFteQuantity);
            }
        }
        return appointmentRequestedFteQuantityTotal;
    }

    /**
     * calcaulte the total csf amount for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total csf amount for the given appointment funding lines
     */
    public static KualiInteger getCsfAmountTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        KualiInteger csfAmountTotal = KualiInteger.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BudgetConstructionCalculatedSalaryFoundationTracker csfTracker = appointmentFunding.getEffectiveCSFTracker();

            if (csfTracker != null && csfTracker.getCsfAmount() != null) {
                csfAmountTotal = csfAmountTotal.add(csfTracker.getCsfAmount());
            }
        }
        return csfAmountTotal;
    }

    /**
     * calcaulte the total csf time percent for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total csf time percent for the given appointment funding lines
     */
    public static BigDecimal getCsfTimePercentTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        BigDecimal csfTimePercentTotal = BigDecimal.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BudgetConstructionCalculatedSalaryFoundationTracker csfTracker = appointmentFunding.getEffectiveCSFTracker();

            if (csfTracker != null && csfTracker.getCsfTimePercent() != null) {
                csfTimePercentTotal = csfTimePercentTotal.add(csfTracker.getCsfTimePercent());
            }
        }
        return csfTimePercentTotal;
    }

    /**
     * calcaulte the total csf standard hours for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total csf standard hours for the given appointment funding lines
     */
    public static BigDecimal getCsfStandardHoursTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        return getStandarHours(getCsfTimePercentTotal(AppointmentFundings));
    }

    /**
     * calcaulte the total csf full time employee quantity for the given appointment funding lines
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return the total csf full time employee quantity for the given appointment funding lines
     */
    public static BigDecimal getCsfFullTimeEmploymentQuantityTotal(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        BigDecimal csfFullTimeEmploymentQuantityTotal = BigDecimal.ZERO;

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            BudgetConstructionCalculatedSalaryFoundationTracker csfTracker = appointmentFunding.getEffectiveCSFTracker();

            if (csfTracker != null && csfTracker.getCsfFullTimeEmploymentQuantity() != null) {
                csfFullTimeEmploymentQuantityTotal = csfFullTimeEmploymentQuantityTotal.add(csfTracker.getCsfFullTimeEmploymentQuantity());
            }
        }
        return csfFullTimeEmploymentQuantityTotal;
    }

    /**
     * Get a collection of PendingBudgetConstructionAppointmentFunding objects that are not purged and not excluded from total. This
     * is used to decide whether or not to include csf, request or requestCsf amounts in the totals. This allows marked deleted line
     * in the set, but this is benign since marked deleted lines have zero request and requestCsf amounts by definition and we want
     * marked delete csf amounts included in the totals.
     * 
     * @param AppointmentFundings the given appointment funding lines
     * @return a collection of PendingBudgetConstructionAppointmentFunding objects that are not marked as deleted
     */
    public static List<PendingBudgetConstructionAppointmentFunding> getEffectiveAppointmentFundings(List<PendingBudgetConstructionAppointmentFunding> AppointmentFundings) {
        List<PendingBudgetConstructionAppointmentFunding> effectiveAppointmentFundings = new ArrayList<PendingBudgetConstructionAppointmentFunding>();

        for (PendingBudgetConstructionAppointmentFunding appointmentFunding : AppointmentFundings) {
            // if (!appointmentFunding.isAppointmentFundingDeleteIndicator() && !appointmentFunding.isExcludedFromTotal() &&
            // !appointmentFunding.isPurged()) {
            if (!appointmentFunding.isExcludedFromTotal() && !appointmentFunding.isPurged()) {
                effectiveAppointmentFundings.add(appointmentFunding);
            }
        }

        return effectiveAppointmentFundings;
    }

    /**
     * calculate the changing percent between the requested amount and the base amount
     * 
     * @param baseAmount the given base amount
     * @param requestedAmount the requested amount
     * @return the changing percent between the requested amount and the base amount if both of amounts are numbers; otherwise,
     *         return null
     */
    public static KualiDecimal getPercentChange(KualiInteger baseAmount, KualiInteger requestedAmount) {
        KualiDecimal percentChange = null;

        if (requestedAmount != null && baseAmount != null && baseAmount.isNonZero()) {
            KualiInteger difference = requestedAmount.subtract(baseAmount);
            BigDecimal percentChangeAsBigDecimal = difference.multiply(KFSConstants.ONE_HUNDRED).divide(baseAmount);

            percentChange = new KualiDecimal(percentChangeAsBigDecimal);
        }

        return percentChange;
    }
}
