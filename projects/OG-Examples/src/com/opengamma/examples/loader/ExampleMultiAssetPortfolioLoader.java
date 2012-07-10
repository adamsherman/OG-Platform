/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Example code to load a multi asset portfolio.
 */
@Scriptable
public class ExampleMultiAssetPortfolioLoader extends AbstractExampleTool {
  
  /**
   * Example mixed portfolio name
   */
  public static final String PORTFOLIO_NAME = "Multi Asset Portfolio";
  /**
   * Portfolio currencies
   */
  public static final Currency[] s_currencies = new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.NZD, Currency.DKK};
  
  private static final String ID_SCHEME = "MULTI_ASSET_PORFOLIO_LOADER";
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final ExternalId USDLIBOR3M = ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDLIBORP3M");
  private static final LocalDate TODAY = LocalDate.now();

  public static void main(String[] args) { //CSIGNORE
    new ExampleMultiAssetPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  private void persistToPortfolio() {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(PORTFOLIO_NAME);
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    addPortfolioNode(rootNode, getIborSwaps(), "Ibor swaps", BigDecimal.ONE);
    addPortfolioNode(rootNode, getCMSwaps(), "CM swaps", BigDecimal.ONE);
//    addPortfolioNode(rootNode, getSimpleFixedIncome(), "Fixed income", BigDecimal.ONE);
    addPortfolioNode(rootNode, getSimpleFX(), "FX forward", BigDecimal.ONE);
    addPortfolioNode(rootNode, getFXOptions(), "FX options", BigDecimal.ONE);
//    addBondNode(rootNode);
    addPortfolioNode(rootNode, getSwaptions(), "Swaptions", BigDecimal.ONE);
    addPortfolioNode(rootNode, getIborCapFloor(), "Ibor cap/floor", BigDecimal.ONE);
    addPortfolioNode(rootNode, getCMCapFloor(), "CM cap/floor", BigDecimal.ONE);
//    addPortfolioNode(rootNode, getIRFutureOptions(), "IR future options", BigDecimal.valueOf(100));
//    addEquityNode(rootNode);
    
    portfolioMaster.add(portfolioDoc);
  }

  private void addEquityNode(ManageablePortfolioNode rootNode) {
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode("Equity");
    
    EquityVarianceSwapSecurity equityVarianceSwap = new EquityVarianceSwapSecurity(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "DJX"), 
        Currency.USD, 0.5, 1000000.0, true, 250.0, ZonedDateTime.of(LocalDateTime.of(2010, 11, 1, 16, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2012, 11, 1, 16, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2010, 11, 1, 16, 0), TimeZone.UTC), ExternalSchemes.currencyRegionId(Currency.USD), SimpleFrequency.DAILY);
    equityVarianceSwap.setName("Equity Variance Swap, USD 1MM, strike=0.5, maturing 2012-11-01");
    equityVarianceSwap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    storeFinancialSecurity(equityVarianceSwap);
    addPosition(portfolioNode, equityVarianceSwap, BigDecimal.ONE);
    
    EquityIndexDividendFutureSecurity dividendFuture = new EquityIndexDividendFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2011, 12, 16, 17, 30), TimeZone.UTC)), 
        "XEUR", "XEUR", Currency.USD, 1000.0, ZonedDateTime.of(LocalDateTime.of(2011, 12, 16, 17, 30), TimeZone.UTC), ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "HSBA"), "STOCK FUTURE");

    dividendFuture.setName("HSBC Holdings SSDF Dec11");
    dividendFuture.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "H2SBZ1GR"));
    storeFinancialSecurity(dividendFuture);
    addPosition(portfolioNode, dividendFuture, BigDecimal.valueOf(100));
    
    EquityFutureSecurity equityFuture = new EquityFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2011, 12, 16, 17, 30), TimeZone.UTC)), 
        "XCME", "XCME", Currency.USD, 250.0, ZonedDateTime.of(LocalDateTime.of(2012, 12, 20, 21, 15), TimeZone.UTC), ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "SPX"), "Equity Index");
    equityFuture.setName("S&P 500 FUTURE Dec12");
    equityFuture.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "SPZ2"));
    storeFinancialSecurity(equityFuture);
    addPosition(portfolioNode, equityFuture, BigDecimal.ONE);
    
    rootNode.addChildNode(portfolioNode);
  }

  private void addBondNode(ManageablePortfolioNode rootNode) {
    
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode("Bonds");
   
    final GovernmentBondSecurity bond1 = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", 
        Currency.USD, SimpleYieldConvention.US_STREET, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 12, 15, 16, 0), TimeZone.UTC)), "FIXED", 2.625, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 
        ZonedDateTime.of(LocalDateTime.of(2009, 5, 30, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 5, 28, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2009, 12, 31, 11, 0), TimeZone.UTC), 
        99.651404, 3.8075E10, 100.0, 100.0, 100.0, 100.0);
    bond1.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "US912828KY53"));
    bond1.setName("T 2 5/8 06/30/14");
    storeFinancialSecurity(bond1);
    addPosition(portfolioNode, bond1, BigDecimal.valueOf(2120));
    
    final GovernmentBondSecurity bond2 = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", 
        Currency.USD, SimpleYieldConvention.US_STREET, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2015, 8, 31, 18, 0), TimeZone.UTC)), "FIXED", 1.25, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 
        ZonedDateTime.of(LocalDateTime.of(2010, 8, 31, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 2, 14, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 2, 28, 11, 0), TimeZone.UTC), 
        99.402797, 3.6881E10, 100.0, 100.0, 100.0, 100.0);
    bond2.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "US912828NV87"));
    bond2.setName("T 1 1/4 08/31/15");
    storeFinancialSecurity(bond2);
    addPosition(portfolioNode, bond2, BigDecimal.valueOf(3940));
    
    final GovernmentBondSecurity bond3 = new GovernmentBondSecurity("TSY 8% 2021", "Sovereign", "GB", "UK GILT STOCK", 
        Currency.GBP, SimpleYieldConvention.UK_BUMP_DMO_METHOD, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2021, 6, 7, 18, 0), TimeZone.UTC)), "FIXED", 8.0, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"), 
        ZonedDateTime.of(LocalDateTime.of(1996, 2, 29, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 1, 28, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(1996, 6, 7, 12, 0), TimeZone.UTC), 
        99.0625, 2.2686E10, 0.01, 0.01, 100.0, 100.0);
    bond3.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GB0009997999"));
    bond3.setName("UKT 8 06/07/21");
    bond3.setAnnouncementDate(ZonedDateTime.of(LocalDateTime.of(1996, 2, 20, 11, 0), TimeZone.UTC));
    storeFinancialSecurity(bond3);
    addPosition(portfolioNode, bond3, BigDecimal.valueOf(4690));
    
    final List<BondFutureDeliverable> bondFutureDelivarables = new ArrayList<BondFutureDeliverable>();
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FF0")), 0.9221));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FJ2")), 1.0132));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FA1")), 1.037));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FE3")), 0.9485));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FB9")), 1.0125));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FM5")), 1.0273));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FG8")), 0.9213));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810PT9")), 0.8398));
    
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FP8")), 0.9301));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GV912810FT0")), 0.8113));
    
    final BondFutureSecurity bond4 = new BondFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 3, 21, 20, 0), TimeZone.UTC)), 
        "XCBT", "XCBT", Currency.USD, 1000.0, bondFutureDelivarables, ZonedDateTime.of(LocalDateTime.of(2012, 3, 1, 0, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2012, 3, 1, 0, 0), TimeZone.UTC), "Bond");
    bond4.setName("US LONG BOND(CBT) Mar12");

    bond4.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USH12"));
    storeFinancialSecurity(bond4);
    addPosition(portfolioNode, bond4, BigDecimal.valueOf(10));
    
    rootNode.addChildNode(portfolioNode);
  }

  private void addPosition(final ManageablePortfolioNode portfolioNode, final FinancialSecurity security, final BigDecimal quantity) {
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    ManageablePosition position = new ManageablePosition(quantity, security.getExternalIdBundle());
    PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
    portfolioNode.addPosition(addedDoc.getUniqueId());
  }

  private Collection<FinancialSecurity> getIRFutureOptions() {
    List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    InterestRateFutureSecurity edu12 = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 9, 17, 20, 0), TimeZone.UTC)), 
        "XCME", "XCME", Currency.USD, 2500.0, USDLIBOR3M, "Interest Rate");
    edu12.setName("90DAY EURO$ FUTR Sep12");
    edu12.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDU12"));
    storeFinancialSecurity(edu12);
    
    AmericanExerciseType exerciseType = new AmericanExerciseType();
    IRFutureOptionSecurity optionSec1 = new IRFutureOptionSecurity("CME", new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 9, 17, 0, 0), TimeZone.UTC)), 
        exerciseType, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDU12"), 6.25, false, Currency.USD, 98.0, OptionType.PUT);
    optionSec1.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDU2P98"));
    optionSec1.setName("EDU2P 2012-09-17 P 98.0");
    storeFinancialSecurity(optionSec1);
    securities.add(optionSec1);
    
    InterestRateFutureSecurity edz12 = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 12, 17, 20, 0), TimeZone.UTC)), 
        "XCME", "XCME", Currency.USD, 2500.0, USDLIBOR3M, "Interest Rate");
    edz12.setName("90DAY EURO$ FUTR Dec12");
    edz12.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDZ12"));
    storeFinancialSecurity(edz12);
    
    IRFutureOptionSecurity optionSec2 = new IRFutureOptionSecurity("CME", new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 12, 17, 0, 0), TimeZone.UTC)), 
        exerciseType, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDZ12"), 6.25, false, Currency.USD, 99.0, OptionType.CALL);
    optionSec2.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDU2P98"));
    optionSec2.setName("EDZ2C 2012-12-17 C 99.0");
    storeFinancialSecurity(optionSec2);
    securities.add(optionSec2);
    
    return securities;
  }

  private void storeFinancialSecurity(final FinancialSecurity security) {
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    SecurityDocument toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(security);
    securityMaster.add(toAddDoc);
  }

  private void addPortfolioNode(final ManageablePortfolioNode rootNode, final Collection<FinancialSecurity> finSecurities, final String portfolioNodeName, BigDecimal quantity) {
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode(portfolioNodeName);
    for (final FinancialSecurity security : finSecurities) {
      storeFinancialSecurity(security);
      ManageablePosition position = new ManageablePosition(quantity, security.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      portfolioNode.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(portfolioNode);
  }
  
  private List<FinancialSecurity> getCMCapFloor() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    final CapFloorSecurity cmsCap = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 4, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2016, 4, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P10Y"), 0.03, SimpleFrequency.ANNUAL, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("Actual/360"), false, true, false);
    cmsCap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    cmsCap.setName(getCapFloorName(cmsCap));
    securities.add(cmsCap);
    
    final CapFloorSecurity cmsFloor = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 9, 9, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2016, 9, 9, 1, 0), TimeZone.UTC), 1.5E7, 
        ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P10Y"), 0.01, SimpleFrequency.SEMI_ANNUAL, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("Actual/360"), false, false, false);
    cmsFloor.setName(getCapFloorName(cmsFloor));
    cmsFloor.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(cmsFloor);
    return securities;
  }

  private String getCapFloorName(final CapFloorSecurity capFloorSec) {
    return String.format("%s %s @ %.2f [%s-%s] %s, %s %s %s", capFloorSec.isIbor() ? "IBOR" : "CMS", capFloorSec.isCap() ? "cap " : "floor ", 
        capFloorSec.getStrike(), capFloorSec.getStartDate().toLocalDate(), capFloorSec.getMaturityDate().toLocalDate(), 
        capFloorSec.getFrequency().getConventionName(), capFloorSec.getCurrency().getCode(),
        PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(capFloorSec.getNotional()), capFloorSec.isPayer() ? " Short" : " Long");
  }
  
  private List<FinancialSecurity> getIborSwaps() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 40, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 ExternalSchemes.countryRegionId(Country.of("US")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.USD, 15000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    USDLIBOR3M, 
                                    FloatingRateType.IBOR));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 5% fixed vs 3m Libor, start=" + swap1.getEffectiveDate().toLocalDate() + ", maturity=" + swap1.getMaturityDate().toLocalDate() + ", notional=USD 15MM");
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 30, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 ExternalSchemes.countryRegionId(Country.of("DE")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.EUR, 20000000), 
                                 true, 
                                 0.04), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("DE")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.EUR, 20000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDLIBORP6M"), 
                                    FloatingRateType.IBOR));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 4% fixed vs 6m Euribor, start=" + swap2.getEffectiveDate().toLocalDate() + ", maturity=" + swap2.getMaturityDate().toLocalDate() + ", notional=EUR 20MM");
    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 13, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 ExternalSchemes.countryRegionId(Country.of("GB")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.GBP, 15000000), 
                                 true, 
                                 0.03), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("GB")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.GBP, 15000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GBPLIBORP6M"), 
                                    FloatingRateType.IBOR));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay 3% fixed vs 6m Libor, start=" + swap3.getEffectiveDate().toLocalDate() + ", maturity=" + swap3.getMaturityDate().toLocalDate() + ", notional=GBP 15MM");
    final SwapSecurity swap4 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 25, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 ExternalSchemes.countryRegionId(Country.of("JP")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.JPY, 100000000), 
                                 true, 
                                 0.02), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("JP")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.JPY, 100000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "JPYLIBORP6M"), 
                                    FloatingRateType.IBOR));
    swap4.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap4.setName("Swap: pay 2% fixed vs 6m Libor, start=" + swap4.getEffectiveDate().toLocalDate() + ", maturity=" + swap4.getMaturityDate().toLocalDate() + ", notional=JPY 100MM");
    final SwapSecurity swap5 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 40, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 ExternalSchemes.countryRegionId(Country.of("CH")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.CHF, 5000000), 
                                 true, 
                                 0.07), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("CH")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.CHF, 5000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "CHFLIBORP6M"), 
                                    FloatingRateType.IBOR));
    swap5.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap5.setName("Swap: pay 7% fixed vs 6m Libor, start=" + swap5.getEffectiveDate().toLocalDate() + ", maturity=" + swap5.getMaturityDate().toLocalDate() + ", notional=CHF 50MM");
