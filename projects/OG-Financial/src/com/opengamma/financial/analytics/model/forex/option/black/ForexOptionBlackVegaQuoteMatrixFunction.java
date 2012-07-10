/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilityQuoteSensitivityForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityQuoteSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;

/**
 * 
 */
public class ForexOptionBlackVegaQuoteMatrixFunction extends ForexOptionBlackSingleValuedFunction {
  private static final PresentValueBlackVolatilityQuoteSensitivityForexCalculator CALCULATOR = PresentValueBlackVolatilityQuoteSensitivityForexCalculator.getInstance();

  public ForexOptionBlackVegaQuoteMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueForexBlackVolatilityQuoteSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    return Collections.singleton(new ComputedValue(spec, VegaMatrixHelper.getVegaFXQuoteMatrixInStandardForm(result)));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String callCurveName, final String putCurveConfig, final String callCurveConfig,
      final String surfaceName, final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName, final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(putCurveName, callCurveName, putCurveConfig, callCurveConfig,
        surfaceName, interpolatorName, leftExtrapolatorName, rightExtrapolatorName, target);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }
}
