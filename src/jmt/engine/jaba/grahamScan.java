package jmt.engine.jaba;

// grahamScan.java
// 
// Mark F. Hulber
// May 1996
//
//
// grahamScan implements the Graham Scan convex hull algorithm.  Given
//   a vector containing points it will return a vector of points forming
//   the convex hull of the input.  This class relies on extensions to the
//   point class called newPoints.  grahamScan does not begin computing the
//   convex hull until three points have been provided.
//
//


public class grahamScan {

    java.util.Vector lines = new java.util.Vector(100, 100);
    java.util.Stack stk = new java.util.Stack();
    java.util.Vector s = new java.util.Vector(100, 100);


    public java.util.Vector doGraham(java.util.Vector q) {

        int m = 0;
        newPoint temp, temp2;
        int n = q.size();
        int a, i;
        if (n > 2) {
            s.removeAllElements();
            s = (java.util.Vector)q.clone();
            for (i = 1; i < n; i++)
                if (((newPoint)s.elementAt(i)).y < ((newPoint)s.elementAt(m)).y ||
                        ((((newPoint)s.elementAt(i)).y == ((newPoint)s.elementAt(m)).y)&&
                                (((newPoint)s.elementAt(i)).x < ((newPoint)s.elementAt(m)).x)))
                    m = i;
            temp = (newPoint)s.elementAt(0);
            s.setElementAt((newPoint)s.elementAt(m),0);
            s.setElementAt(temp, m);

            // stage 2
            temp2 = (newPoint)s.elementAt(0);
            for (i = 2; i < n; i++) {
                for (int j = n-1; j >= i; j --) {
                    if (temp2.polarCmp((newPoint)s.elementAt(j-1),
                            (newPoint)s.elementAt(j)) == 1) {
                        temp = (newPoint)s.elementAt(j-1);
                        s.setElementAt((newPoint)s.elementAt(j),j-1);
                        s.setElementAt(temp, j);
                    }
                }
            }
            for (i = 1; i+1 < s.size() && (((newPoint)s.elementAt(i+1)).classify(
                    (newPoint)s.elementAt(0),
                    (newPoint)s.elementAt(i)) == 3);
                 i++);   //TODO quick fix by Bertoli Marco ( i+1 < s.size() )
            stk.removeAllElements();
            stk.push((newPoint)s.elementAt(0));
            stk.push((newPoint)s.elementAt(i));

            boolean blah;
            for (i = i+1; i < n; i++) {
                blah = true;
                while ( blah ) {
                    temp2 = (newPoint)stk.pop();
                    //TODO quick fix by Bertoli Marco ( stk.empty() )
                    if (stk.empty() || ((newPoint)s.elementAt(i)).classify((newPoint)stk.peek(),temp2) == 0) {
                        stk.push(temp2);
                        blah = false;
                    }
                }
                stk.push((newPoint)s.elementAt(i));
            }

            lines.removeAllElements();

            while (!stk.empty())
                lines.addElement((newPoint)stk.pop());
            return lines;
        }
        return null;
    }
}
