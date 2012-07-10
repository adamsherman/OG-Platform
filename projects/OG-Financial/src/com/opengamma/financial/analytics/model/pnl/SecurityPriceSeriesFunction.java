/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class SecurityPriceSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private final String _resolutionKey;
  private final String _fieldName;

  public SecurityPriceSeriesFunction(final String resolutionKey, final String fieldName) {
    Validate.notNull(resolutionKey, "resolution key");
    Validate.notNull(fieldName, "field name");
    _resolutionKey = resolutionKey;
    _fieldName = fieldName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> samplingPeriodName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    final Set<String> scheduleCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> samplingFunctionName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    final Period samplingPeriod = getSamplingPeriod(samplingPeriodName);
    final LocalDate startDate = now.minus(samplingPeriod);
    final HistoricalTimeSeries hts = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final DoubleTimeSeries<?> ts = hts.getTimeSeries();
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price series for security " + security);
    }
    if (ts.isEmpty()) {
      throw new OpenGammaRuntimeException("Empty price series for security " + security);
    }
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    final DoubleTimeSeries<?> resultTS = samplingFunction.getSampledTimeSeries(ts, schedule);
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode()).get();
    final ValueRequirement vr = new ValueRequirement(ValueRequirementNames.PRICE_SERIES, security, resultProperties);
    final ValueSpecification valueSpecification = new ValueSpecification(vr, getUniqueId());
    final ComputedValue result = new ComputedValue(valueSpecification, resultTS);
    return Sets.newHashSet(result);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    try {
      return FinancialSecurityUtils.getCurrency(target.getSecurity()) != null;
    } catch (final UnsupportedOperationException e) {
      return false;
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> samplingPeriods = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if ((samplingPeriods == null) || (samplingPeriods.size() != 1)) {
      return null;
    }
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(target.getSecurity().getExternalIdBundle(), null, null, null, _fieldName, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries,
        _fieldName, DateConstraint.VALUATION_TIME.minus(samplingPeriods.iterator().next()), true, DateConstraint.VALUATION_TIME, true));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties();
    properties
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getSecurity(), properties.get()), getUniqueId()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
    if (samplingPeriodNames == null || samplingPeriodNames.isEmpty() || samplingPeriodNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique sampling period name: " + samplingPeriodNames);
    }
    return Period.parse(samplingPeriodNames.iterator().next());
  }

  private Schedule getScheduleCalculator(final Set<String> scheduleCalculatorNames) {
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.isEmpty() || scheduleCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique schedule calculator name: " + scheduleCalculatorNames);
    }
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorNames.iterator().next());
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final Set<String> samplingFunctionNames) {
    if (samplingFunctionNames == null || samplingFunctionNames.isEmpty() || samplingFunctionNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique sampling function name: " + samplingFunctionNames);
    }
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionNames.iterator().next());
  }
}
