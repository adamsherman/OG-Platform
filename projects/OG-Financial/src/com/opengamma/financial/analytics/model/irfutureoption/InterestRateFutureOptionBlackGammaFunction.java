/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackGammaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function computes the gamma, second order derivative of position price with respect to the futures rate,
 * for InterestRateFutureOptions in the Black world.
 */
public class InterestRateFutureOptionBlackGammaFunction extends InterestRateFutureOptionBlackCurveSpecificFunction {

  /**
   * The calculator to compute the gamma value.
   */
  private static final PresentValueBlackGammaCalculator CALCULATOR = PresentValueBlackGammaCalculator.getInstance();

  public InterestRateFutureOptionBlackGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final String curveName, final ValueSpecification spec) {

    final Double gamma = CALCULATOR.visit(irFutureOption, data);
    return Collections.singleton(new ComputedValue(spec, gamma));
  }

}
