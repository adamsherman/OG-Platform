/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaSensitivityCalculator;
import com.opengamma.financial.forex.method.PresentValueVolatilityNodeSensitivityDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexSingleBarrierOptionVegaFunction extends ForexSingleBarrierOptionFunction {
  private static final PresentValueForexVegaSensitivityCalculator CALCULATOR = PresentValueForexVegaSensitivityCalculator.getInstance();
  private static final DecimalFormat DELTA_FORMATTER = new DecimalFormat("##");

  public ForexSingleBarrierOptionVegaFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.VEGA_MATRIX);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexDerivative fxBarrierOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final PresentValueVolatilityNodeSensitivityDataBundle result = CALCULATOR.visit(fxBarrierOption, data);
    final double[] expiries = result.getExpiries().getData();
    final double[] delta = result.getDelta().getData();
    final double[][] vega = result.getVega().getData();
    final int nDelta = delta.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[nDelta];
    final String[] columnLabels = new String[nDelta];
    final double[][] values = new double[nDelta][nExpiries];
    for (int i = 0; i < nDelta; i++) {
      columnValues[i] = delta[i];
      columnLabels[i] = "P" + DELTA_FORMATTER.format(delta[i] * 100) + " " + result.getCurrencyPair().getFirst() + "/" + result.getCurrencyPair().getSecond();
      for (int j = 0; j < nExpiries; j++) {
        if (i == 0) {
          rowValues[j] = expiries[j];
          rowLabels[j] = getFormattedExpiry(expiries[j]);
        }
        values[i][j] = vega[j][i];
      }
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutCurveName()).with(ValuePropertyNames.RECEIVE_CURVE, getCallCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values)));
  }

  private String getFormattedExpiry(final double expiry) {
    if (expiry < 1. / 54) {
      final int days = (int) Math.ceil((365 * expiry));
      return days + "D";
    }
    if (expiry < 1. / 13) {
      final int weeks = (int) Math.ceil((52 * expiry));
      return weeks + "W";
    }
    if (expiry < 0.95) {
      final int months = (int) Math.ceil((12 * expiry));
      return months + "M";
    }
    return ((int) Math.ceil(expiry)) + "Y";
  }
}
