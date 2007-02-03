/**    
  * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.

  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
  
package jmt.engine.jaba;

import Jama.Matrix;
import jmt.engine.jaba.Hull.Vertex;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: PoliMi
 * Date: 27-lug-2005
 * Time: 11.56.37
 * To change this template use File | Settings | File Templates.
 */
public class Beta3D {

    private double EPSYLON = 0.00001;

    public Sector3D BetaTriangle(newFace f) {
        Vertex v1 = f.getV0();
        int[] v1c = v1.getCoords();
        int v1x = v1c[0];
        int v1y = v1c[1];
        int v1z = v1c[2];

        Vertex v2 = f.getV1();
        int[] v2c = v2.getCoords();
        int v2x = v2c[0];
        int v2y = v2c[1];
        int v2z = v2c[2];

        Vertex v3 = f.getV2();
        int[] v3c = v3.getCoords();
        int v3x = v3c[0];
        int v3y = v3c[1];
        int v3z = v3c[2];

        double[][] arraya =
                {
                    {1, 1, 1, 0, 0, 0, 0, 0, 0},
                    {0, 0, 0, 1, 1, 1, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 1, 1, 1},
                    {v2x, 0, 0, -v1x, 0, 0, 0, 0, 0},
                    {0, v2y, 0, 0, -v1y, 0, 0, 0, 0},
                    {0, 0, v2z, 0, 0, -v1z, 0, 0, 0},
                    {0, 0, 0, v3x, 0, 0, -v2x, 0, 0},
                    {0, 0, 0, 0, v3y, 0, 0, -v2y, 0},
                    {0, 0, 0, 0, 0, v3z, 0, 0, -v2z}
                };

        double[][] arrayb = {{1}, {1}, {1}, {0}, {0}, {0}, {0}, {0}, {0}, };

        Matrix A = new Matrix(arraya);
        Matrix b = new Matrix(arrayb);
        Matrix x = A.solve(b);

        Sector3D out = new Sector3D(x.get(0, 0), x.get(1, 0), x.get(2, 0), x.get(3, 0), x.get(4, 0), x.get(5, 0), x.get(6, 0), x.get(7, 0), x.get(8, 0), 3, v1, v2, v3);

        return out;
    }

    public Vector BetaTriangles(Vector faces) {
        Vector triangles = new Vector();
        Sector3D s3d = new Sector3D();
        for (int i = 0; i < faces.size(); i++) {
            s3d = BetaTriangle((newFace) faces.get(i));
            // Controllo: se una classe è sempre nulla non la inserisco xè non
            // è un triangolo ma sta sul lato.
            if (
                    (Math.abs(s3d.getBeta(0, 1) - 0) > EPSYLON && Math.abs(s3d.getBeta(0, 2) - 0) > EPSYLON && Math.abs(s3d.getBeta(0, 3) - 0) > EPSYLON) &&
                    (Math.abs(s3d.getBeta(1, 1) - 0) > EPSYLON && Math.abs(s3d.getBeta(1, 2) - 0) > EPSYLON && Math.abs(s3d.getBeta(1, 3) - 0) > EPSYLON) &&
                    (Math.abs(s3d.getBeta(2, 1) - 0) > EPSYLON && Math.abs(s3d.getBeta(2, 2) - 0) > EPSYLON && Math.abs(s3d.getBeta(2, 3) - 0) > EPSYLON)
            )
                triangles.addElement(s3d);
        }
        return triangles;
    }

