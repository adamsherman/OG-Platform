/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class LogPayoffWithDividendsTest {

  private static final PDE1DCoefficientsProvider PDE_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITIAL_COND_PROVIDER = new InitialConditionsProvider();

  private static final Interpolator1D INTEPOLATOR1D = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;

  private static final double EXPIRY = 1.5;
  private static final double DIVIDEND_DATE = 0.85;
  private static final double ALPHA = 6.0;
  private static final double BETA = 0.04;
  private static final double PURE_VOL = 0.5;
  private static final double VOL = 0.4;
  private static final double SPOT = 100.0;
  private static final double DRIFT = 0.1;//0.1;
  private static final LocalVolatilitySurfaceMoneyness PURE_LOCAL_VOL_FLAT;
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL;
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL_SPECIAL;
  private static final LocalVolatilitySurfaceStrike LOCAL_VOL_FLAT;
  private static final LocalVolatilitySurfaceMoneyness PURE_LOCAL_VOL;

  private static final Function1D<Double, Double> R;
  private static final Function1D<Double, Double> D;
  private static final Function1D<Double, Double> F;

  private static final Function1D<Double, Double> PURE_LOG_PAY_OFF;

  static {
    R = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        double prod = Math.exp(t * DRIFT);
        if (t >= DIVIDEND_DATE) {
          prod *= (1 - BETA);
        }
        return prod;
      }
    };

    D = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        final double r_t = R.evaluate(t);
        double sum = 0.0;
        if (DIVIDEND_DATE > t) {
          sum += ALPHA / R.evaluate(DIVIDEND_DATE);
        }
        return sum * r_t;
      }
    };

    F = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        final double r_t = R.evaluate(t);
        double sum = 0.0;
        if (DIVIDEND_DATE <= t) {
          sum += ALPHA / R.evaluate(DIVIDEND_DATE);
        }
        return r_t * (SPOT - sum);
      }
    };

    PURE_LOG_PAY_OFF = new Function1D<Double, Double>() {
      final double fT = F.evaluate(EXPIRY);
      final double dT = D.evaluate(EXPIRY);

      @Override
      public Double evaluate(Double x) {
        final double s = (fT - dT) * Math.exp(x) + dT;
        return Math.log(s);
      }
    };

    Function<Double, Double> localVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        double t = ts[0];
        double s = ts[1];
        double d = D.evaluate(t);
        if (s < d) {
          return 0.0;
        }
        return PURE_VOL * (s - d) / s;
      }
    };

    LOCAL_VOL = new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(localVol));

    Function<Double, Double> localVolSpecial = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tf) {
        double t = tf[0];
        double f = tf[1];
        double rtT = R.evaluate(t);
        double dtT = D.evaluate(t);
        double ftT = F.evaluate(t);
        //        if (f < d) {
        //          return 0.0;
        //        }
        double x = f / rtT / (ftT - dtT);
        return PURE_LOCAL_VOL.getVolatility(t, x);
      }
    };

    LOCAL_VOL_SPECIAL = new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(localVolSpecial));

    Function<Double, Double> pureLocalVol = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        double t = tx[0];
        double x = tx[1];
        double f = F.evaluate(t);
        double d = D.evaluate(t);
        return VOL * ((f - d) * x + d) / (f - d) / x;
      }
    };

    PURE_LOCAL_VOL = new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(pureLocalVol), new ForwardCurve(1.0));

    PURE_LOCAL_VOL_FLAT = new LocalVolatilitySurfaceMoneyness(ConstantDoublesSurface.from(PURE_VOL), new ForwardCurve(1.0));
    LOCAL_VOL_FLAT = new LocalVolatilitySurfaceStrike(ConstantDoublesSurface.from(VOL));

  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the 'pure' stock price 
   * - this avoids having jumps conditions in the PDE. The pure local volatility surface is flat. 
   */
  @Test
  public void backwardsLogPureSpotPDEtest() {
    final double fT = F.evaluate(EXPIRY);
    final double lnFT = Math.log(fT);
    double val = logContactPriceFromPureSpot(PURE_LOCAL_VOL_FLAT);
    assertEquals(PURE_VOL, Math.sqrt(-2 * (val - lnFT) / EXPIRY), 1e-6);
    //   System.out.println(val + "\t" + Math.sqrt(-2 * (val - lnFT) / EXPIRY));
  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the real stock price 
   * - this requires having jumps conditions in the PDE. The local volatility surface is derived from the flat pure local volatility surface.
   */
  @Test
  public void backwardsLogSpotPDEtest() {
    final double fT = F.evaluate(EXPIRY);
    final double lnFT = Math.log(fT);
    double val = logContractPriceFromSpotPDE(LOCAL_VOL);
    assertEquals(PURE_VOL, Math.sqrt(-2 * (val - lnFT) / EXPIRY), 1e-4);
    //   System.out.println(val + "\t" + Math.sqrt(-2 * (val - lnFT) / EXPIRY));
  }

  /**
   * Price the log-contact using the PDE in spot (with the jump conditions) with a flat local volatility surface, and the PDE in pure spot using the pure local volatility 
   * surface derived from the flat surface. They MUST give the same answer
   */
  @Test
  public void backwardsPDETest() {
    final double fT = F.evaluate(EXPIRY);
    final double lnFT = Math.log(fT);
    double val1 = logContractPriceFromSpotPDE(LOCAL_VOL_FLAT);
    double val2 = logContactPriceFromPureSpot(PURE_LOCAL_VOL);
    //convert to realised vol 
    double vol1 = Math.sqrt(-2 * (val1 - lnFT) / EXPIRY);
    double vol2 = Math.sqrt(-2 * (val2 - lnFT) / EXPIRY);
    assertEquals(vol1, vol2, 1e-3);
    //   System.out.println(vol1 + "\t" + vol2);
  }

  /**
   * Check the the log-contract is correctly prices using a backwards PDE expressed in terms of (the log of) the forward F(t,T)
   * - this requires NO jumps conditions in the PDE 
   */
  @Test
  public void backwardsDebugPDEtest() {
    final double fT = F.evaluate(EXPIRY);
    final double lnFT = Math.log(fT);

    final Function1D<Double, Double> payoff = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double y) {
        return y - lnFT;
      }
    };

    // ZZConvectionDiffusionPDEDataBundle pdeBundle1 = getBackwardsPDEDataBundle(EXPIRY, LOCAL_VOL, payoff);
    // ConvectionDiffusionPDE1DCoefficients pde = PDE_PROVIDER.getLogBackwardsLocalVol(FORWARD_CURVE, EXPIRY, LOCAL_VOL);
    ConvectionDiffusionPDE1DCoefficients pde = PDE_PROVIDER.getLogBackwardsLocalVol(0.0, 0.0, EXPIRY, LOCAL_VOL_SPECIAL);

    double theta = 0.5;
    double range = Math.log(5);
    double yL = lnFT - range;
    double yH = lnFT + range;
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1.0, yL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final double[] sNodes = grid.getSpaceNodes();

    //run the PDE solver backward to the dividend date 
    // PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db1 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initialCon, lower1, upper1, grid1);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db1 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, payoff, lower, upper, grid);
    final PDETerminalResults1D res = (PDETerminalResults1D) solver.solve(db1);

    final Interpolator1DDataBundle interpolDB = INTEPOLATOR1D.getDataBundle(sNodes, res.getFinalTimePrices());

    double val = INTEPOLATOR1D.interpolate(interpolDB, lnFT);
    assertEquals(0.41491529, Math.sqrt(-2 * (val) / EXPIRY), 5e-4); //Number from backwardsPDETest
    //   System.out.println(val + "\t" + Math.sqrt(-2 * val / EXPIRY));
  }

  private double logContactPriceFromPureSpot(final LocalVolatilitySurfaceMoneyness lv) {
    final double fT = F.evaluate(EXPIRY);
    final double dT = D.evaluate(EXPIRY);
    final double dStar = dT / (fT - dT);

    ConvectionDiffusionPDE1DCoefficients pde = PDE_PROVIDER.getLogBackwardsLocalVol(EXPIRY, lv);

    double theta = 0.5;
    double yL = -0.5;
    double yH = 0.5;
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1 / (1 + dStar * Math.exp(-yL)), yL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, PURE_LOG_PAY_OFF, lower, upper, grid);
    final PDEResults1D res = solver.solve(db);

    final int n = res.getNumberSpaceNodes();

    double val = res.getFunctionValue(n / 2);
    return val;
  }

  /**
   * Prices a log-contract for a given local volatility surface by backwards solving the PDE expressed in terms of (the log of) the real stock price 
   * - this requires having jumps conditions in the PDE 
   * @param lv Local volatility 
   * @return Forward (non-discounted) price of log-contact 
   */
  private double logContractPriceFromSpotPDE(final LocalVolatilitySurfaceStrike lv) {

    //Set up the PDE to give the forward (non-discounted) option price 
    ConvectionDiffusionPDE1DCoefficients pde = PDE_PROVIDER.getLogBackwardsLocalVol(0.0, -DRIFT, EXPIRY, lv);
    Function1D<Double, Double> initialCon = INITIAL_COND_PROVIDER.getLogContractPayoffInLogCoordinate();

    double theta = 0.5;
    double yL = Math.log(SPOT / 6);
    double yH = Math.log(6 * SPOT);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower1 = new NeumannBoundaryCondition(1.0, yL, true);
    BoundaryCondition upper1 = new NeumannBoundaryCondition(1.0, yH, false);

    final MeshingFunction timeMesh1 = new ExponentialMeshing(0, EXPIRY - DIVIDEND_DATE - 1e-6, 50, 0.0);
    final MeshingFunction timeMesh2 = new ExponentialMeshing(EXPIRY - DIVIDEND_DATE + 1e-6, EXPIRY, 50, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yL, yH, 101, 0.0);

    final PDEGrid1D grid1 = new PDEGrid1D(timeMesh1, spaceMesh);
    final double[] sNodes1 = grid1.getSpaceNodes();

    //run the PDE solver backward to the dividend date 
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db1 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initialCon, lower1, upper1, grid1);
    final PDETerminalResults1D res1 = (PDETerminalResults1D) solver.solve(db1);

    //Map the spot nodes after (in calendar time) the dividend payment to nodes before
    final int nSNodes = sNodes1.length;
    final double[] sNodes2 = new double[nSNodes];
    final double lnBeta = Math.log(1 - BETA);
    for (int i = 0; i < nSNodes; i++) {
      double temp = sNodes1[i];
      if (temp < 0) {
        sNodes2[i] = Math.log(Math.exp(temp) + ALPHA) - lnBeta;
      }
      else {
        sNodes2[i] = temp + Math.log(1 + ALPHA * Math.exp(-temp)) - lnBeta;
      }
    }

    PDEGrid1D grid2 = new PDEGrid1D(timeMesh2.getPoints(), sNodes2);
    BoundaryCondition lower2 = new NeumannBoundaryCondition(1.0, sNodes2[0], true);
    BoundaryCondition upper2 = new NeumannBoundaryCondition(1.0, sNodes2[nSNodes - 1], false);

    //run the PDE solver backward from the dividend date to zero 
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db2 = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, res1.getFinalTimePrices(), lower2, upper2, grid2);
    final PDETerminalResults1D res2 = (PDETerminalResults1D) solver.solve(db2);

    final Interpolator1DDataBundle interpolDB2 = INTEPOLATOR1D.getDataBundle(sNodes2, res2.getFinalTimePrices());
    double val2 = INTEPOLATOR1D.interpolate(interpolDB2, Math.log(SPOT));
    return val2;
  }

}
