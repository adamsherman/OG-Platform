/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PresentValueNodeSensitivityCalculatorTest extends NodeSensitivityCalculatorTest {

  private static PresentValueCalculator VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  private static PresentValueCurveSensitivityCalculator SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  private static PresentValueNodeSensitivityCalculator CALCULATOR = PresentValueNodeSensitivityCalculator.getDefaultInstance();

  @Override
  protected NodeYieldSensitivityCalculator getCalculator() {
    return CALCULATOR;
  }

  @Override
  protected PresentValueCurveSensitivityCalculator getSensitivityCalculator() {
    return SENSITIVITY_CALCULATOR;
  }

  @Override
  protected PresentValueCalculator getValueCalculator() {
    return VALUE_CALCULATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new PresentValueNodeSensitivityCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    PresentValueNodeSensitivityCalculator.using(null);
  }

  @Test
  public void testObject() {
    PresentValueNodeSensitivityCalculator other = new PresentValueNodeSensitivityCalculator();
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new PresentValueNodeSensitivityCalculator(PresentValueCurveSensitivitySABRCalculator.getInstance());
    assertFalse(other.equals(CALCULATOR));
  }

  @Test
  public void testPresentValue() {
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueCurveSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = CALCULATOR.calculateSensitivities(SWAP, senseCalculator, null, CURVE_BUNDLE_YIELD);
    final DoubleMatrix1D fdresult = finiteDiffNodeSensitivitiesYield(SWAP, valueCalculator, null, CURVE_BUNDLE_YIELD);
    assertArrayEquals(result.getData(), fdresult.getData(), TOLERANCE_SENSI);
  }

  //private static final ForexSwapDefinition FX_SWAP = ForexInstrumentsDescriptionDataSet.createForexSwapDefinition(); // EUR/USD - Near date: 2011-May26

  @Test
  /**
   * Tests the present value node sensitivity for a swaption.
   */
  public void presentValueSwaption() {
    final String[] curveNames = TestsDataSetsSABR.curves2Names();
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2009, 3, 28);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition(); // USD - Expiry date: 2011-Mar-28
    final SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(referenceDate, curveNames);
    final PresentValueSABRCalculator pvc = PresentValueSABRCalculator.getInstance();
    final PresentValueCurveSensitivitySABRCalculator pvcsc = PresentValueCurveSensitivitySABRCalculator.getInstance();
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves2();
    final SABRInterestRateParameters sabrParam = TestsDataSetsSABR.createSABR2();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParam, curves);
    final DoubleMatrix1D resultCalculator = CALCULATOR.calculateSensitivities(swaption, pvcsc, null, sabrBundle);
    final DoubleMatrix1D resultFiniteDifference = finiteDiffNodeSensitivitiesYield(swaption, pvc, null, sabrBundle);
    double notional = Math.abs(swaption.getUnderlyingSwap().getFirstLeg().getNthPayment(0).getNotional());
    assertArrayEquals("Present Value Node Sensitivity", resultFiniteDifference.getData(), resultCalculator.getData(), notional * TOLERANCE_SENSI);

    final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
    final InterestRateCurveSensitivity pvcsMethod = method.presentValueCurveSensitivity(swaption, sabrBundle);
    final DoubleMatrix1D resultMethod = CALCULATOR.curveToNodeSensitivities(pvcsMethod, sabrBundle);
    final DoubleMatrix1D resultMethod2 = CALCULATOR.curveToNodeSensitivities(pvcsMethod, curves);
    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod.getData(), notional * TOLERANCE_SENSI);
    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod2.getData(), notional * TOLERANCE_SENSI);
  }

}
