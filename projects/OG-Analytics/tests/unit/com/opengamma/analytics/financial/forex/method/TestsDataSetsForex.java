/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;

/**
 * Sets of market data used in Forex tests.
 */
public class TestsDataSetsForex {

  private static final String DISCOUNTING_EUR = "Discounting EUR";
  private static final String DISCOUNTING_USD = "Discounting USD";
  private static final String DISCOUNTING_GBP = "Discounting GBP";
  private static final String DISCOUNTING_KRW = "Discounting KRW";

  /**
   * Create a yield curve bundle with three curves. One called "Discounting EUR" with a constant rate of 2.50%, one called "Discounting USD" with a constant rate of 1.00%
   * and one called "Discounting GBP" with a constant rate of 2.00%; "Discounting KRW" with a constant rate of 3.21%; 
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurvesForex() {
    final YieldAndDiscountCurve CURVE_EUR = YieldCurve.from(ConstantDoublesCurve.from(0.0250));
    final YieldAndDiscountCurve CURVE_USD = YieldCurve.from(ConstantDoublesCurve.from(0.0100));
    final YieldAndDiscountCurve CURVE_GBP = YieldCurve.from(ConstantDoublesCurve.from(0.0200));
    final YieldAndDiscountCurve CURVE_KRW = YieldCurve.from(ConstantDoublesCurve.from(0.0321));
    YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DISCOUNTING_EUR, CURVE_EUR);
    curves.setCurve(DISCOUNTING_USD, CURVE_USD);
    curves.setCurve(DISCOUNTING_GBP, CURVE_GBP);
    curves.setCurve(DISCOUNTING_KRW, CURVE_KRW);
    return curves;
  }

  public static String[] curveNames() {
    return new String[] {DISCOUNTING_EUR, DISCOUNTING_USD, DISCOUNTING_GBP, DISCOUNTING_KRW};
  }

  public static Map<String, Currency> curveCurrency() {
    Map<String, Currency> map = new HashMap<String, Currency>();
    map.put(DISCOUNTING_EUR, EUR);
    map.put(DISCOUNTING_USD, USD);
    map.put(DISCOUNTING_GBP, Currency.GBP);
    map.put(DISCOUNTING_KRW, Currency.of("KRW"));
    return map;
  }

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  //  private static final int SETTLEMENT_DAYS = 2;
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final Period[] EXPIRY_PERIOD = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5)};
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final double SPOT = 1.40;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR, USD, SPOT);
  private static final double[] ATM = {0.185, 0.18, 0.17, 0.16, 0.16};

  private static final double[] DELTA_2 = new double[] {0.10, 0.25};
  private static final double[][] RISK_REVERSAL_2 = new double[][] { {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE_2 = new double[][] { {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}, {0.0340, 0.0140}};

  private static final double[] DELTA_1 = new double[] {0.25};
  private static final double[][] RISK_REVERSAL_1 = new double[][] { {-0.0060}, {-0.0070}, {-0.0080}, {-0.0090}, {-0.0090}};
  private static final double[][] STRANGLE_1 = new double[][] { {0.0110}, {0.0120}, {0.0130}, {0.0140}, {0.0140}};

  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}};
  private static final double[][] STRANGLE_FLAT = new double[][] { {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}};

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_2, STRANGLE_2);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate, final Interpolator1D interpolator) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_2, STRANGLE_2, interpolator);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile3points(final ZonedDateTime referenceDate, final Interpolator1D interpolator) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_1, ATM, RISK_REVERSAL_1, STRANGLE_1, interpolator);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate, final double shift) {
    double[] atmShift = ATM.clone();
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      atmShift[loopexp] += shift;
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, atmShift, RISK_REVERSAL_2, STRANGLE_2);
  }

  public static SmileDeltaTermStructureParametersStrikeInterpolation smileFlat(final ZonedDateTime referenceDate) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    double[] timeToExpiry = new double[NB_EXP];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[loopexp], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[loopexp] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[loopexp]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_FLAT, STRANGLE_FLAT);
  }

  public static FXMatrix fxMatrix() {
    return FX_MATRIX;
  }

}
