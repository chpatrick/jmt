package jmt.engine.jaba;

// newPoint.java
// 
// Mark F. Hulber
// May 1996
//
//
// newPoint is an extension of the Java Point class.  Additions include 
//    methods for making comparisons between points including relative 
//    direction, magnitude, and angular computations.
//
//

import java.awt.*;

public class newPoint extends Point {
   private java.lang.Math mm;

   public newPoint(int nx, int ny){
      super(nx, ny);
   } 
   public double length() {
      return Math.sqrt(x*x + y*y);
   } 
   public int classify(newPoint p0, newPoint p1) {

     newPoint a = new newPoint(p1.x-p0.x, p1.y-p0.y);
     newPoint b = new newPoint(x - p0.x, y - p0.y);
     
     double sa = a.x * b.y - b.x * a.y;

     if (sa > 0.0)
        return 0;  // LEFT
     if (sa < 0.0)
        return 1;  // RIGHT
     if ((a.x * b.x < 0.0) || (a.y * b.y < 0.0))
        return 2;  // BEHIND
     if (a.length() < b.length())
        return 3;  // BEYOND
     if (p0.equals(this))
        return 4;  // ORIGIN
     if (p1.equals(this))
        return 5;  // DESTINATION
     return 6;     // BETWEEN
  }

  public double polarAngle() {
     
     if ((x == 0.0) && (y == 0.0))
        return -1.0;
     if (x == 0.0)
       return ((y > 0.0) ? 90 : 270);
     double theta = Math.atan((double)y/x);
     theta *= 360/(2*Math.PI);
     if (x > 0.0)
       return ((y >= 0.0) ? theta : 360 + theta);
     else
       return (180+theta);
  }   



  public int polarCmp(newPoint p, newPoint q) {


     newPoint vp = new newPoint(p.x-this.x, p.y-this.y);
     newPoint vq = new newPoint(q.x-this.x, q.y-this.y);

     double pPolar = vp.polarAngle();
     double qPolar = vq.polarAngle();

     if (pPolar < qPolar) return -1;
     if (pPolar > qPolar) return 1;
     if (vp.length() < vq.length()) return -1;
     if (vp.length() > vq.length()) return 1;
     return 0;
  }

}

