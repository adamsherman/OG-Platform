/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is a vector (DoubleMatrix1D) with length equal to the total number of parameters in all the curves, 
 * and ordered as the parameters to the different curves themselves in increasing order. 
 */
public class ParameterSensitivityCalculator extends AbstractParameterSensitivityCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityCalculator(InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param sensitivityCurves The curves which respect to which the sensitivity is computed.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final InterestRateCurveSensitivity sensitivity, final YieldCurveBundle sensitivityCurves) {
    final List<Double> result = new ArrayList<Double>();
    for (final String name : sensitivityCurves.getAllNames()) { // loop over all curves (by name)
      final YieldAndDiscountCurve curve = sensitivityCurves.getCurve(name);
      List<Double> oneCurveSensitivity = pointToParameterSensitivity(sensitivity.getSensitivities().get(name), curve);
      result.addAll(oneCurveSensitivity);
    }
    return new DoubleMatrix1D(result.toArray(new Double[0]));
  }

}
