/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class SABRCMSSpreadRightExtrapolationPresentValueFunction extends SABRCMSSpreadRightExtrapolationFunction {

  @Override
  protected String getValueRequirement() {
    return ValueRequirementNames.PRESENT_VALUE;
  }

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    final Double cutoff = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE));
    final Double mu = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER));
    final PresentValueCalculator calculator = new PresentValueSABRExtrapolationCalculator(cutoff, mu);
    return calculator.visit(derivative, data);
  }
}
