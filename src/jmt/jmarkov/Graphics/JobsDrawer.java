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
  
/*
 * Created on 3-apr-2004 by Ernesto
 *
 */
package jmt.jmarkov.Graphics;

import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * MMQueues
 * --------------------------------------
 * 3-apr-2004 - Graphics/JobsDrawer.java
 * 
 * @author Ernesto
 */
public class JobsDrawer extends JPanel implements Notifier, Runnable {

	private Rectangle2D txtBounds;
	private Area a;
	private int 
		donejobs = 0,
		totjobs = 0,
		panelW = 100,
		panelH = 100;
	private DrawConstrains dCst;

	/**
	 * 
	 */
	public JobsDrawer() {
		super();
		dCst = new DrawNormal();
	}

	/**
	 * @param isDoubleBuffered
	 */
	public JobsDrawer(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		dCst = new DrawNormal();
	}

	/**
	 * @param layout
	 */
	public JobsDrawer(LayoutManager layout) {
		super(layout);
		dCst = new DrawNormal();
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public JobsDrawer(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		dCst = new DrawNormal();
	}
	
	public void setTotalJobs(int totjobs){
		reset();
		this.totjobs = totjobs;
	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#runningIn(double)
	 */
	public void runningIn(double t) {

	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#addingToQ()
	 */
	public void addingToQ(double t) {
		donejobs++;
	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#removingFromQ()
	 */
	public void removingFromQ() {
	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#reset()
	 */
	public void reset() {
		donejobs = 0;
		totjobs = 0;
	}
	
	
	private void drawJobsRemaining(int donejobs, int totjobs, float x, float y, float w, float h,  Graphics2D g2d){
		if(totjobs != 0){
			float percent = (float)donejobs / (float)totjobs;
			Color col1 = Color.RED;
			Color col2 = Color.GREEN;
			Line2D jline, tjline;
			Rectangle2D jR = new Rectangle2D.Float((x + percent * w), y, w, h);
			GeneralPath tjpath =  new GeneralPath();
			
			//disegno triangolo jobs totali
			tjpath.moveTo(x, y);
			tjpath.lineTo(x + w, y + h);
			tjpath.lineTo(x, y + h);
			tjpath.closePath();
			GradientPaint gp = new GradientPaint(x, y, Color.GRAY, x + w, y, Color.LIGHT_GRAY,false);
			g2d.setPaint(gp);
			g2d.fill(tjpath);		
	
			//area dei job rimanenti
			a = new Area(tjpath);		
			a.intersect(new Area(jR));
			gp = new GradientPaint(x, y, col1, x + w, y, col2,false);
			g2d.setPaint(gp);		
			g2d.fill(a);
			g2d.setPaint(Color.BLACK);
			g2d.draw(tjpath);	
			
			//legenda:
			for (int i = 0; i < 3; i++){
				tjline = new Line2D.Float(x + w * i / 2, y + h, x + w * i / 2, y + h + h / 5);
				drawCenteredText("" + (int)(totjobs * ((double)i / 2.0)), Color.BLACK, x + w * i / 2, y + 1.25 * h, g2d, true);
			
			}
		}
		else
			drawCenteredText("tot.jobs arrived: " + donejobs,Color.BLACK, this.getWidth()/2.0, this.getHeight()/2.0,g2d,true);	
	}
	
	public void changeDrawSettings(DrawConstrains dCst){
		this.dCst = dCst;
		panelH = this.getHeight();
		panelW = this.getWidth();
	}
	
	private Rectangle2D drawCenteredText(String s, Color c, double centerX, double centerY, Graphics2D g2d, boolean draw){
		double x, y;
		double gap = dCst.getElementsGap();
		g2d.setFont(dCst.getFont());
		txtBounds = dCst.getFont().getStringBounds(s, g2d.getFontRenderContext());
		x = centerX - txtBounds.getWidth()/2.0;
		y = centerY - txtBounds.getY() - txtBounds.getHeight()/2; 
		txtBounds.setRect(
			x - gap,
			y - txtBounds.getHeight() / 2.0 - gap,
			txtBounds.getWidth() + 2 * gap,
			txtBounds.getHeight() + 2 * gap);
		Color ctmp = g2d.getColor();
		g2d.setColor(c);
		if(draw) g2d.drawString(s, (float)x,(float)y);
		g2d.setColor(ctmp);
		return txtBounds;
	}


	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.clearRect(0,0,this.getWidth(),this.getHeight());
		g2d.setStroke(dCst.getDrawStroke());
		changeDrawSettings(dCst);
		
		//disegna jobs
		drawJobsRemaining(donejobs, totjobs, panelW * 0.15f , 10.0f, panelW * 0.7f, panelH / 2.0f,  g2d);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(true){
			try {
				this.repaint();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
