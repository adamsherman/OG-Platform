/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
@Deprecated
public final class FixedIncomeInstrumentCurveExposureHelper {

  private FixedIncomeInstrumentCurveExposureHelper() {
  }

  public static String[] getCurveNamesForFundingCurveInstrument(final StripInstrumentType type, final String fundingCurveName, final String forwardCurveName) {
    switch (type) {
      case SWAP_3M:
        return new String[] {fundingCurveName, forwardCurveName };
      case SWAP_6M:
        return new String[] {fundingCurveName, forwardCurveName };
      case CASH:
        return new String[] {fundingCurveName };
      case FRA_3M:
        return new String[] {fundingCurveName, forwardCurveName };
      case FRA_6M:
        return new String[] {fundingCurveName, forwardCurveName };
      case FUTURE:
        return new String[] {fundingCurveName, forwardCurveName };
      case LIBOR:
        return new String[] {fundingCurveName };
      case EURIBOR:
        return new String[] {fundingCurveName };
      case CDOR:
        return new String[] {fundingCurveName };
      case CIBOR:
        return new String[] {fundingCurveName };
      case STIBOR:
        return new String[] {fundingCurveName };
      case TENOR_SWAP:
        return new String[] {fundingCurveName, forwardCurveName, fundingCurveName };
      case OIS_SWAP:
        return new String[] {fundingCurveName, fundingCurveName };
      case PERIODIC_ZERO_DEPOSIT:
        return new String[] {fundingCurveName };
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in funding curve instrument list");
    }
  }

  public static String[] getCurveNamesForForwardCurveInstrument(final StripInstrumentType type, final String fundingCurveName, final String forwardCurveName) {
    switch (type) {
      case SWAP_3M:
        return new String[] {fundingCurveName, forwardCurveName };
      case SWAP_6M:
        return new String[] {fundingCurveName, forwardCurveName };
      case CASH:
        return new String[] {forwardCurveName };
      case FRA_3M:
        return new String[] {fundingCurveName, forwardCurveName };
      case FRA_6M:
        return new String[] {fundingCurveName, forwardCurveName };
      case FUTURE:
        return new String[] {fundingCurveName, forwardCurveName };
      case LIBOR:
        return new String[] {forwardCurveName };
      case EURIBOR:
        return new String[] {forwardCurveName };
      case CDOR:
        return new String[] {forwardCurveName };
      case CIBOR:
        return new String[] {forwardCurveName };
      case STIBOR:
        return new String[] {forwardCurveName };
      case TENOR_SWAP:
        return new String[] {fundingCurveName, fundingCurveName, forwardCurveName };
      case OIS_SWAP:
        return new String[] {fundingCurveName, fundingCurveName };
      case PERIODIC_ZERO_DEPOSIT:
        return new String[] {forwardCurveName };
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in forward curve instrument list");
    }
  }

  public static String[] getCurveNamesForSecurity(final FinancialSecurity security, final String fundingCurveName, final String forwardCurveName) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
    switch (type) {
      case SWAP_FIXED_IBOR:
        return new String[] {fundingCurveName, forwardCurveName };
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return new String[] {fundingCurveName, forwardCurveName };
      case SWAP_IBOR_IBOR:
        return new String[] {fundingCurveName, forwardCurveName, forwardCurveName };
      case CASH:
        return new String[] {fundingCurveName };
      case FRA:
        return new String[] {fundingCurveName, forwardCurveName };
      case IR_FUTURE:
        return new String[] {fundingCurveName, forwardCurveName };
      case COUPON_BOND:
        return new String[] {fundingCurveName, fundingCurveName };
      case SWAP_FIXED_CMS:
        return new String[] {fundingCurveName, forwardCurveName };
      case SWAP_IBOR_CMS:
        return new String[] {fundingCurveName, forwardCurveName };
      case SWAP_CMS_CMS:
        return new String[] {fundingCurveName, forwardCurveName };
      case BOND_FUTURE:
        return new String[] {fundingCurveName, fundingCurveName };
      case SWAP_FIXED_OIS:
        return new String[] {fundingCurveName, fundingCurveName };
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in security instrument list");
    }
  }

  public static String[] getCurveNamesForSecurity(final FinancialSecurity security, final String[] curveNames) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
    final String fundingCurveName = curveNames[0];
    final String forward1CurveName = curveNames.length > 1 ? curveNames[1] : fundingCurveName;
    final String forward2CurveName = curveNames.length == 3 ? curveNames[2] : forward1CurveName;
    switch (type) {
      case SWAP_FIXED_IBOR:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_IBOR_IBOR:
        return new String[] {fundingCurveName, forward1CurveName, forward2CurveName};
      case CASH:
        return new String[] {fundingCurveName };
      case FRA:
        return new String[] {fundingCurveName, forward1CurveName };
      case IR_FUTURE:
        return new String[] {fundingCurveName, forward1CurveName };
      case COUPON_BOND:
        return new String[] {fundingCurveName, fundingCurveName };
      case SWAP_FIXED_CMS:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_IBOR_CMS:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_CMS_CMS:
        return new String[] {fundingCurveName, forward1CurveName };
      case BOND_FUTURE:
        return new String[] {fundingCurveName, fundingCurveName };
      case SWAP_FIXED_OIS:
        return new String[] {fundingCurveName, fundingCurveName };
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in security instrument list");
    }
  }
  
  public static String[] getCurveNamesForSecurity(final FinancialSecurity security, final String[] curveNames, final Frequency resetFrequency) {
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
    final String fundingCurveName = curveNames[0];
    final String forward1CurveName = curveNames.length > 1 ? curveNames[1] : fundingCurveName;
    final String forward2CurveName = curveNames.length == 3 ? curveNames[2] : forward1CurveName;
    switch (type) {
      case SWAP_FIXED_IBOR:
        if (resetFrequency.getConventionName().equals(Frequency.QUARTERLY_NAME)) {
          return new String[] {fundingCurveName, forward1CurveName};
        } else if (resetFrequency.getConventionName().equals(Frequency.SEMI_ANNUAL_NAME)) {
          return new String[] {fundingCurveName, forward2CurveName};
        }
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_IBOR_IBOR:
        return new String[] {fundingCurveName, forward1CurveName, forward2CurveName};
      case CASH:
        return new String[] {fundingCurveName };
      case FRA:
        return new String[] {fundingCurveName, forward1CurveName };
      case IR_FUTURE:
        return new String[] {fundingCurveName, forward1CurveName };
      case COUPON_BOND:
        return new String[] {fundingCurveName, fundingCurveName };
      case SWAP_FIXED_CMS:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_IBOR_CMS:
        return new String[] {fundingCurveName, forward1CurveName };
      case SWAP_CMS_CMS:
        return new String[] {fundingCurveName, forward1CurveName };
      case BOND_FUTURE:
        return new String[] {fundingCurveName, fundingCurveName };
      case SWAP_FIXED_OIS:
        return new String[] {fundingCurveName, fundingCurveName };
      default:
        throw new OpenGammaRuntimeException("Could not find " + type + " in security instrument list");
    }
  }
}
