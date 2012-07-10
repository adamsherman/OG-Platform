/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * The function calculating the Black volatility sensitivity to each point to which the option is sensitive..
 */
public class ForexDigitalOptionCallSpreadBlackVegaPointFunction extends ForexDigitalOptionCallSpreadBlackSingleValuedFunction {

  public ForexDigitalOptionCallSpreadBlackVegaPointFunction() {
    super(ValueRequirementNames.CALL_SPREAD_VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator calculator = new PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator(spread);
    final PresentValueForexBlackVolatilitySensitivity result = calculator.visit(fxDigital, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
