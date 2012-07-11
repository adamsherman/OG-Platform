/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.curve.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

public abstract class ParameterSensitivityCalculatorTest {

  protected static final String DISCOUNTING_CURVE_NAME = "USD Discounting";
  protected static final String FORWARD_CURVE_NAME = "USD Forward 3M";
  protected static final String[] CURVE_NAMES = new String[] {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  protected static final YieldCurveBundle CURVE_BUNDLE_YIELD;
  protected static final YieldAndDiscountCurve DISCOUNTING_CURVE_YIELD;
  protected static final YieldAndDiscountCurve FORWARD_CURVE_YIELD;

  protected static final YieldCurveBundle CURVE_BUNDLE_SPREAD;
  protected static final YieldAndDiscountCurve DISCOUNTING_CURVE_SPREAD;
  protected static final YieldAndDiscountCurve FORWARD_CURVE_SPREAD;

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapTestsMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR6M = USD6MLIBOR3M.getIborIndex();
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final double SWAP_RATE = 0.05;
  private static final double SWAP_NOTIONAL = 1.0;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 6, 29);
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, USDLIBOR6M.getSpotLag(), NYC);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLE_DATE, SWAP_TENOR, USD6MLIBOR3M, SWAP_NOTIONAL, SWAP_RATE, true);
  protected static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_DQ = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR,
      FLAT_EXTRAPOLATOR);
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR_CS = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, LINEAR_EXTRAPOLATOR,
      FLAT_EXTRAPOLATOR);

  protected static final double TOLERANCE_SENSI = 1.0E-6;

  static {
    final double[] dscCurveNodes = new double[] {0.01, 0.5, 1, 1.5, 2.0, 3.1, 4.1, 5, 6.0};
    final double[] fwdCurveNodes = new double[] {0.01, 1, 1.5, 1.9, 3., 4.0, 5.0, 6.0};

    final double[] dscCurveYields = new double[] {0.03, 0.03, 0.04, 0.043, 0.06, 0.03, 0.036, 0.03, 0.03};
    final double[] fwdCurveYields = new double[] {0.03, 0.05, 0.043, 0.048, 0.031, 0.0362, 0.032, 0.032};

    DISCOUNTING_CURVE_YIELD = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(dscCurveNodes, dscCurveYields, INTERPOLATOR_DQ));
    FORWARD_CURVE_YIELD = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(fwdCurveNodes, fwdCurveYields, INTERPOLATOR_CS));
    LinkedHashMap<String, YieldAndDiscountCurve> curvesY = new LinkedHashMap<String, YieldAndDiscountCurve>();
    curvesY.put(DISCOUNTING_CURVE_NAME, DISCOUNTING_CURVE_YIELD);
    curvesY.put(FORWARD_CURVE_NAME, FORWARD_CURVE_YIELD);
    CURVE_BUNDLE_YIELD = new YieldCurveBundle(curvesY);

    double spread = 0.01;
    YieldAndDiscountCurve spreadCurve = YieldCurve.from(new ConstantDoublesCurve(spread));
    DISCOUNTING_CURVE_SPREAD = new YieldAndDiscountAddZeroSpreadCurve("Dsc+Spread", false, DISCOUNTING_CURVE_YIELD, spreadCurve);
    FORWARD_CURVE_SPREAD = new YieldAndDiscountAddZeroSpreadCurve("Fwd+Spread", false, FORWARD_CURVE_YIELD, spreadCurve);
    LinkedHashMap<String, YieldAndDiscountCurve> curvesDF = new LinkedHashMap<String, YieldAndDiscountCurve>();
    curvesDF.put(DISCOUNTING_CURVE_NAME, DISCOUNTING_CURVE_SPREAD);
    curvesDF.put(FORWARD_CURVE_NAME, FORWARD_CURVE_SPREAD);
    CURVE_BUNDLE_SPREAD = new YieldCurveBundle(curvesDF);
  }

  protected abstract ParameterSensitivityCalculator getCalculator();

  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Double> getValueCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument() {
    getCalculator().calculateSensitivity(null, null, CURVE_BUNDLE_YIELD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolatedCurves() {
    getCalculator().calculateSensitivity(SWAP, null, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNames() {
    getCalculator().calculateSensitivity(SWAP, new YieldCurveBundle(CURVE_BUNDLE_YIELD), CURVE_BUNDLE_YIELD);
  }

  @Test
  public void testWithKnownYieldCurve() {
    final YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(DISCOUNTING_CURVE_NAME, DISCOUNTING_CURVE_YIELD);
    final LinkedHashMap<String, YieldAndDiscountCurve> fittingCurveMap = new LinkedHashMap<String, YieldAndDiscountCurve>();
    fittingCurveMap.put(FORWARD_CURVE_NAME, FORWARD_CURVE_YIELD);
    YieldCurveBundle fittingCurve = new YieldCurveBundle(fittingCurveMap);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = getValueCalculator();
    final DoubleMatrix1D result = getCalculator().calculateSensitivity(SWAP, fixedCurve, fittingCurve);
    final DoubleMatrix1D fdResult = finiteDiffNodeSensitivitiesYield(SWAP, valueCalculator, fixedCurve, fittingCurve);
    assertArrayEquals("Sensitivity to rates: YieldCurve", fdResult.getData(), result.getData(), TOLERANCE_SENSI);
  }

  @Test
  public void testWithKnownSpreadCurve() {
    final YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(DISCOUNTING_CURVE_NAME, DISCOUNTING_CURVE_SPREAD);
    final LinkedHashMap<String, YieldAndDiscountCurve> fittingCurveMap = new LinkedHashMap<String, YieldAndDiscountCurve>();
    fittingCurveMap.put(FORWARD_CURVE_NAME, FORWARD_CURVE_SPREAD);
    YieldCurveBundle fittingCurve = new YieldCurveBundle(fittingCurveMap);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = getValueCalculator();
    final DoubleMatrix1D result = getCalculator().calculateSensitivity(SWAP, fixedCurve, fittingCurve);
    final DoubleMatrix1D fdResult = finiteDiffNodeSensitivitiesSpread(SWAP, valueCalculator, fixedCurve, fittingCurve);
    assertArrayEquals("Sensitivity to rates: YieldCurve", fdResult.getData(), result.getData(), TOLERANCE_SENSI);
  }

  protected DoubleMatrix1D finiteDiffNodeSensitivitiesYield(final InstrumentDerivative ird, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {

    int nNodes = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) ((YieldCurve) interpolatedCurves.getCurve(curveName)).getCurve()).getDataBundle();
      nNodes += dataBundle.size();
    }

    final double[] yields = new double[nNodes];
    int index = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) ((YieldCurve) interpolatedCurves.getCurve(curveName)).getCurve()).getDataBundle();
      for (final double y : dataBundle.getValues()) {
        yields[index++] = y;
      }
    }

    final Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(final DoubleMatrix1D x) {
        final YieldCurveBundle curves = interpolatedCurves.copy();
        int index2 = 0;
        for (final String name : interpolatedCurves.getAllNames()) {
          final YieldCurve curve = (YieldCurve) interpolatedCurves.getCurve(name);
          final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
          final int numberOfNodes = dataBundle.size();
          final double[] yields1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          index2 += numberOfNodes;
          final YieldAndDiscountCurve newCurve = YieldCurve.from(InterpolatedDoublesCurve.from(dataBundle.getKeys(), yields1, ((InterpolatedDoublesCurve) curve.getCurve()).getInterpolator()));
          curves.replaceCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return valueCalculator.visit(ird, curves);
      }
    };

    final ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.differentiate(f);

    return grad.evaluate(new DoubleMatrix1D(yields));

  }

  protected DoubleMatrix1D finiteDiffNodeSensitivitiesSpread(final InstrumentDerivative ird, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle spreadCurves) {
    int nNodes = 0;
    for (final String curveName : spreadCurves.getAllNames()) {
      YieldCurve yieldCurve = (YieldCurve) ((YieldAndDiscountAddZeroSpreadCurve) spreadCurves.getCurve(curveName)).getCurves()[0];
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) yieldCurve.getCurve()).getDataBundle();
      nNodes += dataBundle.size() + 1; // +1 for spread
    }
    final double[] param = new double[nNodes];
    int index = 0;
    for (final String curveName : spreadCurves.getAllNames()) {
      YieldCurve yieldCurve = (YieldCurve) ((YieldAndDiscountAddZeroSpreadCurve) spreadCurves.getCurve(curveName)).getCurves()[0];
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) yieldCurve.getCurve()).getDataBundle();
      for (final double y : dataBundle.getValues()) {
        param[index++] = y;
      }
      YieldCurve spreadCurve = (YieldCurve) ((YieldAndDiscountAddZeroSpreadCurve) spreadCurves.getCurve(curveName)).getCurves()[1];
      param[index++] = ((ConstantDoublesCurve) (spreadCurve.getCurve())).getYData()[0];
    }

    final Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(final DoubleMatrix1D x) {
        final YieldCurveBundle curves = spreadCurves.copy();
        int index2 = 0;
        for (final String name : spreadCurves.getAllNames()) {
          YieldCurve yieldCurve = (YieldCurve) ((YieldAndDiscountAddZeroSpreadCurve) spreadCurves.getCurve(name)).getCurves()[0];
          final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) yieldCurve.getCurve()).getDataBundle();
          final int numberOfNodes = dataBundle.size();
          final double[] yields1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          final double spread1 = x.getData()[index2 + numberOfNodes];
          index2 += numberOfNodes + 1;
          final YieldCurve newYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.from(dataBundle.getKeys(), yields1, ((InterpolatedDoublesCurve) yieldCurve.getCurve()).getInterpolator()));
          final YieldCurve newSpreadCurve = YieldCurve.from(new ConstantDoublesCurve(spread1));
          final YieldAndDiscountCurve newCurve = new YieldAndDiscountAddZeroSpreadCurve("NewYield+Spread", false, newYieldCurve, newSpreadCurve);
          curves.replaceCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return valueCalculator.visit(ird, curves);
      }
    };

    final ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.differentiate(f);
    return grad.evaluate(new DoubleMatrix1D(param));
  }
}
