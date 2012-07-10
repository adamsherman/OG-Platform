/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of deposits zero-coupon by discounting.
 */
public class DepositZeroDiscountingMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE_FIGURE = 0.0250;
  private static final InterestRate RATE = new PeriodicInterestRate(RATE_FIGURE, 1);
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final double DEPOSIT_AF = GENERATOR.getDayCount().getDayCountFraction(SPOT_DATE, END_DATE);
  private static final DepositZeroDefinition DEPOSIT_DEFINITION = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves2Names();

  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT = DepositZeroDiscountingMethod.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PRCSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double TOLERANCE_TIME = 1.0E-6;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-10;

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvMethod.getAmount(), TOLERANCE_PRICE);
    double pvCalculator = PVC.visit(deposit, CURVES);
    assertEquals("DepositDefinition: present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvMethod.getAmount(), TOLERANCE_PRICE);
    double pvCalculator = PVC.visit(deposit, CURVES);
    assertEquals("DepositDefinition: present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pvcsMethod = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsMethod.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 2, pvcsMethod.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    DepositZero depositBunped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getStartTime(), deposit.getEndTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBunped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsMethod.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC.visit(deposit, CURVES));
    assertTrue("DepositZero: present value curve sensitivity", InterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_RATE));
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pvcsMethod = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    pvcsMethod = pvcsMethod.cleaned(0.0, 1.0E-4);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsMethod.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsMethod.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    DepositZero depositBumped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getEndTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBumped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsMethod.getSensitivities().get(CURVES_NAME[0]);
    final DoublesPair pairPv = sensiPvDisc.get(0);
    assertEquals("Sensitivity coupon pv to forward curve: Node " + 0, nodeTimesDisc[0], pairPv.getFirst(), 1E-8);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[0], deltaTolerancePrice);
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC.visit(deposit, CURVES));
    pvcsCalculator = pvcsCalculator.cleaned(0.0, 1.0E-4);
    assertTrue("DepositZero: present value curve sensitivity", InterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_RATE));
  }

  @Test
  /**
   * Tests the par rate when the valuation date is on trade date.
   */
  public void parRateTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    double prMethod = METHOD_DEPOSIT.parRate(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    final double rcc = Math.log(dfStart / dfEnd) / deposit.getPaymentAccrualFactor();
    double prExpected = deposit.getRate().fromContinuous(new ContinuousInterestRate(rcc)).getRate();
    assertEquals("DepositZero: par rate", prExpected, prMethod, TOLERANCE_RATE);
    double prCalculator = PRC.visit(deposit, CURVES);
    assertEquals("DepositZero: par rate", prMethod, prCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate when the valuation date is on trade date.
   */
  public void parRateSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    double prMethod = METHOD_DEPOSIT.parRate(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = 1.0;
    final double rcc = Math.log(dfStart / dfEnd) / deposit.getPaymentAccrualFactor();
    double prExpected = deposit.getRate().fromContinuous(new ContinuousInterestRate(rcc)).getRate();
    assertEquals("DepositZero: par rate", prExpected, prMethod, TOLERANCE_RATE);
    double prCalculator = PRC.visit(deposit, CURVES);
    assertEquals("DepositZero: par rate", prMethod, prCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate curve sensitivity when the valuation date is on trade date.
   */
  public void parRateCurveSensitivityTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity prcsMethod = METHOD_DEPOSIT.parRateCurveSensitivity(deposit, CURVES);
    final List<DoublesPair> sensiPvDisc = prcsMethod.getSensitivities().get(CURVES_NAME[0]);
    double pr = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final YieldAndDiscountCurve curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    double deltaShift = 0.0001;
    int nbNode = 2;
    double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    nodeTimesExtended[1] = deposit.getStartTime();
    nodeTimesExtended[2] = deposit.getEndTime();
    final double[] yields = new double[nbNode + 1];
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES.replaceCurve(CURVES_NAME[0], curveBumped);
      final double prBumped = METHOD_DEPOSIT.parRate(deposit, CURVES);
      result[loopnode] = (prBumped - pr) / deltaShift;
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, result[loopnode], TOLERANCE_PRICE);
    }
    CURVES.replaceCurve(CURVES_NAME[0], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = new InterestRateCurveSensitivity(PRCSC.visit(deposit, CURVES));
    prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
    assertTrue("DepositZero: par rate curve sensitivity", InterestRateCurveSensitivity.compare(prcsMethod, prcsCalculator, TOLERANCE_RATE));
  }

  @Test
  /**
   * Tests the par rate curve sensitivity when the valuation date is on trade date.
   */
  public void parRateCurveSensitivitySettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity prcsMethod = METHOD_DEPOSIT.parRateCurveSensitivity(deposit, CURVES);
    final List<DoublesPair> sensiPvDisc = prcsMethod.getSensitivities().get(CURVES_NAME[0]);
    double pr = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final YieldAndDiscountCurve curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    double deltaShift = 0.0001;
    int nbNode = 2;
    double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode];
    nodeTimesExtended[0] = deposit.getStartTime();
    nodeTimesExtended[1] = deposit.getEndTime();
    final double[] yields = new double[nbNode];
    yields[0] = curveToBump.getInterestRate(nodeTimesExtended[0]);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode], deltaShift);
      CURVES.replaceCurve(CURVES_NAME[0], curveBumped);
      final double prBumped = METHOD_DEPOSIT.parRate(deposit, CURVES);
      result[loopnode] = (prBumped - pr) / deltaShift;
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesExtended[loopnode], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, result[loopnode], TOLERANCE_PRICE);
    }
    CURVES.replaceCurve(CURVES_NAME[0], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = new InterestRateCurveSensitivity(PRCSC.visit(deposit, CURVES));
    assertTrue("DepositZero: par rate curve sensitivity", InterestRateCurveSensitivity.compare(prcsMethod, prcsCalculator, TOLERANCE_RATE));
  }

  @Test
  /**
   * Tests the par spread when the valuation date is on trade date.
   */
  public void parSpreadTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    double psMethod = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    DepositZeroDefinition deposit0Definition = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, new PeriodicInterestRate(RATE_FIGURE + psMethod, 1));
    DepositZero deposit0 = deposit0Definition.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, CURVES);
    assertEquals("DepositZero: par spread", 0, pv0.getAmount(), TOLERANCE_PRICE);
    double psCalculator = PSC.visit(deposit, CURVES);
    assertEquals("DepositZero: par rate", psMethod, psCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    ZonedDateTime referenceDate = TRADE_DATE;
    DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, CURVES);
    final List<DoublesPair> sensiPvDisc = pscsMethod.getSensitivities().get(CURVES_NAME[0]);
    double ps = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    final YieldAndDiscountCurve curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    double deltaShift = 0.0001;
    int nbNode = 2;
    double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    nodeTimesExtended[1] = deposit.getStartTime();
    nodeTimesExtended[2] = deposit.getEndTime();
    final double[] yields = new double[nbNode + 1];
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES.replaceCurve(CURVES_NAME[0], curveBumped);
      final double psBumped = METHOD_DEPOSIT.parSpread(deposit, CURVES);
      result[loopnode] = (psBumped - ps) / deltaShift;
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity par spread to curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity par spread to curve: Node", pairPv.second, result[loopnode], TOLERANCE_PRICE);
    }
    CURVES.replaceCurve(CURVES_NAME[0], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = PSCSC.visit(deposit, CURVES);
    prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
    assertTrue("DepositZero: par rate curve sensitivity", InterestRateCurveSensitivity.compare(pscsMethod, prcsCalculator, TOLERANCE_SPREAD_DELTA));
  }

}
