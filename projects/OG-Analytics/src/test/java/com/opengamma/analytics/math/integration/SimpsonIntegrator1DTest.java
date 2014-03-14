/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;



public class SimpsonIntegrator1DTest extends Integrator1DTestCase {
  private static final Integrator1D<Double, Double> INTEGRATOR = new SimpsonIntegrator1D();

  @Override
  public Integrator1D<Double, Double> getIntegrator() {
    return INTEGRATOR;
  }

}