    public Sector3D TriNoB3(BetaVertex v1, BetaVertex v2, Vertex st1, Vertex st2) {


        int[] v1c = st1.getCoords();
        int v1x = v1c[0];
        int v1y = v1c[1];
        int v1z = v1c[2];

        int[] v2c = st2.getCoords();
        int v2x = v2c[0];
        int v2y = v2c[1];
        int v2z = v2c[2];
        /*
        int[] v3c = s3.getCoords();
        int v3x=v3c[0];
        int v3y=v3c[1];
        int v3z=v3c[2];
        */
        double[][] arraya =
                {
                    {1, 1, 1, 0, 0, 0},
                    {0, 0, 0, 1, 1, 1},
                    {v2x, 0, 0, -v1x, 0, 0},
                    {0, v2y, 0, 0, -v1y, 0},
                    {0, 0, v2z, 0, 0, -v1z},
                    {0, 0, 0, 0, 0, 1}
                };

        double[][] arrayb = {{1}, {1}, {0}, {0}, {0}, {0}};
        //t double[][] arrayb ={{1,1,1,0,0,0,0,0,0}};

        Matrix A = new Matrix(arraya);
        Matrix b = new Matrix(arrayb);
        Matrix x = A.solve(b);

        BetaVertex vf1 = new BetaVertex(x.get(0, 0), x.get(1, 0), x.get(2, 0));
        BetaVertex vf2 = new BetaVertex(x.get(3, 0), x.get(4, 0), x.get(5, 0));

        //BetaVertex v3 = new BetaVertex();
        //BetaVertex v4 = new BetaVertex();

        Sector3D out = new Sector3D(vf1, vf2, v1, v2, 2, st1, st2);
        return out;
    }

    public Sector3D TriNoB1(BetaVertex v1, BetaVertex v2, Vertex st1, Vertex st2) {


        int[] v1c = st1.getCoords();
        int v1x = v1c[0];
        int v1y = v1c[1];
        int v1z = v1c[2];

        int[] v2c = st2.getCoords();
        int v2x = v2c[0];
        int v2y = v2c[1];
        int v2z = v2c[2];
        /*
        int[] v3c = s3.getCoords();
        int v3x=v3c[0];
        int v3y=v3c[1];
        int v3z=v3c[2];
        */
        double[][] arraya =
                {
                    {1, 1, 1, 0, 0, 0},
                    {0, 0, 0, 1, 1, 1},
                    {v2x, 0, 0, -v1x, 0, 0},
                    {0, v2y, 0, 0, -v1y, 0},
                    {0, 0, v2z, 0, 0, -v1z},
                    {0, 0, 0, 1, 0, 0}
                };

        double[][] arrayb = {{1}, {1}, {0}, {0}, {0}, {0}};
        //t double[][] arrayb ={{1,1,1,0,0,0,0,0,0}};

        Matrix A = new Matrix(arraya);
        Matrix b = new Matrix(arrayb);
        Matrix x = A.solve(b);

        BetaVertex vf1 = new BetaVertex(x.get(0, 0), x.get(1, 0), x.get(2, 0));
        BetaVertex vf2 = new BetaVertex(x.get(3, 0), x.get(4, 0), x.get(5, 0));

        //BetaVertex v3 = new BetaVertex();
        //BetaVertex v4 = new BetaVertex();

        Sector3D out = new Sector3D(vf1, vf2, v1, v2, 2, st1, st2);
        return out;
    }

    public Sector3D TriNoB2(BetaVertex v1, BetaVertex v2, Vertex st1, Vertex st2) {


        int[] v1c = st1.getCoords();
        int v1x = v1c[0];
        int v1y = v1c[1];
        int v1z = v1c[2];

        int[] v2c = st2.getCoords();
        int v2x = v2c[0];
        int v2y = v2c[1];
        int v2z = v2c[2];
        /*
        int[] v3c = s3.getCoords();
        int v3x=v3c[0];
        int v3y=v3c[1];
        int v3z=v3c[2];
        */
        double[][] arraya =
                {
                    {1, 1, 1, 0, 0, 0},
                    {0, 0, 0, 1, 1, 1},
                    {v2x, 0, 0, -v1x, 0, 0},
                    {0, v2y, 0, 0, -v1y, 0},
                    {0, 0, v2z, 0, 0, -v1z},
                    {0, 0, 0, 0, 1, 0}
                };

        double[][] arrayb = {{1}, {1}, {0}, {0}, {0}, {0}};
        //t double[][] arrayb ={{1,1,1,0,0,0,0,0,0}};

        Matrix A = new Matrix(arraya);
        Matrix b = new Matrix(arrayb);
        Matrix x = A.solve(b);

        BetaVertex vf1 = new BetaVertex(x.get(0, 0), x.get(1, 0), x.get(2, 0));
        BetaVertex vf2 = new BetaVertex(x.get(3, 0), x.get(4, 0), x.get(5, 0));

        //BetaVertex v3 = new BetaVertex();
        //BetaVertex v4 = new BetaVertex();

        Sector3D out = new Sector3D(vf1, vf2, v1, v2, 2, st1, st2);
        return out;
    }

