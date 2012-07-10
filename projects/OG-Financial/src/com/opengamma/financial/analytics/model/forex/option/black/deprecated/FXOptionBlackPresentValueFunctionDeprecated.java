/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXOptionBlackPresentValueFunction
 */
@Deprecated
public class FXOptionBlackPresentValueFunctionDeprecated extends FXOptionBlackSingleValuedFunctionDeprecated {
  private static final PresentValueBlackForexCalculator CALCULATOR = PresentValueBlackForexCalculator.getInstance();

  public FXOptionBlackPresentValueFunctionDeprecated() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxOption, data);
    ArgumentChecker.isTrue(result.size() == 1, "result size must be one; have {}", result.size());
    final CurrencyAmount ca = result.getCurrencyAmounts()[0];
    final double amount = ca.getAmount();
    return Collections.singleton(new ComputedValue(spec, amount));
  }

}
