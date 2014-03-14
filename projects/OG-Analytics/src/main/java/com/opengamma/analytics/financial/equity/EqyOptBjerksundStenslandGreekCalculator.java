/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the greeks of a commodity future option using the Barone-Adesi Whaley model {@link BaroneAdesiWhaleyModel}.
 * <p>
 * The greeks returned are delta, dual-delta, rho, carry rho, theta and vega.
 */
public final class EqyOptBjerksundStenslandGreekCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, GreekResultCollection> {
  /** A static instance of this calculator */
  private static final EqyOptBjerksundStenslandGreekCalculator INSTANCE = new EqyOptBjerksundStenslandGreekCalculator();
  /** The pricing model */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * @return A static instance of this class
   */
  public static EqyOptBjerksundStenslandGreekCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptBjerksundStenslandGreekCalculator() {
  }

  @Override
  public GreekResultCollection visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, b, t, volatility, isCall);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, greeks[4] / 100.);
    result.put(Greek.THETA, -greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    final double[] pdg = MODEL.getPriceDeltaGamma(s, k, r, b, t, volatility, isCall);
    result.put(Greek.GAMMA, pdg[2]);
    return result;
  }

  @Override
  public GreekResultCollection visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, b, t, volatility, isCall);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, greeks[4] / 100.);
    result.put(Greek.THETA, -greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    final double[] pdg = MODEL.getPriceDeltaGamma(s, k, r, b, t, volatility, isCall);
    result.put(Greek.GAMMA, pdg[2]);
    return result;
  }

  @Override
  public GreekResultCollection visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, b, t, volatility, isCall);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, greeks[4] / 100.);
    result.put(Greek.THETA, -greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    final double[] pdg = MODEL.getPriceDeltaGamma(s, k, r, b, t, volatility, isCall);
    result.put(Greek.GAMMA, pdg[2]);
    return result;
  }
}
