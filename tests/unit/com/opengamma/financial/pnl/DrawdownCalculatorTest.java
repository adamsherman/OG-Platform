/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public class DrawdownCalculatorTest {
  private static final int N = 100;
  private static final long[] T = new long[N];
  private static final double[] FLAT = new double[N];
  private static final double[] FLAT_DRAWDOWN = new double[N];
  private static final double[] HIGH_FIRST = new double[N];
  private static final double[] HIGH_FIRST_DRAWDOWN = new double[N];
  private static final double[] X = new double[N];
  private static final double[] X_DRAWDOWN = new double[N];
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.DATE_EPOCH_DAYS;
  private static final Function1D<DoubleTimeSeries<?>, DoubleTimeSeries<?>> CALCULATOR = new DrawdownCalculator();

  static {
    for (int i = 0; i < N; i++) {
      T[i] = i;
      FLAT[i] = 100;
      FLAT_DRAWDOWN[i] = 0;
      HIGH_FIRST[i] = 100;
      HIGH_FIRST_DRAWDOWN[i] = 2. / 3;
      if (i % 10 == 0) {
        X[i] = (i + 10) * 10;
        X_DRAWDOWN[i] = 0;
      } else {
        X[i] = 100;
        X_DRAWDOWN[i] = (i / 10) * 100 / X[10 * (i / 10)];
      }
    }
    HIGH_FIRST[0] = 300;
    HIGH_FIRST_DRAWDOWN[0] = 0;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS() {
    CALCULATOR.evaluate(new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new long[0], new double[0]));
  }

  @Test
  public void test() {
    assertTimeSeriesEquals(CALCULATOR.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, T, FLAT)), new FastArrayLongDoubleTimeSeries(ENCODING, T, FLAT_DRAWDOWN));
    assertTimeSeriesEquals(CALCULATOR.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, T, HIGH_FIRST)), new FastArrayLongDoubleTimeSeries(ENCODING, T, HIGH_FIRST_DRAWDOWN));
    assertTimeSeriesEquals(CALCULATOR.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, T, X)), new FastArrayLongDoubleTimeSeries(ENCODING, T, X_DRAWDOWN));
  }

  private void assertTimeSeriesEquals(final DoubleTimeSeries<?> ts1, final DoubleTimeSeries<?> ts2) {
    final FastLongDoubleTimeSeries fastTS1 = ts1.toFastLongDoubleTimeSeries();
    final FastLongDoubleTimeSeries fastTS2 = ts2.toFastLongDoubleTimeSeries();
    assertEquals(fastTS1.getEncoding(), fastTS2.getEncoding());
    assertArrayEquals(fastTS1.timesArrayFast(), fastTS2.timesArrayFast());
    assertArrayEquals(fastTS1.valuesArrayFast(), fastTS2.valuesArrayFast(), 1e-15);
  }
}