    /**
     * Il metodo controlla se due triangoli sono unibili con un settore in
     * cui saturano 2 stazioni.
     *
     * @param s1
     * @param s2
     * @return
     */
    public boolean Joinable(Sector3D s1, Sector3D s2) {
        int controllo = 0;
        for (int i = 0; i < s1.CountStations(); i++) {
            for (int j = 0; j < s2.CountStations(); j++) {
                if ((Vertex) s1.getS(i) == (Vertex) s2.getS(j)) {
                    controllo++;
                }
            }
        }
        if (controllo == 2)
            return true;
        else
            return false;
    }

    /**
     * Dati due settori triangolari con stazioni in comune restituisce la faccia che li unisce
     *
     * @param s1
     * @param s2
     * @return
     */
    public Sector3D JoinTriangle(Sector3D s1, Sector3D s2) {
        Vector vtemp = new Vector();
        Vector stemp = new Vector();

        //Sector3D out = new Sector3D();
        {
            for (int i = 0; i < s1.CountStations(); i++) {
                for (int j = 0; j < s2.CountStations(); j++) {
                    if ((Vertex) s1.getS(i) == (Vertex) s2.getS(j)) {
                        vtemp.addElement((BetaVertex) s1.getV(i));
                        vtemp.addElement((BetaVertex) s2.getV(j));
                        stemp.addElement((Vertex) s1.getS(i));
                        stemp.addElement((Vertex) s2.getS(j));
                    }
                }
            }
        }
        Sector3D out = new Sector3D(vtemp, 2, stemp);
        return out;
    }

    /**
     * Dato un vettore contenente i settori triangolari restituisce un vettore contenente
     * i settori dove saturano 2 stazioni che li uniscono
     *
     * @param triangles
     * @return
     */
    public Vector JoinTriangles(Vector triangles) {
        Vector sett2staz = new Vector();
        for (int i = 0; i < triangles.size(); i++) {
            //if (i+1>=triangles.size())
            for (int j = i + 1; j < triangles.size(); j++) {
                if (Joinable((Sector3D) triangles.get(i), (Sector3D) triangles.get(j)))
                    sett2staz.addElement(JoinTriangle((Sector3D) triangles.get(i), (Sector3D) triangles.get(j)));
            }
        }
        return sett2staz;
    }

