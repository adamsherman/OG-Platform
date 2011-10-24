/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.calculator.ZSpreadFromCurvesCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondZSpreadFromCurvesFunction extends BondFromCurvesFunction {
  private static final ZSpreadFromCurvesCalculator CALCULATOR = ZSpreadFromCurvesCalculator.getInstance();
  
  public BondZSpreadFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName);
  }
  
  public BondZSpreadFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName);
  }
  
  @Override
  protected Set<ComputedValue> calculate(final BondFixedSecurity bond, final YieldCurveBundle data, final ComputationTarget target) {
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), CALCULATOR.visit(bond, data)));
  }

  @Override
  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD).get();
    return new ValueSpecification(ValueRequirementNames.Z_SPREAD, target.toSpecification(), properties);
  }
}
