/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MarginPriceVisitor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Provides the reference margin price,
 * for futures, options and other exchange traded securities that are margined
 * @deprecated Use the version of the function that does not refer to funding or forward curves
 * @see MarginPriceFunction
 */
@Deprecated
public class MarginPriceFunctionDeprecated extends InterestRateFutureOptionBlackFunctionDeprecated {

  private static MarginPriceVisitor s_priceVisitor = MarginPriceVisitor.getInstance();

  public MarginPriceFunctionDeprecated() {
    super(ValueRequirementNames.DAILY_PRICE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final Double price = irFutureOption.accept(s_priceVisitor, data);
    return Collections.singleton(new ComputedValue(spec, price));
  }

}
