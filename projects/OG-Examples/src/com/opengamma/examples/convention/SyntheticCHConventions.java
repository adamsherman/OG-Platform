/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.convention;

import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class SyntheticCHConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final ExternalId ch = ExternalSchemes.financialRegionId("CH");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP6M"), simpleNameSecurityId("CHF LIBOR 6m")), "CHF LIBOR 6m", 
        act360, following, Period.ofMonths(6), 2, false, ch);

    //Identifiers for external data
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP1D")), "CHFCASHP1D", act360, following, Period.ofDays(1), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP1M")), "CHFCASHP1M", act360, modified, Period.ofMonths(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP2M")), "CHFCASHP2M", act360, modified, Period.ofMonths(2), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP3M")), "CHFCASHP3M", act360, modified, Period.ofMonths(3), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP4M")), "CHFCASHP4M", act360, modified, Period.ofMonths(4), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP5M")), "CHFCASHP5M", act360, modified, Period.ofMonths(5), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP6M")), "CHFCASHP6M", act360, modified, Period.ofMonths(6), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP7M")), "CHFCASHP7M", act360, modified, Period.ofMonths(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP8M")), "CHFCASHP8M", act360, modified, Period.ofMonths(8), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP9M")), "CHFCASHP9M", act360, modified, Period.ofMonths(9), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP10M")), "CHFCASHP10M", act360, modified, Period.ofMonths(10), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP11M")), "CHFCASHP11M", act360, modified, Period.ofMonths(11), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP12M")), "CHFCASHP12M", act360, modified, Period.ofMonths(12), 2, false, ch);
    
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_SWAP")), "CHF_SWAP", thirty360, modified, annual, 2, ch, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_3M_SWAP")), "CHF_3M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, quarterly, 2, simpleNameSecurityId("CHF LIBOR 3m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_6M_SWAP")), "CHF_6M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);
  }
}
