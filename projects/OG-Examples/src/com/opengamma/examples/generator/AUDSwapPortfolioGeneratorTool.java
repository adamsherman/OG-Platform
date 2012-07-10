/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class AUDSwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  private static final ZonedDateTime TRADE_DATE = ZonedDateTime.of(LocalDate.of(2013, 9, 5), LocalTime.MIDNIGHT, TimeZone.UTC);
  private static final ZonedDateTime MATURITY = ZonedDateTime.of(LocalDate.of(2015, 9, 5), LocalTime.MIDNIGHT, TimeZone.UTC);
  private static final String COUNTERPARTY = "Cpty";
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("Act/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Act/360");
  private static final Frequency QUARTERLY = PeriodFrequency.QUARTERLY;
  private static final Frequency SEMI_ANNUAL = PeriodFrequency.SEMI_ANNUAL;
  private static final ExternalId REGION =  ExternalSchemes.financialRegionId("AU");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final InterestRateNotional NOTIONAL = new InterestRateNotional(Currency.AUD, 15000000);
  private static final ExternalId AUD_LIBOR_3M = ExternalSchemes.syntheticSecurityId("AUDLIBORP3M");
  private static final ExternalId AUD_LIBOR_6M = ExternalSchemes.syntheticSecurityId("AUDLIBORP6M");
  private static final SwapSecurity[] SWAPS = new SwapSecurity[4];

  static {
    final FloatingInterestRateLeg payLeg1 = new FloatingInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg1 = new FixedInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, 0.04);
    final SwapSecurity swap1 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg1, receiveLeg1);
    swap1.setName("Swap AUD Bank Bill 3M");
    final FloatingInterestRateLeg payLeg2 = new FloatingInterestRateLeg(ACT_365, SEMI_ANNUAL, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_6M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg2 = new FixedInterestRateLeg(ACT_365, SEMI_ANNUAL, REGION, FOLLOWING, NOTIONAL, true, 0.04);
    final SwapSecurity swap2 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg2, receiveLeg2);
    swap2.setName("Swap AUD Bank Bill 6M");
    final FloatingInterestRateLeg payLeg3 = new FloatingInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg3 = new FixedInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, 0.0365);
    final SwapSecurity swap3 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg3, receiveLeg3);
    swap3.setName("Swap: receive 3.65% fixed ACT/365 vs 3m Bank Bill");
    final FloatingInterestRateLeg payLeg4 = new FloatingInterestRateLeg(ACT_360, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, AUD_LIBOR_3M, FloatingRateType.IBOR);
    final FixedInterestRateLeg receiveLeg4 = new FixedInterestRateLeg(ACT_365, QUARTERLY, REGION, FOLLOWING, NOTIONAL, true, 0.036);
    final SwapSecurity swap4 = new SwapSecurity(TRADE_DATE, TRADE_DATE, MATURITY, COUNTERPARTY, payLeg4, receiveLeg4);
    swap4.setName("Swap: receive 3.60% fixed ACT/365 vs 3m Bank Bill");
    SWAPS[0] = swap1;
    SWAPS[1] = swap2;
    SWAPS[2] = swap3;
    SWAPS[3] = swap4;
  }
  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<SwapSecurity> securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<SwapSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("AUD Swaps"), positions, 4);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<SwapSecurity> securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<SwapSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, 4);
  }

  private SecurityGenerator<SwapSecurity> createSwapSecurityGenerator() {
    final SecurityGenerator<SwapSecurity> securities = new SecurityGenerator<SwapSecurity>() {
      private int _count;

      @Override
      public SwapSecurity createSecurity() {
        if (_count > 3) {
          throw new IllegalStateException("Should not ask for more than 4 securities");
        }
        final SwapSecurity swap = SWAPS[_count++];
        return swap;
      }

    };
    configure(securities);
    return securities;
  }
}