//    final SwapSecurity swap6 = new SwapSecurity(
//        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2040, 5, 1, 11, 0), TimeZone.UTC), 
//        "Cpty", 
//        new FixedInterestRateLeg(DAY_COUNT, 
//                                 SimpleFrequency.SEMI_ANNUAL, 
//                                 RegionUtils.countryRegionId(Country.of("CA")), 
//                                 BUSINESS_DAY, 
//                                 new InterestRateNotional(Currency.CAD, 20000000), 
//                                 true, 
//                                 0.05), 
//        new FloatingInterestRateLeg(DAY_COUNT, 
//                                    SimpleFrequency.QUARTERLY, 
//                                    RegionUtils.countryRegionId(Country.of("CA")), 
//                                    BUSINESS_DAY, 
//                                    new InterestRateNotional(Currency.CAD, 20000000), 
//                                    true, 
//                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "CDOR06 RBC Index"), 
//                                    FloatingRateType.IBOR));
//    swap6.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    swap6.setName("Swap: pay 5% fixed vs 6m CDOR, start=1/5/2010, maturity=1/5/2040, notional=CAD 20MM");
//    final SwapSecurity swap7 = new SwapSecurity(
//        ZonedDateTime.of(LocalDateTime.of(2005, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2005, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2025, 5, 1, 11, 0), TimeZone.UTC), 
//        "Cpty", 
//        new FixedInterestRateLeg(DAY_COUNT, 
//                                 SimpleFrequency.SEMI_ANNUAL, 
//                                 RegionUtils.countryRegionId(Country.of("AU")), 
//                                 BUSINESS_DAY, 
//                                 new InterestRateNotional(Currency.AUD, 25000000), 
//                                 true, 
//                                 0.05), 
//        new FloatingInterestRateLeg(DAY_COUNT, 
//                                    SimpleFrequency.QUARTERLY, 
//                                    RegionUtils.countryRegionId(Country.of("AU")), 
//                                    BUSINESS_DAY, 
//                                    new InterestRateNotional(Currency.AUD, 25000000), 
//                                    true, 
//                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "AU0006M Index"), 
//                                    FloatingRateType.IBOR));
//    swap7.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    swap7.setName("Swap: pay 5% fixed vs 6m Libor, start=1/5/2005, maturity=1/5/2025, notional=AUD 25MM");
    /*
    final SwapSecurity swap8 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2030, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("NZ")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.NZD, 55000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("NZ")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.NZD, 55000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "NZDLIBORP6M"), 
                                    FloatingRateType.IBOR));
    swap8.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap8.setName("Swap: pay 5% fixed vs 6m Libor, start=1/5/2010, maturity=1/5/2030, notional=NZD 55MM");
    */
    final SwapSecurity swap9 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 20, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 ExternalSchemes.countryRegionId(Country.of("DK")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.DKK, 90000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("DK")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.DKK, 90000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "DKKLIBORP6M"), 
                                    FloatingRateType.IBOR));
    swap9.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap9.setName("Swap: pay 5% fixed vs 6m Cibor, start=" + swap9.getEffectiveDate().toLocalDate() + ", maturity=" + swap9.getMaturityDate().toLocalDate() + ", notional=DKK 90MM");
    securities.add(swap1);
    securities.add(swap2);
    securities.add(swap3);
    securities.add(swap4);
    securities.add(swap5);
