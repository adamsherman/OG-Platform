/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateInstrumentTradeOrSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunctionHelper;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterpolatedYieldCurveFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedYieldCurveFunction.class);
  private static final LastTimeCalculator LAST_DATE_CALCULATOR = LastTimeCalculator.getInstance();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(compilationContext);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(compilationContext);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(compilationContext);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(compilationContext);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(compilationContext);
    final InterestRateInstrumentTradeOrSecurityConverter securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true);
    final FixedIncomeConverterDataProvider definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final HistoricalTimeSeriesBundle timeSeries = (HistoricalTimeSeriesBundle) inputs.getValue(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
        final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
        final ValueProperties inputProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();
        final Object specificationObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, target.toSpecification(), inputProperties));
        if (specificationObject == null) {
          throw new OpenGammaRuntimeException("Could not get interpolated yield curve specification");
        }
        final Object dataObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, target.toSpecification(), inputProperties));
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get yield curve data");
        }
        final InterpolatedYieldCurveSpecificationWithSecurities specification = (InterpolatedYieldCurveSpecificationWithSecurities) specificationObject;
        final SnapshotDataBundle data = (SnapshotDataBundle) dataObject;
        final Map<ExternalId, Double> marketData = YieldCurveFunctionHelper.buildMarketDataMap(data);
        final int n = marketData.size();
        final double[] times = new double[n];
        final double[] yields = new double[n];
        int i = 0;
        for (final FixedIncomeStripWithSecurity strip : specification.getStrips()) {
          final Double marketValue = marketData.get(strip.getSecurityIdentifier());
          if (marketValue == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + strip);
          }
          final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
          final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), curveName, curveName);
          final InstrumentDefinition<?> definition = securityConverter.visit(financialSecurity);
          final InstrumentDerivative derivative = definitionConverter.convert(financialSecurity, definition, now, curveNames, timeSeries);
          if (derivative == null) {
            throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
          }
          times[i] = LAST_DATE_CALCULATOR.visit(derivative);
          yields[i++] = marketValue;
        }
        final String interpolatorName = Interpolator1DFactory.getInterpolatorName(specification.getInterpolator());
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(times, yields, interpolator);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
            .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME).get();
        final ValueSpecification result = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(result, YieldCurve.from(curve)));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        if (target.getUniqueId() == null) {
          s_logger.error("Target unique id was null; {}", target);
          return false;
        }
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
            .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME)
            .get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          s_logger.error("Could not get curve name from constraints {}", constraints);
          return null;
        }
        final Set<String> leftInterpolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
        if (leftInterpolatorNames == null || leftInterpolatorNames.size() != 1) {
          return null;
        }
        final Set<String> rightInterpolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
        if (rightInterpolatorNames == null || rightInterpolatorNames.size() != 1) {
          return null;
        }
        final String curveName = curveNames.iterator().next();
        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(2);
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName)
            .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveName).get()));
        return requirements;
      }

    };
  }

}
