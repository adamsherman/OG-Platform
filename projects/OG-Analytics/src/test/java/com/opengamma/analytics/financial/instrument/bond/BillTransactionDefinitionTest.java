/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of bills transaction.
 */
public class BillTransactionDefinitionTest {

  private final static Currency EUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");

  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  private final static String ISSUER_BEL = "BELGIUM GOVT";
  private final static String ISSUER_GER = "GERMANY GOVT";
  private final static ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 2, 29);
  private final static double NOTIONAL = 1000;
  private final static BillSecurityDefinition BILL_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);

  private final static double QUANTITY = 123456;
  private final static ZonedDateTime SETTLE_DATE = DateUtils.getUTCDate(2012, 1, 18);
  private final static double SETTLE_AMOUT = -NOTIONAL * QUANTITY * 99.95;

  private final static String DSC_NAME = "EUR Discounting";
  private final static String CREDIT_NAME = "EUR BELGIUM GOVT";

  private final static BillTransactionDefinition BILL_TRA_DEFINITION = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, SETTLE_AMOUT);

  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new BillTransactionDefinition(null, QUANTITY, SETTLE_DATE, SETTLE_AMOUT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSettleDate() {
    new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, null, SETTLE_AMOUT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSettleAmount() {
    new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, -SETTLE_AMOUT);
  }

  @Test
  /**
   * Tests the bill getters.
   */
  public void getters() {
    assertEquals("Bill Transaction Definition: getter", BILL_SEC_DEFINITION, BILL_TRA_DEFINITION.getUnderlying());
    assertEquals("Bill Transaction Definition: getter", QUANTITY, BILL_TRA_DEFINITION.getQuantity());
    assertEquals("Bill Transaction Definition: getter", SETTLE_DATE, BILL_TRA_DEFINITION.getSettlementDate());
    assertEquals("Bill Transaction Definition: getter", SETTLE_AMOUT, BILL_TRA_DEFINITION.getSettlementAmount());
  }

  @Test
  /**
   * Tests the constructor from yield.
   */
  public void fromYield() {
    final double yield = 0.0020;
    final double accrualFactor = ACT360.getDayCountFraction(SETTLE_DATE, END_DATE);
    final double price = METHOD_BILL_SECURITY.priceFromYield(YIELD_CONVENTION, yield, accrualFactor);
    final double settlementAmount = -QUANTITY * price * NOTIONAL;
    final BillTransactionDefinition from = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, yield, CALENDAR);
    final BillTransactionDefinition constructed = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, settlementAmount);
    assertEquals("Bill Transaction Definition: fromYield", constructed, from);
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals("Bill Transaction Definition: equal-hash code", BILL_TRA_DEFINITION, BILL_TRA_DEFINITION);
    final BillTransactionDefinition other = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, SETTLE_AMOUT);
    assertEquals("Bill Transaction Definition: equal-hash code", BILL_TRA_DEFINITION, other);
    assertEquals("Bill Transaction Definition: equal-hash code", BILL_TRA_DEFINITION.hashCode(), other.hashCode());
    BillTransactionDefinition modified;
    modified = new BillTransactionDefinition(new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_GER), QUANTITY, SETTLE_DATE, SETTLE_AMOUT);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY + 10.0, SETTLE_DATE, SETTLE_AMOUT);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE.plusDays(1), SETTLE_AMOUT);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, SETTLE_AMOUT + 1.0);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeStandardDeprecated() {
    final ZonedDateTime referenceDateStandard = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, -SETTLEMENT_DAYS, CALENDAR);
    final BillTransaction transactionConverted1 = BILL_TRA_DEFINITION.toDerivative(referenceDateStandard, DSC_NAME, CREDIT_NAME);
    final BillSecurity purchased1 = BILL_SEC_DEFINITION.toDerivative(referenceDateStandard, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
    final BillSecurity standard1 = BILL_SEC_DEFINITION.toDerivative(referenceDateStandard, DSC_NAME, CREDIT_NAME);
    final BillTransaction transactionExpected1 = new BillTransaction(purchased1, QUANTITY, SETTLE_AMOUT, standard1);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected1, transactionConverted1);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeEarlyDeprecated() {
    final ZonedDateTime referenceDateEarly = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, -(SETTLEMENT_DAYS + 1), CALENDAR);
    final BillTransaction transactionConverted2 = BILL_TRA_DEFINITION.toDerivative(referenceDateEarly, DSC_NAME, CREDIT_NAME);
    final BillSecurity purchased2 = BILL_SEC_DEFINITION.toDerivative(referenceDateEarly, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
    final BillSecurity standard2 = BILL_SEC_DEFINITION.toDerivative(referenceDateEarly, DSC_NAME, CREDIT_NAME);
    final BillTransaction transactionExpected2 = new BillTransaction(purchased2, QUANTITY, SETTLE_AMOUT, standard2);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected2, transactionConverted2);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeAtSettleDeprecated() {
    final ZonedDateTime referenceDate = SETTLE_DATE;
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, SETTLE_AMOUT, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeBetweenSettleAndMaturityDeprecated() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeBetweenJustBeforeMaturityDeprecated() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(END_DATE, -1, CALENDAR);
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeBetweenAtMaturityDeprecated() {
    final ZonedDateTime referenceDate = END_DATE;
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate, DSC_NAME, CREDIT_NAME);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeStandard() {
    final ZonedDateTime referenceDateStandard = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, -SETTLEMENT_DAYS, CALENDAR);
    final BillTransaction transactionConverted1 = BILL_TRA_DEFINITION.toDerivative(referenceDateStandard);
    final BillSecurity purchased1 = BILL_SEC_DEFINITION.toDerivative(referenceDateStandard, SETTLE_DATE);
    final BillSecurity standard1 = BILL_SEC_DEFINITION.toDerivative(referenceDateStandard);
    final BillTransaction transactionExpected1 = new BillTransaction(purchased1, QUANTITY, SETTLE_AMOUT, standard1);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected1, transactionConverted1);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeEarly() {
    final ZonedDateTime referenceDateEarly = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, -(SETTLEMENT_DAYS + 1), CALENDAR);
    final BillTransaction transactionConverted2 = BILL_TRA_DEFINITION.toDerivative(referenceDateEarly);
    final BillSecurity purchased2 = BILL_SEC_DEFINITION.toDerivative(referenceDateEarly, SETTLE_DATE);
    final BillSecurity standard2 = BILL_SEC_DEFINITION.toDerivative(referenceDateEarly);
    final BillTransaction transactionExpected2 = new BillTransaction(purchased2, QUANTITY, SETTLE_AMOUT, standard2);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected2, transactionConverted2);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeAtSettle() {
    final ZonedDateTime referenceDate = SETTLE_DATE;
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, SETTLE_AMOUT, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeBetweenSettleAndMaturity() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeBetweenJustBeforeMaturity() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(END_DATE, -1, CALENDAR);
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeBetweenAtMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }
}