//    securities.add(swap6);
//    securities.add(swap7);
    /*securities.add(swap8);*/ 
    securities.add(swap9);
    return securities;
  }
  
  private Collection<FinancialSecurity> getCMSwaps() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final int year = TODAY.getYear();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 1, 12, 20, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 1, 12, 20, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 5, 12, 20, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.QUARTERLY, 
                                 ExternalSchemes.countryRegionId(Country.of("US")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.USD, 21000000), 
                                 true, 
                                 0.035), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 21000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P10Y"), 
                                    FloatingRateType.CMS));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("CMSwap: pay 5Y fixed @ 3.5% vs USDISDA10P10Y, start=" + swap1.getEffectiveDate().toLocalDate() + ", maturity=" + swap1.getMaturityDate().toLocalDate() + ", notional=USD 21MM");
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 1, 4, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 1, 4, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 7, 4, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 123000000), 
                                    true, 
                                    ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P1Y"), 
                                    FloatingRateType.CMS),
        new FloatingSpreadIRLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    ExternalSchemes.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 123000000), 
                                    true, 
                                    USDLIBOR3M, 
                                    FloatingRateType.IBOR, 0.005));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("CMSwap: pay USDISDA10P1Y vs 3m Libor, start=" + swap2.getEffectiveDate().toLocalDate() + ", maturity=" + swap2.getMaturityDate().toLocalDate() + ", notional=USD 123MM");
    securities.add(swap1);
    securities.add(swap2);
    return securities;
  }
  
  private Collection<FinancialSecurity> getSwaptions() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final int year = TODAY.getYear();
    final EuropeanExerciseType europeanExerciseType = new EuropeanExerciseType();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 1, 6, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 1, 6, 1, 1, 0), TimeZone.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 11, 6, 1, 1, 0), TimeZone.UTC), 
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY, 
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 1.0E7), 
            false, 
            USDLIBOR3M, 
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("30U/360"), 
            SimpleFrequency.SEMI_ANNUAL, 
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 1.0E7), 
            false, 
            0.04));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 3m Libor vs 4% fixed, start=" + swap1.getEffectiveDate().toLocalDate() + ", maturity=" + swap1.getMaturityDate().toLocalDate() + ", notional=USD 10MM");
    storeFinancialSecurity(swap1);
    final SwaptionSecurity swaption1 = new SwaptionSecurity(false, swap1.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)),
        true, new Expiry(ZonedDateTime.of(LocalDateTime.of(year + 1, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    swaption1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption1.setName("Vanilla swaption, 1Y x 10Y, USD 10,000,000 @ 4%");
    securities.add(swaption1);
    
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 2, 6, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 2, 6, 1, 1, 0), TimeZone.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 4, 6, 1, 1, 0), TimeZone.UTC), 
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY, 
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 3000000.0), 
            false, 
            USDLIBOR3M, 
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("30U/360"), 
            SimpleFrequency.SEMI_ANNUAL, 
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 3000000.0), 
            false, 
            0.01));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 3m Libor vs 1% fixed, start=" + swap2.getEffectiveDate().toLocalDate() + ", maturity=" + swap2.getMaturityDate().toLocalDate() + ", notional=USD 3MM"); 
    storeFinancialSecurity(swap2);
    final SwaptionSecurity swaption2 = new SwaptionSecurity(false, swap2.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)), 
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(year + 2, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    swaption2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption2.setName("Vanilla swaption, 2Y x 2Y, USD 3,000,000 @ 1%");
    securities.add(swaption2);

    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 5, 6, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 5, 6, 1, 1, 0), TimeZone.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 20, 6, 1, 1, 0), TimeZone.UTC), 
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY, 
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 6000000.0), 
            false, 
            USDLIBOR3M, 
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("30U/360"), 
            SimpleFrequency.SEMI_ANNUAL, 
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 6000000.0), 
            false, 
            0.035));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay 3m Libor vs 3.5% fixed, start=" + swap3.getEffectiveDate().toLocalDate() + ", maturity=" + swap3.getMaturityDate().toLocalDate() + ", notional=USD 6MM");
    storeFinancialSecurity(swap3);
    final SwaptionSecurity swaption3 = new SwaptionSecurity(false, swap3.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)), 
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(year + 5, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    swaption3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption3.setName("Vanilla swaption, 5Y x 15Y, USD 6,000,000 @ 3.5%");
    securities.add(swaption3);
    return securities;
  }
  
  private Collection<FinancialSecurity> getIborCapFloor() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    int year = TODAY.getYear();
    final CapFloorSecurity sec1 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(year + 1, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 3, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        USDLIBOR3M, 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, true, true);
    sec1.setName(getCapFloorName(sec1));
    sec1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec1);
    
    final CapFloorSecurity sec2 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(year + 1, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(year + 3, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        USDLIBOR3M, 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, false, true);
    sec2.setName(getCapFloorName(sec2));
    sec2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec2);
    return securities;
  }

  private static Collection<FinancialSecurity> getSimpleFixedIncome() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final FRASecurity fra = new FRASecurity(Currency.USD,
      ExternalSchemes.countryRegionId(Country.of("US")),
      ZonedDateTime.of(LocalDateTime.of(2012, 1, 14, 11, 0), TimeZone.UTC),
      ZonedDateTime.of(LocalDateTime.of(2012, 4, 14, 11, 0), TimeZone.UTC),
      0.01,
      15000000,
      USDLIBOR3M,
      ZonedDateTime.of(LocalDateTime.of(2011, 1, 14, 11, 0), TimeZone.UTC));
    fra.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fra.setName("FRA: pay 1% vs 3m Libor, start=1/14/2012, maturity=4/14/2012, notional=USD 15MM");
    final InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 12, 15, 16, 0), TimeZone.UTC)),
      "CME",
      "CME",
      Currency.USD,
      1000,
      USDLIBOR3M,      
      "Interest Rate");
    irFuture.setName("90DAY EURO$ FUTR Jun13");
    irFuture.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EDZ13"));
    securities.add(fra);
    securities.add(irFuture);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getSimpleFX() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    int year = LocalDate.now().getYear();
    final FXForwardSecurity fxForward1 = new FXForwardSecurity(Currency.USD, 1000000, Currency.EUR, 1000000,
                                                               ZonedDateTime.of(LocalDateTime.of(2013, 2, 1, 11, 0), TimeZone.UTC), 
                                                               ExternalSchemes.countryRegionId(Country.of("US")));
    fxForward1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxForward1.setName("FX forward, pay USD 1000000, receive EUR 1000000, maturity=1/2/2013");