    public Vector Join2Statxy(Vector latixy, Vector lati, Vector sett2staz) {
        Vector out = new Vector();
        int[] p1 = new int[3];
        for (int i = 0; i < lati.size(); i++) {
            for (int j = 0; j < latixy.size(); j++) {
                p1 = ((Vertex) ((Segment3D) lati.get(i)).getS1()).getCoords();
                int[] p2 = ((Vertex) ((Segment3D) lati.get(i)).getS2()).getCoords();
                int[] pxy1 = ((Vertex) ((Segment3D) latixy.get(j)).getS1()).getCoords();
                int[] pxy2 = ((Vertex) ((Segment3D) latixy.get(j)).getS2()).getCoords();

                // 2 segmenti possono essere unibili se hanno gli stessi estremi.
                // Questo può accadere in 2 modi: se A1-A2 e B1-B2 sono gli estremi
                // dei segmenti A e B, si può avere A1=B1 e A2=B2
                if (
                        (
                        p1[0] == pxy1[0] &&
                        p1[1] == pxy1[1] &&
                        p2[0] == pxy2[0] &&
                        p2[1] == pxy2[1]
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    lati.removeElementAt(i);
                    latixy.removeElementAt(j);
                    //i--;
                    //j--;
                } else if (
                        (
                        p1[0] == pxy2[0] &&
                        p1[1] == pxy2[1] &&
                        p2[0] == pxy1[0] &&
                        p2[1] == pxy1[1]
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    lati.removeElementAt(i);
                    latixy.removeElementAt(j);
                    //i--;
                    //j--;
                }
            }
        }
        out.addElement(sett2staz);
        out.addElement(lati);
        out.addElement(latixy);
        return out;
    }

    public Vector Join2Statxz(Vector latixz, Vector lati, Vector sett2staz) {
        Vector out = new Vector();
        int[] p1 = new int[3];
        for (int i = 0; i < lati.size(); i++) {
            for (int j = 0; j < latixz.size(); j++) {
                p1 = ((Vertex) ((Segment3D) lati.get(i)).getS1()).getCoords();
                int[] p2 = ((Vertex) ((Segment3D) lati.get(i)).getS2()).getCoords();
                int[] pxz1 = ((Vertex) ((Segment3D) latixz.get(j)).getS1()).getCoords();
                int[] pxz2 = ((Vertex) ((Segment3D) latixz.get(j)).getS2()).getCoords();

                // 2 segmenti possono essere unibili se hanno gli stessi estremi.
                // Questo può accadere in 2 modi: se A1-A2 e B1-B2 sono gli estremi
                // dei segmenti A e B, si può avere A1=B1 e A2=B2
                if (
                        (
                        p1[0] == pxz1[0] &&
                        pxz1[1] == -1 &&
                        p1[2] == pxz1[2] &&
                        p2[0] == pxz2[0] &&
                        pxz2[1] == -1 &&
                        p2[2] == pxz2[2]
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)));

                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1())));

                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));

                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    lati.removeElementAt(i);
                    latixz.removeElementAt(j);
                    //i--;
                    //j--;
                } else if (
                        (
                        p1[0] == pxz2[0] &&
                        pxz1[1] == -1 &&
                        p1[2] == pxz2[2] &&
                        p2[0] == pxz1[0] &&
                        pxz2[1] == -1 &&
                        p2[2] == pxz1[2]
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    lati.removeElementAt(i);
                    latixz.removeElementAt(j);
                    //i--;
                    //j--;
                }
            }
        }
        out.addElement(sett2staz);
        out.addElement(lati);
        out.addElement(latixz);
        return out;
    }

    public Vector Join2Statyz(Vector latiyz, Vector lati, Vector sett2staz) {
        Vector out = new Vector();
        int[] p1 = new int[3];
        for (int i = 0; i < lati.size(); i++) {
            for (int j = 0; j < latiyz.size(); j++) {
                p1 = ((Vertex) ((Segment3D) lati.get(i)).getS1()).getCoords();
                int[] p2 = ((Vertex) ((Segment3D) lati.get(i)).getS2()).getCoords();
                int[] pyz1 = ((Vertex) ((Segment3D) latiyz.get(j)).getS1()).getCoords();
                int[] pyz2 = ((Vertex) ((Segment3D) latiyz.get(j)).getS2()).getCoords();
                // 2 segmenti possono essere unibili se hanno gli stessi estremi.
                // Questo può accadere in 2 modi: se A1-A2 e B1-B2 sono gli estremi
                // dei segmenti A e B,

                // si può avere A1=B1 e A2=B2
                if (
                        (
                        p1[2] == pyz1[2] &&
                        p1[1] == pyz1[1] &&
                        p2[2] == pyz2[2] &&
                        p2[1] == pyz2[1]
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)));

                    // Aggiungo il segmento A1-B1
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1())));

                    // Aggiungo il segmento A2-B2
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));

                    // Aggiungo il settore creato unendo i 4 segmenti
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));

                    // Rimuovo i segmenti utilizzati
                    lati.removeElementAt(i);
                    latiyz.removeElementAt(j);
                    //i--;
                    //j--;
                }
                // oppure si può avere A1=B2 e A2=B1
                else if (
                        (
                        p1[2] == pyz2[2] &&
                        p1[1] == pyz2[1] &&
                        p2[2] == pyz1[2] &&
                        p2[1] == pyz1[1]
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) lati.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS1()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2()),
                            ((Vertex) ((Segment3D) lati.get(i)).getS2())));
                    lati.removeElementAt(i);
                    latiyz.removeElementAt(j);
                    //i--;
                    //j--;
                }
            }
        }
        out.addElement(sett2staz);
        out.addElement(lati);
        out.addElement(latiyz);
        return out;
    }

    public Vector Join1Staz(Vector latixy, Vector latixz, Vector latiyz, Vector lati, Vector sett1staz) {
        //Vector out = new Vector();


        for (int i = 0; i < lati.size(); i++) {
            //Vector stemp = new Vector();    // Conterrà i segmenti selezionati
            Vector ptemp = new Vector();    // Conterrà i punti dei segmenti
            Vector statp = new Vector();    // Conterrà le stazioni a cui sono associati i punti dei segmenti


            // Creo un array con le coordinate dei primi punti dei lati
            int[] clato = (((Vertex) ((Segment3D) lati.get(i)).getS1()).getCoords());

            // Controllo necessario per i casi particolari in cui vengono collegati come sett2staz
            // due lati del triangolo
            if ((((Vertex) ((Segment3D) lati.get(i)).getS1())) == (((Vertex) ((Segment3D) lati.get(i)).getS2()))) {

                // Cerco tra i segmenti sul lato con beta3=0
                for (int j = 0; j < latixy.size(); j++) {
                    int[] cxy = (((Vertex) ((Segment3D) latixy.get(j)).getS1()).getCoords());
                    if (clato[0] == cxy[0] && clato[1] == cxy[1]) {
                        //stemp.addElement(((Segment3D)latixy.get(j)));
                        ptemp.addElement((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(0));
                        statp.addElement(((Vertex) ((Segment3D) lati.get(i)).getS1()));
                        ptemp.addElement((BetaVertex) ((Segment3D) latixy.get(j)).getBeta(1));
                        statp.addElement(((Vertex) ((Segment3D) lati.get(i)).getS2()));
                        latixy.removeElementAt(j);
                        j--;
                    }
                }
                // Cerco tra i segmenti sul lato con beta2=0
                for (int k = 0; k < latixz.size(); k++) {
                    int[] cxz = (((Vertex) ((Segment3D) latixz.get(k)).getS1()).getCoords());
                    if (clato[0] == cxz[0] && clato[2] == cxz[2]) {
                        //stemp.addElement(((Segment3D)latixz.get(k)));
                        ptemp.addElement((BetaVertex) ((Segment3D) latixz.get(k)).getBeta(0));
                        statp.addElement(((Vertex) ((Segment3D) lati.get(i)).getS1()));
                        ptemp.addElement((BetaVertex) ((Segment3D) latixz.get(k)).getBeta(1));
                        statp.addElement(((Vertex) ((Segment3D) lati.get(i)).getS1()));
                        latixz.removeElementAt(k);
                        k--;
                    }
                }
                // Cerco tra i segmenti sul lato con beta1=0
                for (int h = 0; h < latiyz.size(); h++) {
                    int[] cyz = (((Vertex) ((Segment3D) latiyz.get(h)).getS1()).getCoords());
                    if (clato[1] == cyz[1] && clato[2] == cyz[2]) {
                        //stemp.addElement(((Segment3D)latiyz.get(h)));
                        ptemp.addElement((BetaVertex) ((Segment3D) latiyz.get(h)).getBeta(0));
                        statp.addElement(((Vertex) ((Segment3D) lati.get(i)).getS1()));
                        ptemp.addElement((BetaVertex) ((Segment3D) latiyz.get(h)).getBeta(1));
                        statp.addElement(((Vertex) ((Segment3D) lati.get(i)).getS1()));
                        latiyz.removeElementAt(h);
                        h--;
                    }
                }

                // Cerco tra i segmenti del gruppo lati
                for (int l = 0; l < lati.size(); l++) {

                    int[] clat = (((Vertex) ((Segment3D) lati.get(l)).getS1()).getCoords());
                    if (clato[0] == clat[0] && clato[1] == clat[1] && clato[2] == clat[2]) {
                        ptemp.addElement((BetaVertex) ((Segment3D) lati.get(l)).getBeta(0));
                        //cambiato .get(i) in .get(j)
                        statp.addElement(((Vertex) ((Segment3D) lati.get(l)).getS1()));
                        ptemp.addElement((BetaVertex) ((Segment3D) lati.get(l)).getBeta(1));
                        //cambiato .get(i) in .get(j)
                        statp.addElement(((Vertex) ((Segment3D) lati.get(l)).getS1()));
                        lati.removeElementAt(l);
                        l--;
                    }

                }

                // Elimino i punti doppi
                for (int k = 0; k < ptemp.size(); k++)
                    for (int j = k + 1; j < ptemp.size(); j++) {
                        {
                            if (
                                    ((BetaVertex) ptemp.get(k)).getX() == ((BetaVertex) ptemp.get(j)).getX() &&
                                    ((BetaVertex) ptemp.get(k)).getY() == ((BetaVertex) ptemp.get(j)).getY() &&
                                    ((BetaVertex) ptemp.get(k)).getZ() == ((BetaVertex) ptemp.get(j)).getZ()
                            ) {
                                ptemp.removeElementAt(j);
                                statp.removeElementAt(j);
                                j--;
                            }

                        }
                    }
                sett1staz.addElement(new Sector3D(ptemp, 1, statp));
            }
        }
        return sett1staz;
    }

    /**
     * Presi i vettori con i segmenti sul lato del triangolo li unisce.
     * Utile soprattutto se non ci sono settori con 3 stazioni saturanti contemporaneamente.
     * @param latixy
     * @param latixz
     * @param latiyz
     * @param sett1staz
     * @param sett2staz
     * @return sett2staz
     */
    public Vector Join2StazN3(Vector latixy,Vector latixz,Vector latiyz,Vector sett1staz,Vector sett2staz)
    {
        Vector out = new Vector();

        Vector lati = new Vector();

        if (latixy.size()>1 && latixz.size()>1)
        for (int i = 0; i < latixy.size(); i++)
        {
           int[] p1xy = new int[3];

            // confronto xy-xz
            for (int j = 0; j < latixz.size(); j++)
            {
                p1xy = ((Vertex) ((Segment3D) latixy.get(i)).getS1()).getCoords();
                int[] p2xy = ((Vertex) ((Segment3D) latixy.get(i)).getS2()).getCoords();
                int[] pxz1 = ((Vertex) ((Segment3D) latixz.get(j)).getS1()).getCoords();
                int[] pxz2 = ((Vertex) ((Segment3D) latixz.get(j)).getS2()).getCoords();

                // 2 segmenti possono essere uniti se hanno gli stessi estremi.
                if (
                        (
                        p1xy[0] == pxz1[0] &&
                        p2xy[0] == pxz2[0] &&
                        p1xy[0] != p2xy[0] &&   //assicura che non sia la stessa stazione
                        p2xy[0] != -1           //assicura che non è un estremo
                        )
                        ||
                        (
                        p2xy[0] == pxz1[0] &&
                        p1xy[0] == pxz2[0] &&
                        p1xy[0] != p2xy[0] &&
                        p2xy[0] != -1
                        )
                )

                {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(1)));

                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2())));
                    latixy.removeElementAt(i);
                    latixz.removeElementAt(j);
                }
            }
        }

        if (latixy.size()>1 && latiyz.size()>1)
        for (int i = 0; i < latixy.size(); i++)
        {
           int[] p1xy = new int[3];

            // confronto xy-yz
            if (latiyz.size()>0)
            for (int j = 0; j < latiyz.size(); j++)
            {
                p1xy = ((Vertex) ((Segment3D) latixy.get(i)).getS1()).getCoords();
                int[] p2xy = ((Vertex) ((Segment3D) latixy.get(i)).getS2()).getCoords();
                int[] pyz1 = ((Vertex) ((Segment3D) latiyz.get(j)).getS1()).getCoords();
                int[] pyz2 = ((Vertex) ((Segment3D) latiyz.get(j)).getS2()).getCoords();


                if (
                        (
                        p1xy[1] == pyz1[1] &&
                        p2xy[1] == pyz2[1] &&
                        p1xy[1] != p2xy[1] &&
                        p2xy[1] != -1
                        )
                        ||
                        (
                        p2xy[1] == pyz1[1] &&
                        p1xy[1] == pyz2[1] &&
                        p1xy[1] != p2xy[1] &&
                        p2xy[1] != -1
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(1)));

                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latiyz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) latixy.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2()),
                            ((Vertex) ((Segment3D) latixy.get(i)).getS2())));
                    latixy.removeElementAt(i);
                    latiyz.removeElementAt(j);
                }
            }
        }

        // confronto yz-xz

        if (latiyz.size()>1 && latixz.size()>1)
        for (int i = 0; i < latiyz.size(); i++)
        {
            int[] p1yz = ((Vertex) ((Segment3D) latiyz.get(i)).getS1()).getCoords();
            int[] p2yz = ((Vertex) ((Segment3D) latiyz.get(i)).getS2()).getCoords();

            for (int j = 0; j < latixz.size(); j++)
            {
                int[] pxz1 = ((Vertex) ((Segment3D) latixz.get(j)).getS1()).getCoords();
                int[] pxz2 = ((Vertex) ((Segment3D) latixz.get(j)).getS2()).getCoords();

                // 2 segmenti possono essere uniti se hanno gli stessi estremi.
                if (
                        (
                        p1yz[2] == pxz1[2] &&
                        p2yz[2] == pxz2[2] &&
                        p1yz[2] != p2yz[2] &&
                        p2yz[2] != -1
                        ) ||
                        (
                        p2yz[2] == pxz1[2] &&
                        p1yz[2] == pxz2[2] &&
                        p1yz[2] != p2yz[2] &&
                        p2yz[2] != -1
                        )
                ) {
                    Vector betav = new Vector();
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)));
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(i)).getBeta(0)));
                    betav.addElement(((BetaVertex) ((Segment3D) latiyz.get(i)).getBeta(1)));

                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(0)),
                            ((BetaVertex) ((Segment3D) latiyz.get(i)).getBeta(0)),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS1())));
                    lati.addElement(new Segment3D(((BetaVertex) ((Segment3D) latixz.get(j)).getBeta(1)),
                            ((BetaVertex) ((Segment3D) latiyz.get(i)).getBeta(1)),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS2()),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS2())));
                    sett2staz.addElement(new Sector3D(betav,
                            2,
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS1()),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS2()),
                            ((Vertex) ((Segment3D) latiyz.get(i)).getS2())));
                    latiyz.removeElementAt(i);
                    latixz.removeElementAt(j);
                }
            }
        }
        out.addElement(lati);
        out.addElement(latixy);
        out.addElement(latixz);
        out.addElement(latiyz);
        out.addElement(sett1staz);
        out.addElement(sett2staz);
        return out;
    }

    /**
     * Il metodo è usato per unire i settori complanari, quelli cioè dove saturano più di 3 stazioni contemp.
     *
     * @param triangles è un vettore contenente i settori a triangolo dove saturano 3 stazioni contemporaneamente
     * @return un vettore con i settori uniti
     */
    public Vector JoinComplanars(Vector triangles) {
        Vector out = new Vector();
        for (int i = 0; i < triangles.size(); i++)
            for (int j = i + 1; j < triangles.size(); j++) {
                Vector bi = new Vector();
                int c = 0;
                for (int h = 0; h < ((Sector3D) triangles.get(i)).CountPoint(); h++) {
                    bi.addElement(((Sector3D) triangles.get(i)).getV(h));
                }

                {
                    int[] d = {-1, -1};
                    int k = 0;

                    Vector bj = new Vector();
                    for (int h = 0; h < ((Sector3D) triangles.get(j)).CountPoint(); h++) {
                        bj.addElement(((Sector3D) triangles.get(j)).getV(h));
                    }

                    for (int a = 0; a < bi.size(); a++) {
                        for (int b = 0; b < bj.size(); b++) {
                            if (((BetaVertex) bj.get(b)).CircaEquals(((BetaVertex) bi.get(a)))) {
                                c++;
                                d[k] = a;
                                k++;
                            }
                        }
                    }

                    /* ATTIVARE SOLO PER CONTROLLI
                    System.out.println(c);
                    System.out.println(k);
                    */
                    if (c > 1) {
                        // toglie un triangolo e crea un settore con 4 stazioni
                        ((Sector3D) triangles.get(i)).setType(((Sector3D) triangles.get(i)).getType() + 1);
                        int z = 0;
                        if (d[0] != 0 && d[1] != 0)
                            z = 0;
                        else if (d[0] != 1 && d[1] != 1)
                            z = 1;
                        else if (d[0] != 2 && d[1] != 2) z = 2;
                        BetaVertex betas = new BetaVertex(((Sector3D) triangles.get(j)).getBetas(z));
                        ((Sector3D) triangles.get(i)).AddVertex(betas, ((Sector3D) triangles.get(j)).getS(z));
                        triangles.removeElementAt(j);
                        j--;

                    }
                }
            }

        //todo creare un controllo ciclico che crei settori con N stazioni
        out = triangles;
        return out;
    }

    /**
     * Cancella il settore dove saturano 2 stazioni che unirebbe due settori complanari in cui saturano
     * 3 stazioni
     *
     * @param s3d
     * @return
     */
    public Vector DeleteFake(Vector s3d) {
        for (int i = 0; i < s3d.size(); i++) {


            if (
                    Math.abs(((Sector3D) s3d.get(i)).getBeta(0, 0) - ((Sector3D) s3d.get(i)).getBeta(1, 0)) < EPSYLON &&
                    Math.abs(((Sector3D) s3d.get(i)).getBeta(0, 1) - ((Sector3D) s3d.get(i)).getBeta(1, 1)) < EPSYLON &&
                    Math.abs(((Sector3D) s3d.get(i)).getBeta(0, 2) - ((Sector3D) s3d.get(i)).getBeta(1, 2)) < EPSYLON &&
                    Math.abs(((Sector3D) s3d.get(i)).getBeta(2, 0) - ((Sector3D) s3d.get(i)).getBeta(3, 0)) < EPSYLON &&
                    Math.abs(((Sector3D) s3d.get(i)).getBeta(2, 1) - ((Sector3D) s3d.get(i)).getBeta(3, 1)) < EPSYLON &&
                    Math.abs(((Sector3D) s3d.get(i)).getBeta(2, 2) - ((Sector3D) s3d.get(i)).getBeta(3, 2)) < EPSYLON
            ) {
                s3d.removeElementAt(i);
                i--;
            }
        }
        return s3d;
    }

    /**
     * Crea il settore di saturazione globale nel caso in cui saturi soltanto una stazione
     * @param v la stazione
     * @return il settore
     */
    public Vector OneDominator(Vertex v)
    {
        Vector out = new Vector();

        Vector points = new Vector();
        
        points.addElement(new BetaVertex(1,0,0));
        points.addElement(new BetaVertex(0,1,0));
        points.addElement(new BetaVertex(0,0,1));

        Vector stations = new Vector();
        stations.addElement(v);

        Sector3D s3d = new Sector3D(points,1,stations);

        out.addElement(s3d);
        return out;
    }

}
