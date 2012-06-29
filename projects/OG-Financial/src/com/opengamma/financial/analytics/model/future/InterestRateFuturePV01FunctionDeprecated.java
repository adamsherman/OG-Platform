/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;

/**
 * @deprecated Use the version of the function that does not refer to funding and forward curves
 * @see InterestRateFuturePV01Function
 */
@Deprecated
public class InterestRateFuturePV01FunctionDeprecated extends InterestRateFutureCurveSpecificFunctionDeprecated {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public InterestRateFuturePV01FunctionDeprecated() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFuture, final String curveName, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle curves, final ValueSpecification resultSpec) {
    final Map<String, Double> pv01 = CALCULATOR.visit(irFuture, curves);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, pv01.get(curveName)));
  }

}
