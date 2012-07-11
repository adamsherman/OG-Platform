/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.volatility.VolatilityDataFittingDefaults;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRRightExtrapolationVegaFunctionDeprecated extends SABRVegaFunctionDeprecated {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cutoffs = constraints.getValues(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE);
    if (cutoffs == null || cutoffs.size() != 1) {
      return null;
    }
    final Set<String> mus = constraints.getValues(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER);
    if (mus == null || mus.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    return requirements;
  }

  @Override
  protected ValueProperties getSensitivityProperties(final ComputationTarget target, final String currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String cutoff = desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER);
    return ValueProperties.builder().with(ValuePropertyNames.CURRENCY, currency).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).with(ValuePropertyNames.CUBE, cubeName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE, cutoff).with(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER, mu)
        .with(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION).get();
  }

  @Override
  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency, final DayCount dayCount,
      final ValueRequirement desiredValue) {
    final YieldCurveBundle yieldCurves = getYieldCurves(inputs, currency, desiredValue);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    final ValueRequirement surfacesRequirement = getCubeRequirement(cubeName, currency, fittingMethod);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final SABRInterestRateParameters modelParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount);
    return new SABRInterestRateDataBundle(modelParameters, yieldCurves);
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency) {
    return properties.copy().with(ValuePropertyNames.CURRENCY, currency).withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CUBE).withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD).withAny(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE)
        .withAny(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER).withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.Y_INTERPOLATOR_NAME).withAny(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME).with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION)
        .withAny(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD)
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .with(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_CUBE).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
    final String fittingMethod = desiredValue.getConstraint(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String xInterpolator = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String xLeftExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String xRightExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String yInterpolator = desiredValue.getConstraint(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    final String yLeftExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME);
    final String yRightExtrapolator = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME);
    final String cutoff = desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE);
    final String mu = desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER);
    return properties.copy().with(ValuePropertyNames.CURRENCY, currency).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).with(ValuePropertyNames.CUBE, cubeName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE, cutoff).with(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER, mu)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, xInterpolator).with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, xLeftExtrapolator)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, xRightExtrapolator).with(InterpolatedDataProperties.Y_INTERPOLATOR_NAME, yInterpolator)
        .with(InterpolatedDataProperties.LEFT_Y_EXTRAPOLATOR_NAME, yLeftExtrapolator).with(InterpolatedDataProperties.RIGHT_Y_EXTRAPOLATOR_NAME, yRightExtrapolator)
        .with(ValuePropertyNames.CALCULATION_METHOD, SABR_RIGHT_EXTRAPOLATION).with(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD, fittingMethod)
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .with(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_CUBE).get();
  }
}
