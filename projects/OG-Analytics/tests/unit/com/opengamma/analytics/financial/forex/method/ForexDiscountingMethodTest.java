/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.ForwardRateForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.horizon.ConstantSpreadYieldCurveBundleRolldownFunction;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the method for Forex transaction by discounting on each payment.
 */
public class ForexDiscountingMethodTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 5, 24);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final Map<String, Currency> CURVE_CURRENCY = TestsDataSetsForex.curveCurrency();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final PaymentFixedDefinition PAY_DEFINITION_1 = new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1);
  private static final PaymentFixed PAY_1 = PAY_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_NAME[0]);
  private static final PaymentFixedDefinition PAY_DEFINITION_2 = new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE);
  private static final PaymentFixed PAY_2 = PAY_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_NAME[1]);

  private static final ForexDiscountingMethod METHOD = ForexDiscountingMethod.getInstance();
  private static final com.opengamma.analytics.financial.interestrate.PresentValueCalculator PVC_IR = com.opengamma.analytics.financial.interestrate.PresentValueCalculator.getInstance();
  private static final com.opengamma.analytics.financial.forex.calculator.PresentValueMCACalculator PVC_FX = com.opengamma.analytics.financial.forex.calculator.PresentValueMCACalculator
      .getInstance();
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final CurrencyExposureForexCalculator CEC_FX = CurrencyExposureForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityMCSCalculator PVCSC_FX = PresentValueCurveSensitivityMCSCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();
  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    final CurrencyAmount ca1 = CurrencyAmount.of(CUR_1, PVC_IR.visit(PAY_1, CURVES));
    final CurrencyAmount ca2 = CurrencyAmount.of(CUR_2, PVC_IR.visit(PAY_2, CURVES));
    assertEquals(ca1, pv.getCurrencyAmount(CUR_1));
    assertEquals(ca2, pv.getCurrencyAmount(CUR_2));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(FX, CURVES);
    final MultipleCurrencyAmount pvCalculator = PVC_FX.visit(FX, CURVES);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcs = METHOD.presentValueCurveSensitivity(FX, CURVES);
    final Map<String, List<DoublesPair>> pvs1 = PVSC.visit(PAY_1, CURVES);
    final Map<String, List<DoublesPair>> pvs2 = PVSC.visit(PAY_2, CURVES);
    assertEquals(pvs1.get(CURVES_NAME[0]), pvcs.getSensitivity(CUR_1).getSensitivities().get(CURVES_NAME[0]));
    assertEquals(pvs2.get(CURVES_NAME[1]), pvcs.getSensitivity(CUR_2).getSensitivities().get(CURVES_NAME[1]));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(FX, CURVES);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = PVCSC_FX.visit(FX, CURVES);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Test the present value of EUR/USD is the same as an USD/EUR.
   */
  public void presentValueReverse() {
    final ForexDefinition fxReverseDefinition = new ForexDefinition(CUR_2, CUR_1, PAYMENT_DATE, -NOMINAL_1 * FX_RATE, 1.0 / FX_RATE);
    final Forex fxReverse = fxReverseDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0]});
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    final MultipleCurrencyAmount pvReverse = METHOD.presentValue(fxReverse, CURVES);
    assertEquals("Forex present value: Reverse description", pv.getAmount(CUR_1), pvReverse.getAmount(CUR_1), TOLERANCE_PV);
    assertEquals("Forex present value: Reverse description", pv.getAmount(CUR_2), pvReverse.getAmount(CUR_2), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure computation.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount exposureMethod = METHOD.currencyExposure(FX, CURVES);
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    assertEquals("Currency exposure", pv, exposureMethod);
    final MultipleCurrencyAmount exposureCalculator = CEC_FX.visit(FX, CURVES);
    assertEquals("Currency exposure: Method vs Calculator", exposureMethod, exposureCalculator);
  }

  @Test
  /**
   * Tests the forward Forex rate computation.
   */
  public void forwardRate() {
    final double fxToday = 1.4123;
    final FXMatrix fxMatrix = new FXMatrix(CUR_1, CUR_2, fxToday);
    final YieldCurveBundle curvesFx = new YieldCurveBundle(CURVES.getCurvesMap(), fxMatrix, CURVE_CURRENCY);
    final double fwd = METHOD.forwardForexRate(FX, curvesFx);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(FX.getPaymentTime());
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(FX.getPaymentTime());
    final double fwdExpected = fxToday * dfForeign / dfDomestic;
    assertEquals("Forex: forward rate", fwdExpected, fwd, 1.0E-10);
  }

  @Test
  /**
   * Tests the forward Forex rate through the method and through the calculator.
   */
  public void forwardRateMethodVsCalculator() {
    final double fxToday = 1.4123;
    final FXMatrix fxMatrix = new FXMatrix(CUR_1, CUR_2, fxToday);
    final YieldCurveBundle curvesFx = new YieldCurveBundle(CURVES.getCurvesMap(), fxMatrix, CURVE_CURRENCY);
    final double fwdMethod = METHOD.forwardForexRate(FX, curvesFx);
    final ForwardRateForexCalculator FWDC = ForwardRateForexCalculator.getInstance();
    final double fwdCalculator = FWDC.visit(FX, curvesFx);
    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, 1.0E-10);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentBeforePayment() {
    final Forex fx = FX_DEFINITION.toDerivative(PAYMENT_DATE.minusDays(1), CURVES_NAME);
    final MultipleCurrencyAmount cash = TPC.visit(fx);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(fx.getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(fx.getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentOnPayment() {
    final Forex fx = FX_DEFINITION.toDerivative(PAYMENT_DATE, CURVES_NAME);
    final MultipleCurrencyAmount cash = TPC.visit(fx);
    assertEquals("TodayPaymentCalculator: forex", FX_DEFINITION.getPaymentCurrency1().getAmount(), cash.getAmount(fx.getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", FX_DEFINITION.getPaymentCurrency2().getAmount(), cash.getAmount(fx.getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the Theta (1 day change of pv) for forex transactions.
   */
  public void thetaBeforePayment() {
    final MultipleCurrencyAmount theta = THETAC.getTheta(FX_DEFINITION, REFERENCE_DATE, CURVES_NAME, CURVES, 1);
    final Forex swapToday = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final Forex swapTomorrow = FX_DEFINITION.toDerivative(REFERENCE_DATE.plusDays(1), CURVES_NAME);
    final MultipleCurrencyAmount pvToday = PVC_FX.visit(swapToday, CURVES);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(REFERENCE_DATE, REFERENCE_DATE.plusDays(1)));
    final MultipleCurrencyAmount pvTomorrow = PVC_FX.visit(swapTomorrow, tomorrowData);
    final MultipleCurrencyAmount thetaExpected = pvTomorrow.plus(pvToday.multipliedBy(-1.0));
    assertEquals("ThetaCalculator: fixed-coupon swap", thetaExpected.getAmount(CUR_1), theta.getAmount(CUR_1), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", thetaExpected.getAmount(CUR_2), theta.getAmount(CUR_2), TOLERANCE_PV);
    assertEquals("ThetaCalculator: fixed-coupon swap", 2, theta.getCurrencyAmounts().length);
  }
}
