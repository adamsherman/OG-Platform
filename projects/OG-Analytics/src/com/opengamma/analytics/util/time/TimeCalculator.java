/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

/**
 * Converts dates to 'Analytics Time'. The latter are stored as doubles, 
 * and typically represent the fraction of years between some date and the current one.
 */
public abstract class TimeCalculator {
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {
    ArgumentChecker.notNull(date1, "date1");
    ArgumentChecker.notNull(date1, "date2");

    // Note that here we convert date2 to the same zone as date1 so we don't accidentally gain or lose a day.
    ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());

    final boolean timeIsNegative = date1.isAfter(rebasedDate2); // date1 >= date2

    if (!timeIsNegative) {
      final double time = ACT_ACT.getDayCountFraction(date1, rebasedDate2);
      return time;
    }
    return -1.0 * ACT_ACT.getDayCountFraction(rebasedDate2, date1);
  }

  public static double getTimeBetween(final LocalDate date1, final LocalDate date2) {
    return getTimeBetween(date1.atStartOfDayInZone(TimeZone.UTC), date2.atStartOfDayInZone(TimeZone.UTC));
  }

  public static double getTimeBetween(final ZonedDateTime zdt1, final LocalDate date2) {
    ZonedDateTime zdt2 = date2.atStartOfDayInZone(TimeZone.UTC);
    ZonedDateTime rebasedZdt1 = zdt1.withZoneSameInstant(TimeZone.UTC);
    return getTimeBetween(rebasedZdt1, zdt2);
  }

  public static double getTimeBetween(final LocalDate date1, final ZonedDateTime zdt2) {
    ZonedDateTime zdt1 = date1.atStartOfDayInZone(TimeZone.UTC);
    ZonedDateTime rebasedZdt2 = zdt2.withZoneSameInstant(TimeZone.UTC);
    return getTimeBetween(zdt1, rebasedZdt2);
  }
}
