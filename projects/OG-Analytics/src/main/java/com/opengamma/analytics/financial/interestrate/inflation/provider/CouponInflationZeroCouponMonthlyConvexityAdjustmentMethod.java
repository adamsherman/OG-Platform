/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.provider.description.inflation.InflationConvexityAdjustmentProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for inflation zero-coupon. The price is computed by index estimation, discounting and using a convexity adjustment.
 * See note "Inflation convexity adjustment" by Arroub Zine-eddine for details.
 */
public class CouponInflationZeroCouponMonthlyConvexityAdjustmentMethod {

  /**
   * The convexity adjustment function used in the pricing.
   */
  private static final InflationMarketModelConvexityAdjustmentForCoupon CONVEXITY_ADJUSTMENT_FUNCTION = new InflationMarketModelConvexityAdjustmentForCoupon();

  /**
   * Computes the net amount of the zero-coupon coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The net amount.
   */
  public MultipleCurrencyAmount netAmount(final CouponInflationZeroCouponMonthly coupon, final InflationConvexityAdjustmentProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndex = indexEstimation(coupon, inflation);
    final double convexityAdjustemnt = CONVEXITY_ADJUSTMENT_FUNCTION.getZeroCouponConvexityAdjustment(coupon, inflation);
    final double netAmount = (estimatedIndex / coupon.getIndexStartValue() * convexityAdjustemnt - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), netAmount);
  }

  /**
   * Computes the present value of the zero-coupon coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponInflationZeroCouponMonthly coupon, final InflationConvexityAdjustmentProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double discountFactor = inflation.getInflationProvider().getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    return netAmount(coupon, inflation).multipliedBy(discountFactor);
  }

  /**
   * Computes the estimated index with the weight and the reference end date.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The estimated index.
   */
  public double indexEstimation(final CouponInflationZeroCouponMonthly coupon, final InflationConvexityAdjustmentProviderInterface inflation) {
    final double estimatedIndex = inflation.getInflationProvider().getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime());
    return estimatedIndex;
  }

  /**
   * Compute the present value sensitivity to rates of a Inflation coupon.
   * @param coupon The coupon.
   * @param inflation The inflation provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final CouponInflationZeroCouponMonthly coupon, final InflationConvexityAdjustmentProviderInterface inflation) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(inflation, "Inflation");
    final double estimatedIndex = indexEstimation(coupon, inflation);
    final double convexityAdjustemnt = CONVEXITY_ADJUSTMENT_FUNCTION.getZeroCouponConvexityAdjustment(coupon, inflation);
    final double discountFactor = inflation.getInflationProvider().getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double discountFactorBar = (estimatedIndex / coupon.getIndexStartValue() * convexityAdjustemnt - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional() * pvBar;
    final double estimatedIndexBar = 1.0 / coupon.getIndexStartValue() * convexityAdjustemnt * discountFactor * coupon.getNotional() * pvBar;
    final Map<String, List<DoublesPair>> resultMapDisc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * discountFactor * discountFactorBar));
    resultMapDisc.put(inflation.getInflationProvider().getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<>();
    final List<DoublesPair> listPrice = new ArrayList<>();
    listPrice.add(new DoublesPair(coupon.getReferenceEndTime(), estimatedIndexBar));
    resultMapPrice.put(inflation.getInflationProvider().getName(coupon.getPriceIndex()), listPrice);
    final InflationSensitivity inflationSensitivity = InflationSensitivity.ofYieldDiscountingAndPriceIndex(resultMapDisc, resultMapPrice);
    return MultipleCurrencyInflationSensitivity.of(coupon.getCurrency(), inflationSensitivity);
  }

}
