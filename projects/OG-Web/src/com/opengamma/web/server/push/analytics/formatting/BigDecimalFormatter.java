/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.server.conversion.DoubleValueDecimalPlaceFormatter;
import com.opengamma.web.server.conversion.DoubleValueFormatter;
import com.opengamma.web.server.conversion.DoubleValueSignificantFiguresFormatter;
import com.opengamma.web.server.conversion.DoubleValueSizeBasedDecimalPlaceFormatter;

/**
 *
 */
/* package */ class BigDecimalFormatter implements Formatter<BigDecimal> {

  private static final Logger s_logger = LoggerFactory.getLogger(BigDecimalFormatter.class);
  private static final Map<String, DoubleValueFormatter> s_formatters = Maps.newHashMap();
  private static final DoubleValueFormatter s_defaultFormatter = DoubleValueSignificantFiguresFormatter.NON_CCY_5SF;

  static {
    // General
    s_formatters.put(ValueRequirementNames.DISCOUNT_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.YIELD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOLATILITY_SURFACE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOLATILITY_SURFACE_DATA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.COST_OF_CARRY, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Pricing
    s_formatters.put(ValueRequirementNames.PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.PV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.PAR_RATE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.VALUE_THETA, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.POSITION_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.VALUE_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.SECURITY_MARKET_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.SECURITY_MODEL_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.UNDERLYING_MARKET_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.UNDERLYING_MODEL_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.DAILY_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));

    // PnL
    s_formatters.put(ValueRequirementNames.PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.DAILY_PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Greeks
    s_formatters.put(ValueRequirementNames.DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DELTA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.STRIKE_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.STRIKE_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA_P_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VEGA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VEGA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.THETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CARRY_RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.YIELD_CURVE_JACOBIAN, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.SPEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.SPEED_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DVANNA_DVOL, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DUAL_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DUAL_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.IMPLIED_VOLATILITY, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Position/value greeks
    addBulkConversion("(POSITION_|VALUE_).*", DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Series analysis
    s_formatters.put(ValueRequirementNames.SKEW, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.FISHER_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.PEARSON_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // VaR
    s_formatters.put(ValueRequirementNames.HISTORICAL_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.PARAMETRIC_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.HISTORICAL_VAR_STDDEV, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.CONDITIONAL_HISTORICAL_VAR,
                     DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Capital Asset Pricing
    s_formatters.put(ValueRequirementNames.CAPM_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Traditional Risk-Reward
    s_formatters.put(ValueRequirementNames.SHARPE_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.TREYNOR_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.JENSENS_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.TOTAL_RISK_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.WEIGHT, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // Bonds
    s_formatters.put(ValueRequirementNames.CLEAN_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.YTM, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.MARKET_YTM, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.MARKET_DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.MACAULAY_DURATION, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.CONVEXITY, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.Z_SPREAD, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.CONVERTION_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.IMPLIED_REPO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.GROSS_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.NET_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.BOND_TENOR, DoubleValueDecimalPlaceFormatter.NON_CCY_2DP);
    s_formatters.put(ValueRequirementNames.NS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.NSS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Options
    s_formatters.put(ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // FX
    s_formatters.put(ValueRequirementNames.FX_PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
  }

  private static void addBulkConversion(String valueRequirementFieldNamePattern, DoubleValueFormatter conversionSettings) {
    Pattern pattern = Pattern.compile(valueRequirementFieldNamePattern);
    for (Field field : ValueRequirementNames.class.getFields()) {
      if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC)
          && String.class.equals(field.getType()) && pattern.matcher(field.getName()).matches()) {
        String fieldValue;
        try {
          fieldValue = (String) field.get(null);
          s_formatters.put(fieldValue, conversionSettings);
        } catch (Exception e) {
          s_logger.debug("Unexpected exception initializing formatter", e);
        }
      }
    }
  }

  private DoubleValueFormatter getFormatter(ValueSpecification valueSpec) {
    DoubleValueFormatter valueNameFormatter = s_formatters.get(valueSpec.getValueName());
    if (valueNameFormatter != null) {
      return valueNameFormatter;
    } else {
      return s_defaultFormatter;
    }
  }


  @Override
  public String formatForDisplay(BigDecimal value, ValueSpecification valueSpec) {
    DoubleValueFormatter formatter = getFormatter(valueSpec);
    String formattedNumber = formatter.format(value);
    String formattedValue;
    if (formatter.isCurrencyAmount()) {
      Set<String> currencyValues = valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY);
      String ccy;
      if (currencyValues == null) {
        formattedValue = formattedNumber;
      } else if (currencyValues.isEmpty()) {
        formattedValue = formattedNumber;
      } else {
        ccy = currencyValues.iterator().next();
        formattedValue = ccy + " " + formattedNumber;
      }
    } else {
      formattedValue = formattedNumber;
    }
    return formattedValue;
  }

  @Override
  public String formatForExpandedDisplay(BigDecimal value, ValueSpecification valueSpec) {
    return formatForDisplay(value, valueSpec);
  }

  @Override
  public BigDecimal formatForHistory(BigDecimal value, ValueSpecification valueSpec) {
    DoubleValueFormatter formatter = getFormatter(valueSpec);
    return formatter.getRoundedValue(value);
  }
}
