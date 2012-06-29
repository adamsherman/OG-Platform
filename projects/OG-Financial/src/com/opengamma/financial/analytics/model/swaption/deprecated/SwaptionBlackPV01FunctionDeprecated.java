/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.deprecated;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * @deprecated Use the version of this function that does not refer to funding and forward curves
 * @see SwaptionBlackPV01Function
 */
@Deprecated
public class SwaptionBlackPV01FunctionDeprecated extends SwaptionBlackCurveSpecificFunctionDeprecated {
  private static final PV01Calculator CALCULATOR = new PV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  public SwaptionBlackPV01FunctionDeprecated() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final String curveName, final ValueSpecification spec) {
    final Map<String, Double> pv01 = CALCULATOR.visit(swaption, data);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + curveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(curveName)));
  }
}
