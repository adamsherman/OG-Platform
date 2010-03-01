/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class DownhillSimplexMinimizerTest extends MultidimensionalMinimizerTestCase {

  @Test
  public void test() {
    retry(2);
    final MultidimensionalMinimizer MINIMIZER = new DownhillSimplexMinimizer();
    super.testInputs(MINIMIZER);
    super.test(MINIMIZER);
  }

}
