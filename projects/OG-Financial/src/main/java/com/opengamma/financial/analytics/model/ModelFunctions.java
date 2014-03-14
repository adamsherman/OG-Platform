/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.cds.CDSFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.financial.analytics.model.discounting.DiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.fx.FXForwardPricingFunctions;
import com.opengamma.financial.analytics.model.g2ppdiscounting.G2ppPricingFunctions;
import com.opengamma.financial.analytics.model.horizon.HorizonFunctions;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhitePricingFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.option.OptionFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleInstrumentFunctions;
import com.opengamma.financial.analytics.model.swaption.SwaptionFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.VolatilityFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class ModelFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new ModelFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(MarginPriceFunction.class));
  }

  protected FunctionConfigurationSource bondFunctionConfiguration() {
    return BondFunctions.instance();
  }

  protected FunctionConfigurationSource bondFutureOptionFunctionConfiguration() {
    return BondFutureOptionFunctions.instance();
  }

  protected FunctionConfigurationSource cdsFunctionConfiguration() {
    return CDSFunctions.instance();
  }

  protected FunctionConfigurationSource creditFunctionConfiguration() {
    return CreditFunctions.instance();
  }

  protected FunctionConfigurationSource curveFunctionConfiguration() {
    return CurveFunctions.instance();
  }

  protected FunctionConfigurationSource equityFunctionConfiguration() {
    return EquityFunctions.instance();
  }

  /**
   * Adds deprecated interest rate instrument functions.
   * @return A configuration source containing the deprecated interest rate instrument functions
   * @deprecated The current versions of these functions are added in {@link #discountingFunctionConfiguration}
   */
  @Deprecated
  protected FunctionConfigurationSource fixedIncomeFunctionConfiguration() {
    return com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed with the discounting method.
   * @return A configuration source containing these functions.
   */
  protected FunctionConfigurationSource discountingFunctionConfiguration() {
    return DiscountingPricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use Black surfaces and curve constructed with
   * the discounting method.
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource blackDiscountingFunctionConfiguration() {
    return BlackDiscountingPricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed using the Hull-White
   * one factor discounting method.
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource hullWhitePricingFunctionConfiguration() {
    return HullWhitePricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed using the G2++
   * discounting method
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource g2ppPricingFunctionConfiguration() {
    return G2ppPricingFunctions.instance();
  }

  protected FunctionConfigurationSource fxPricingFunctionConfiguration() {
    return FXForwardPricingFunctions.instance();
  }

  protected FunctionConfigurationSource yieldCurveFunctionConfiguration() {
    return InterestRateFunctions.instance();
  }

  protected FunctionConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected FunctionConfigurationSource forexFunctionConfiguration() {
    return ForexFunctions.instance();
  }

  protected FunctionConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected FunctionConfigurationSource futureOptionFunctionConfiguration() {
    return FutureOptionFunctions.instance();
  }

  /**
   * Adds horizon functions.
   * @return A configuration source containing horizon functions.
   */
  protected FunctionConfigurationSource horizonFunctionConfiguration() {
    return HorizonFunctions.instance();
  }

  /**
   * Adds interest rate future-specific functions.
   * @return A configuration source containing the deprecated interest rate future functions.
   * @deprecated The current versions of these functions are added in {@link ModelFunctions#blackDiscountingFunctionConfiguration}
   */
  @Deprecated
  protected FunctionConfigurationSource interestRateFutureFunctionConfiguration() {
    return FutureFunctions.deprecated();
  }

  protected FunctionConfigurationSource irFutureOptionFunctionConfiguration() {
    return IRFutureOptionFunctions.instance();
  }

  /**
   * Adds general option functions.
   * @return A configuration source containing option functions
   * @deprecated The underlying-specific functions should be used
   */
  @Deprecated
  protected FunctionConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  protected FunctionConfigurationSource pnlFunctionConfiguration() {
    return PNLFunctions.instance();
  }

  protected FunctionConfigurationSource riskFactorFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  /**
   * Adds SABR pricing functions for swaptions, cap/floors, CMS and cap/floor CMS spreads
   * @return A configuration source containing the deprecated functions
   * @deprecated The current versions of these functions are added in {@link ModelFunctions#sabrDiscountingFunctionConfiguration()}
   */
  @Deprecated
  protected FunctionConfigurationSource sabrCubeFunctionConfiguration() {
    return SABRCubeFunctions.instance();
  }

  protected FunctionConfigurationSource sabrDiscountingFunctionConfiguration() {
    return SABRDiscountingPricingFunctions.instance();
  }

  protected FunctionConfigurationSource sensitivitiesFunctionConfiguration() {
    return SensitivitiesFunctions.instance();
  }

  protected FunctionConfigurationSource simpleInstrumentFunctionConfiguration() {
    return SimpleInstrumentFunctions.instance();
  }

  protected FunctionConfigurationSource swaptionFunctionConfiguration() {
    return SwaptionFunctions.instance();
  }

  protected FunctionConfigurationSource varFunctionConfiguration() {
    return VaRFunctions.instance();
  }

  protected FunctionConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  protected FunctionConfigurationSource futureCurveFunctionConfiguration() {
    return com.opengamma.financial.analytics.model.curve.future.FutureFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(),
        bondFunctionConfiguration(),
        bondFutureOptionFunctionConfiguration(),
        cdsFunctionConfiguration(),
        creditFunctionConfiguration(),
        curveFunctionConfiguration(),
        equityFunctionConfiguration(),
        fixedIncomeFunctionConfiguration(),
        forexFunctionConfiguration(),
        futureFunctionConfiguration(),
        futureOptionFunctionConfiguration(),
        horizonFunctionConfiguration(),
        irFutureOptionFunctionConfiguration(),
        optionFunctionConfiguration(),
        pnlFunctionConfiguration(),
        riskFactorFunctionConfiguration(),
        sabrCubeFunctionConfiguration(),
        sensitivitiesFunctionConfiguration(),
        simpleInstrumentFunctionConfiguration(),
        swaptionFunctionConfiguration(),
        varFunctionConfiguration(),
        volatilityFunctionConfiguration(),
        yieldCurveFunctionConfiguration(),
        forwardFunctionConfiguration(),
        futureCurveFunctionConfiguration(),
        discountingFunctionConfiguration(),
        hullWhitePricingFunctionConfiguration(),
        interestRateFutureFunctionConfiguration(),
        fxPricingFunctionConfiguration(),
        blackDiscountingFunctionConfiguration(),
        sabrDiscountingFunctionConfiguration(),
        g2ppPricingFunctionConfiguration());
  }

}
