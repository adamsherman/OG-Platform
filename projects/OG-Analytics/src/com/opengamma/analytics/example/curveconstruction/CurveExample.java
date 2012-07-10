package com.opengamma.analytics.example.curveconstruction;

// @export "imports"
import java.io.PrintStream;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

public class CurveExample {
  // @export "constantDoublesCurveDemo"
  public static void constantDoublesCurveDemo(PrintStream out) {
    Curve curve = new ConstantDoublesCurve(5.0);

    out.println(curve.getYValue(0.0));
    out.println(curve.getYValue(10.0));
    out.println(curve.getYValue(-10.0));
  }

  // @export "nodalDoublesCurveDemo"
  //    public static void nodalDoublesCurveDemo(PrintStream out) {
  //        double[] xdata = { 1.0, 2.0, 3.0 };
  //        double[] ydata = { 2.0, 4.0, 6.0 };
  //        Curve curve = new NodalDoublesCurve(xdata, ydata, true);
  //
  //        out.println(curve.getYValue(1.0));
  //        out.println(curve.getYValue(2.0));
  //        out.println(curve.getYValue(3.0));
  //
  //        try {
  //            out.println("Trying to get y value for an undefined x value...");
  //            curve.getYValue(1.5);
  //        } catch (java.lang.IllegalArgumentException  e) {
  //            out.println("IllegalArgumentException called");
  //        }
  //    }

  // @export "interpolatedDoublesCurveDemo"
  public static void interpolatedDoublesCurveDemo(PrintStream out) {
    double[] xdata = {1.0, 2.0, 3.0};
    double[] ydata = {2.0, 4.0, 6.0};
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    Curve curve = new InterpolatedDoublesCurve(xdata, ydata, interpolator, true);

    out.println(curve.getYValue(1.0));
    out.println(curve.getYValue(2.0));
    out.println(curve.getYValue(3.0));

    out.println(curve.getYValue(1.5));
    try {
      out.println("Trying to get y value for too large an x...");
      curve.getYValue(4.0);
    } catch (java.lang.IllegalArgumentException e) {
      out.println("IllegalArgumentException called");
    }
  }

  // @export "interpolatorExtrapolatorDoublesCurveDemo"
  public static void interpolatorExtrapolatorDoublesCurveDemo(PrintStream out) {
    double[] xdata = {1.0, 2.0, 3.0};
    double[] ydata = {2.0, 4.0, 6.0};

    Interpolator1D interpolator = new LinearInterpolator1D();
    Interpolator1D leftExtrapolator = new LinearExtrapolator1D(interpolator);
    Interpolator1D rightExtrapolator = new LinearExtrapolator1D(interpolator);
    Interpolator1D combined = new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);

    Curve curve = new InterpolatedDoublesCurve(xdata, ydata, combined, true);

    out.println(curve.getYValue(1.0));
    out.println(curve.getYValue(2.0));
    out.println(curve.getYValue(3.0));

    out.println(curve.getYValue(1.5));
    out.println(curve.getYValue(4.0));
  }
}
