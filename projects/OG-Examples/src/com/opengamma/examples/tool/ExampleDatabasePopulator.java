/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.examples.generator.PortfolioGeneratorTool;
import com.opengamma.examples.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.examples.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.loader.ExampleMultiAssetPortfolioLoader;
import com.opengamma.examples.loader.ExampleSwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.loader.ExampleViewsPopulator;
import com.opengamma.examples.loader.PortfolioLoaderHelper;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Single class that populates the database with data for running the example server.
 * <p>
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleDatabasePopulator extends AbstractExampleTool {
  
  
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabasePopulator.class);
  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "MultiCurrency Swap Portfolio";
  /**
   * The name of Forward Swap Portfolio
   */
  public static final String FORWARD_SWAP_PORTFOLION_NAME = "Forward Swap Portfolio";
  /**
   * The name of Cap/Floor portfolio
   */
  public static final String CAP_FLOOR_PORTFOLIO_NAME = "Cap/Floor Portfolio";

  /**
   * The currencies.
   */
  private static final Set<Currency> s_currencies = Sets.newHashSet(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.AUD, Currency.CAD);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   * 
   * @param args the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    try {
      new ExampleDatabasePopulator().initAndRun(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {

    loadCurveAndSurfaceDefinitions();
    loadDefaultVolatilityCubeDefinition();
    loadTimeSeriesRating();
//    loadYieldCurves();
    loadSimulatedHistoricalData();
    loadEquityPortfolio();
    loadEquityOptionPortfolio();
    loadMultiCurrencySwapPortfolio();
    loadForwardSwapPortfolio();
//    loadSwaptionPortfolio();
    loadBondPortfolio();
    loadCapFloorCMSSpreadPortfolio();
    loadCapFloorPortfolio();
    loadCashPortfolio();
    loadFRAPortfolio();
    loadLiborRawSecurities();
    loadMixedFXPortfolio();
//    loadMixedPortfolio();
    loadMultiAssetPortfolio();
    loadViews();
  }

  private void loadCurveAndSurfaceDefinitions() {
    ExampleCurveAndSurfaceDefinitionLoader curveLoader = new ExampleCurveAndSurfaceDefinitionLoader();
    s_logger.info("Creating curve and surface definitions");
    curveLoader.run(getToolContext());
    s_logger.info("Finished");
  }

  private void loadDefaultVolatilityCubeDefinition() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    
    ConfigDocument<VolatilityCubeDefinition> doc = new ConfigDocument<VolatilityCubeDefinition>(VolatilityCubeDefinition.class);
    doc.setName("SECONDARY_USD");
    doc.setValue(createDefaultDefinition());
    s_logger.info("Populating vol cube defn " + doc.getName());
    ConfigMasterUtils.storeByName(configMaster, doc);
    
    VolatilityCubeConfigPopulator.populateVolatilityCubeConfigMaster(configMaster);
  }

  private static final class Log {

    private final String _str;

    private Log(final String str) {
      s_logger.info(str);
      _str = str;
    }

    private void done() {
      s_logger.info(_str + " - finished");
    }

    private void fail(final RuntimeException e) {
      System.err.println(_str + " - failed - " + e.getMessage());
      throw e;
    }

  }

  private void loadMultiAssetPortfolio() {
    final Log log = new Log("Creating example multi asset portfolio");
    try {
      ExampleMultiAssetPortfolioLoader mixedPortfolioLoader = new ExampleMultiAssetPortfolioLoader();
      mixedPortfolioLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadTimeSeriesRating() {
    final Log log = new Log("Creating Timeseries configuration");
    try {
      ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
      timeSeriesRatingLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadYieldCurves() {
    final Log log = new Log("Creating yield curve definitions");
    try {
      YieldCurveConfigPopulator.populateCurveConfigMaster(getToolContext().getConfigMaster());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSimulatedHistoricalData() {
    final Log log = new Log("Creating simulated historical timeseries");
    try {
      final ExampleHistoricalDataGeneratorTool historicalDataGenerator = new ExampleHistoricalDataGeneratorTool();
      historicalDataGenerator.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityPortfolio() {
    final Log log = new Log("Creating example equity portfolio");
    try {
      final ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
      equityLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private PortfolioGeneratorTool portfolioGeneratorTool() {
    final PortfolioGeneratorTool tool = new PortfolioGeneratorTool();
    tool.setCounterPartyGenerator(new StaticNameGenerator(AbstractPortfolioGeneratorTool.DEFAULT_COUNTER_PARTY));
    return tool;
  }

  private void loadMultiCurrencySwapPortfolio() {
    final Log log = new Log("Creating example multi currency swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAP_PORTFOLIO_NAME, "Swap", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadForwardSwapPortfolio() {
    final Log log = new Log("Creating example forward swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), FORWARD_SWAP_PORTFOLION_NAME, "ForwardSwap", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSwaptionPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), "Swaption Portfolio", "Swaption", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadBondPortfolio() {
    final Log log = new Log("Creating example bond portfolio");
    try {
      // TODO: load from CSV file
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCapFloorCMSSpreadPortfolio() {
    final Log log = new Log("Creating example cap/floor CMS spread portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), "Cap/Floor CMS Spread Portfolio", "CapFloorCMSSpread", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCapFloorPortfolio() {
    final Log log = new Log("Creating example cap/floor portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), CAP_FLOOR_PORTFOLIO_NAME, "CapFloor", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCashPortfolio() {
    final Log log = new Log("Creating example cash portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), "Cash Portfolio", "Cash", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Creating example equity option portfolio");
    try {
      // TODO: load from CSV file
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFRAPortfolio() {
    final Log log = new Log("Creating example FRA portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), "FRA Portfolio", "FRA", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadMixedFXPortfolio() {
    final Log log = new Log("Creating mixed FX portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), "Mixed FX Portfolio", "MixedFX", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }
  
  private void loadMixedPortfolio() {
    final Log log = new Log("Creating mixed portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), "Mixed Portfolio", "Mixed", true, null);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadLiborRawSecurities() {
    final Log log = new Log("Creating libor raw securities");
    try {
      PortfolioLoaderHelper.persistLiborRawSecurities(getAllCurrencies(), getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final ExampleViewsPopulator populator = new ExampleViewsPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private static Set<Currency> getAllCurrencies() {
    return s_currencies;
  }
  
  private static VolatilityCubeDefinition createDefaultDefinition() {
    
    VolatilityCubeDefinition volatilityCubeDefinition = new VolatilityCubeDefinition();
    volatilityCubeDefinition.setSwapTenors(Lists.newArrayList(Tenor.ofMonths(3), Tenor.ofYears(1), Tenor.ofYears(2),
        Tenor.ofYears(5), Tenor.ofYears(10), Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(30)));
    volatilityCubeDefinition.setOptionExpiries(Lists.newArrayList(Tenor.ofMonths(3), Tenor.ofMonths(6),
        Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(4), Tenor.ofYears(5), Tenor.ofYears(10), Tenor.ofYears(15),
        Tenor.ofYears(20)));
    
    int[] values = new int[] {0, 20, 25, 50, 70, 75, 100, 200, 5 };
    List<Double> relativeStrikes = new ArrayList<Double>(values.length * 2 - 1);
    for (int value : values) {
      relativeStrikes.add(Double.valueOf(value));
      if (value != 0) {
        relativeStrikes.add(Double.valueOf(-value));
      }
    }
    Collections.sort(relativeStrikes);
    
    volatilityCubeDefinition.setRelativeStrikes(relativeStrikes);
    return volatilityCubeDefinition;
  }
}
