/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureTradeConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunctionNew;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureYieldCurveNodeSensitivitiesFunction.class);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.getDefaultInstance();
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  private InterestRateFutureTradeConverter _converter;
  private FixedIncomeConverterDataProvider _dataConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _converter = new InterestRateFutureTradeConverter(new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource));
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Trade trade = target.getTrade();
    final FinancialSecurity security = (FinancialSecurity) trade.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final LocalDate localNow = now.toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final YieldCurveBundle fixedCurves = YieldCurveFunctionUtils.getFixedCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final InstrumentDefinition<?> definition = _converter.convert(trade);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for trade " + trade + " was null");
    }
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final InstrumentDerivative derivative = _dataConverter.convert(security, definition, now, curveNames, dataSource);
    final YieldCurveBundle bundle = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      final Object couponSensitivitiesObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
      if (couponSensitivitiesObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = new DoubleMatrix1D(FunctionUtils.decodeCouponSensitivities(couponSensitivitiesObject));
      sensitivities = CALCULATOR.calculateFromPresentValue(derivative, fixedCurves, curves, couponSensitivity, jacobian, NSC);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(derivative, fixedCurves, curves, jacobian, NSC);
    }
    if (curveCalculationMethod.equals(FXImpliedYieldCurveFunctionNew.FX_IMPLIED)) {
      final Currency domesticCurrency = Currency.of(curveCalculationConfig.getUniqueId().getUniqueId().getValue());
      final Currency foreignCurrency =
          Currency.of(curveCalculationConfigSource.getConfig(curveCalculationConfig.getExogenousConfigData().keySet().iterator().next()).getUniqueId().getUniqueId().getValue());
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(sensitivities, domesticCurrency, foreignCurrency, curveNames,
          curves, configSource, localNow, getResultSpec(target, currency, curveName, curveCalculationConfigName));
    }
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, bundle, sensitivities, curveSpec,
        getResultSpec(target, currency, curveName, curveCalculationConfigName));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof InterestRateFutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    return Collections.singleton(getResultSpec(target, ccy));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> curves = constraints.getValues(ValuePropertyNames.CURVE);
    if (curves == null || curves.size() != 1) {
      s_logger.error("Must specify a single curve name; have {}", curves);
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final UniqueIdentifiable uniqueId = curveCalculationConfig.getUniqueId();
    if (Currency.OBJECT_SCHEME.equals(uniqueId.getUniqueId().getScheme()) && !(uniqueId.getUniqueId().getValue().equals(currency.getCode()))) {
      return null;
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String curve = curves.iterator().next();
    if (Arrays.binarySearch(curveNames, curve) < 0) {
      s_logger.info("Curve named {} is not available in curve calculation configuration called {}", curve, curveCalculationConfigName);
      return null;
    }
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource));
    if (!curveCalculationMethod.equals(FXImpliedYieldCurveFunctionNew.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(currency, curve));
    }
    requirements.add(getJacobianRequirement(currency, curveCalculationConfigName, curveCalculationMethod));
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    String calculationConfig = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (input.getKey().getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        calculationConfig = input.getKey().getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      }
    }
    assert calculationConfig != null;
    return Collections.singleton(getResultSpec(target, ccy, calculationConfig));
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final Currency ccy) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.CURVE).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final Currency ccy, final String calculationConfig) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
        .withAny(ValuePropertyNames.CURVE).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getResultSpec(final ComputationTarget target, final Currency ccy, final String curveName, final String calculationConfig) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, calculationConfig)
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, currency, properties);
  }

  private ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, currency, properties);
  }

  private ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, currency, properties);
  }

}
