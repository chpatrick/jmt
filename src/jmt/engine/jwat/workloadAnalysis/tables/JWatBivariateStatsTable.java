package jmt.engine.jwat.workloadAnalysis.tables;

import java.awt.Color;

import javax.swing.JTable;

import jmt.gui.common.CommonConstants;

public class JWatBivariateStatsTable extends JTable implements CommonConstants{
	public JWatBivariateStatsTable(){
		setSelectionBackground(new Color(181,189,214));
		setSelectionForeground(Color.BLACK);
	}
	// Sets a table model for visualization and editing of data
	public void setModel(JWatBivariateStatsTableModel tabMod) {
		super.setModel(tabMod);
		setRowHeight(ROW_HEIGHT);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}
}