//    final FXForwardSecurity fxForward2 = new FXForwardSecurity(Currency.CAD, 800000, Currency.JPY, 80000000, 
//                                                               ZonedDateTime.of(LocalDateTime.of(2013, 2, 1, 11, 0), TimeZone.UTC), 
//                                                               RegionUtils.countryRegionId(Country.of("US")));
//    fxForward2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    fxForward2.setName("FX forward, pay CAD 800000, receive JPY 80000000, maturity=1/2/2013");
    
    final FXForwardSecurity fxForward3 = new FXForwardSecurity(Currency.CHF, 2000000, Currency.EUR, 1000000,
                                                               ZonedDateTime.of(LocalDateTime.of(2013, 2, 1, 11, 0), TimeZone.UTC), 
                                                               ExternalSchemes.countryRegionId(Country.of("US")));
    fxForward3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxForward3.setName("FX forward, pay CHF 2000000, receive EUR 1000000, maturity=1/2/2013");
    securities.add(fxForward1);
//    securities.add(fxForward2);
    securities.add(fxForward3);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getFXOptions() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final FXOptionSecurity vanilla1 = new FXOptionSecurity(Currency.USD, 
                                                           Currency.EUR, 
                                                           1000000, 
                                                           1000000, 
                                                           new Expiry(ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), TimeZone.UTC)), 
                                                           ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), TimeZone.UTC), 
                                                           true, 
                                                           new EuropeanExerciseType());
    vanilla1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    vanilla1.setName("FX vanilla option, put USD 1000000, receive EUR 1000000, maturity=" + vanilla1.getSettlementDate().toLocalDate());
    final FXOptionSecurity vanilla2 = new FXOptionSecurity(Currency.EUR, 
                                                           Currency.USD, 
                                                           1500000, 
                                                           1000000, 
                                                           new Expiry(ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 2, 1, 6, 11, 0), TimeZone.UTC)), 
                                                           ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 2, 1, 6, 11, 0), TimeZone.UTC), 
                                                           true, 
                                                           new EuropeanExerciseType());
    vanilla2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    vanilla2.setName("FX vanilla option, put EUR 1500000, receive USD 1000000, maturity=" + vanilla2.getSettlementDate().toLocalDate());
    final FXBarrierOptionSecurity barrier1 = new FXBarrierOptionSecurity(Currency.USD, 
                                                                         Currency.EUR, 
                                                                         1000000, 
                                                                         1000000, 
                                                                         new Expiry(ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), TimeZone.UTC)), 
                                                                         ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), TimeZone.UTC), 
                                                                         BarrierType.UP, 
                                                                         BarrierDirection.KNOCK_OUT, 
                                                                         MonitoringType.CONTINUOUS, 
                                                                         SamplingFrequency.DAILY_CLOSE, 
                                                                         1.5, 
                                                                         true);
    barrier1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    barrier1.setName("FX single barrier up knock-out option, put USD 1000000, receive EUR 1000000, maturity=" + barrier1.getSettlementDate().toLocalDate() + ", barrier=1.5 EUR/USD");
//    final FXBarrierOptionSecurity barrier2 = new FXBarrierOptionSecurity(Currency.EUR, 
//                                                                         Currency.USD, 
//                                                                         1500000, 
//                                                                         1000000, 
//                                                                         new Expiry(ZonedDateTime.of(LocalDateTime.of(2015, 1, 6, 11, 0), TimeZone.UTC)), 
//                                                                         ZonedDateTime.of(LocalDateTime.of(2015, 1, 6, 11, 0), TimeZone.UTC), 
//                                                                         BarrierType.DOWN, 
//                                                                         BarrierDirection.KNOCK_OUT, 
//                                                                         MonitoringType.CONTINUOUS, 
//                                                                         SamplingFrequency.DAILY_CLOSE, 
//                                                                         0.2, 
//                                                                         true);
//    barrier2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    barrier2.setName("FX single barrier down knock-out option, put EUR 1500000, receive USD 1000000, maturity=1/6/2015, barrier=0.2 USD/EUR");    
    securities.add(vanilla1);
    securities.add(vanilla2);
    securities.add(barrier1);
//    securities.add(barrier2);
    return securities;
  }

  @Override
  protected void doRun() {
    persistToPortfolio();
  }

}
