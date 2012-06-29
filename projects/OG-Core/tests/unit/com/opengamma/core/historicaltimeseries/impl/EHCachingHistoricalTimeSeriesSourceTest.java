/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Test {@link EHCachingHistoricalTimeSeriesSource}.
 */
@Test
public class EHCachingHistoricalTimeSeriesSourceTest {

  private HistoricalTimeSeriesSource _underlyingSource;
  private EHCachingHistoricalTimeSeriesSource _cachingSource;

  private static final UniqueId UID = UniqueId.of("A", "B");

  @BeforeMethod
  public void setUp() throws Exception {
    EHCacheUtils.clearAll();
    _underlyingSource = mock(HistoricalTimeSeriesSource.class);
    CacheManager cm = EHCacheUtils.createCacheManager();
    _cachingSource = new EHCachingHistoricalTimeSeriesSource(_underlyingSource, cm);
  }

  //-------------------------------------------------------------------------
  public void getHistoricalTimeSeries_UniqueId() {
    LocalDate[] dates = {LocalDate.of(2011, 6, 30)};
    double[] values = {12.34d};
    ArrayLocalDateDoubleTimeSeries timeSeries = new ArrayLocalDateDoubleTimeSeries(dates, values);
    HistoricalTimeSeries series = new SimpleHistoricalTimeSeries(UID, timeSeries);
    
    when(_underlyingSource.getHistoricalTimeSeries(UID)).thenReturn(series);
    
    // Fetching same series twice should return same result
    HistoricalTimeSeries series1 = _cachingSource.getHistoricalTimeSeries(UID);
    HistoricalTimeSeries series2 = _cachingSource.getHistoricalTimeSeries(UID);
    assertEquals(series, series1);
    assertEquals(series, series2);
    assertEquals(series1, series2);
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingSource, times(1)).getHistoricalTimeSeries(UID);
  }
  
  public void getExternalIdBundle_UniqueId() {
    ExternalId djxTicker = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "DJX Index");
    ExternalId djxBUID = ExternalId.of(ExternalSchemes.BLOOMBERG_BUID, "EI09JDX");
    ExternalIdBundle idBundle = ExternalIdBundle.of(djxTicker, djxBUID);
    
    when(_underlyingSource.getExternalIdBundle(UID)).thenReturn(idBundle);
    
    // Fetching same series twice should return same result
    ExternalIdBundle bundle1 = _cachingSource.getExternalIdBundle(UID);
    ExternalIdBundle bundle2 = _cachingSource.getExternalIdBundle(UID);
    assertEquals(idBundle, bundle1);
    assertEquals(idBundle, bundle2);
    assertEquals(bundle1, bundle2);
    
    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingSource, times(1)).getExternalIdBundle(UID);
  }


}
