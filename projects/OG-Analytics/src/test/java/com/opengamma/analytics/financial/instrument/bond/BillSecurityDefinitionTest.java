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
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of bills security.
 */
public class BillSecurityDefinitionTest {

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

  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new BillSecurityDefinition(null, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate() {
    new BillSecurityDefinition(EUR, null, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, null, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYield() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, null, ACT360, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, null, ISSUER_BEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIssuer() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notionalPositive() {
    new BillSecurityDefinition(EUR, END_DATE, -NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  @Test
  /**
   * Tests the bill getters.
   */
  public void getters() {
    assertEquals("Bill Security Definition: getter", EUR, BILL_SEC_DEFINITION.getCurrency());
    assertEquals("Bill Security Definition: getter", END_DATE, BILL_SEC_DEFINITION.getEndDate());
    assertEquals("Bill Security Definition: getter", NOTIONAL, BILL_SEC_DEFINITION.getNotional());
    assertEquals("Bill Security Definition: getter", SETTLEMENT_DAYS, BILL_SEC_DEFINITION.getSettlementDays());
    assertEquals("Bill Security Definition: getter", CALENDAR, BILL_SEC_DEFINITION.getCalendar());
    assertEquals("Bill Security Definition: getter", YIELD_CONVENTION, BILL_SEC_DEFINITION.getYieldConvention());
    assertEquals("Bill Security Definition: getter", ACT360, BILL_SEC_DEFINITION.getDayCount());
    assertEquals("Bill Security Definition: getter", ISSUER_BEL, BILL_SEC_DEFINITION.getIssuer());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION, BILL_SEC_DEFINITION);
    final BillSecurityDefinition other = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION, other);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.hashCode(), other.hashCode());
    BillSecurityDefinition modified;
    modified = new BillSecurityDefinition(Currency.USD, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE.plusDays(1), NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL + 10.0, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS + 1, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, new MondayToFridayCalendar("OTHER"), YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT"), ACT360, ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, DayCountFactory.INSTANCE.getDayCount("Actual/365"), ISSUER_BEL);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_GER);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivativeDeprecated() {
    final String dsc = "EUR Discounting";
    final String credit = "EUR BELGIUM GOVT";
    final ZonedDateTime standardSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillSecurity securityConverted1 = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, standardSettlementDate, dsc, credit);
    final double standardSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, standardSettlementDate);
    final double endTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, END_DATE);
    final double accrualFactorStandard = ACT360.getDayCountFraction(standardSettlementDate, END_DATE);
    final BillSecurity securityExpected1 = new BillSecurity(EUR, standardSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorStandard, ISSUER_BEL, credit, dsc);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted1);
    final BillSecurity securityConverted2 = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, dsc, credit);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted2);
    final ZonedDateTime otherSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS + 1, CALENDAR);
    final BillSecurity securityConverted3 = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, otherSettlementDate, dsc, credit);
    final double otherSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, otherSettlementDate);
    final double accrualFactorOther = ACT360.getDayCountFraction(otherSettlementDate, END_DATE);
    final BillSecurity securityExpected3 = new BillSecurity(EUR, otherSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorOther, ISSUER_BEL, credit, dsc);
    assertEquals("Bill Security Definition: toDerivative", securityExpected3, securityConverted3);
  }

  @Test
  /**
   * Tests the toDerivative methods.
   */
  public void toDerivative() {
    final ZonedDateTime standardSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillSecurity securityConverted1 = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, standardSettlementDate);
    final double standardSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, standardSettlementDate);
    final double endTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, END_DATE);
    final double accrualFactorStandard = ACT360.getDayCountFraction(standardSettlementDate, END_DATE);
    final BillSecurity securityExpected1 = new BillSecurity(EUR, standardSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorStandard, ISSUER_BEL);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted1);
    final BillSecurity securityConverted2 = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted2);
    final ZonedDateTime otherSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS + 1, CALENDAR);
    final BillSecurity securityConverted3 = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, otherSettlementDate);
    final double otherSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, otherSettlementDate);
    final double accrualFactorOther = ACT360.getDayCountFraction(otherSettlementDate, END_DATE);
    final BillSecurity securityExpected3 = new BillSecurity(EUR, otherSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorOther, ISSUER_BEL);
    assertEquals("Bill Security Definition: toDerivative", securityExpected3, securityConverted3);
  }
}
