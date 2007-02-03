package jmt.engine.jwat.workloadAnalysis.panels;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;

import jmt.engine.jwat.workloadAnalysis.tables.JWatBivariateStatsTable;
import jmt.engine.jwat.workloadAnalysis.tables.JWatBivariateStatsTableModel;
import jmt.engine.jwat.workloadAnalysis.tables.renderers.RowHeaderRenderer;

public class ScrollBivariatePanel extends JScrollPane {
	
	private JWatBivariateStatsTable tab = null; 
	
	public ScrollBivariatePanel(JWatBivariateStatsTable table){
		tab = table;
		setViewportView(tab);
		ListModel lm = new AbstractListModel() {
		      String headers[] = ((JWatBivariateStatsTableModel)tab.getModel()).getNames();

		      public int getSize() {
		        return headers.length;
		      }

		      public Object getElementAt(int index) {
		        return headers[index];
		      }
		    };
	    JList rowHeader = new JList(lm);
	    rowHeader.setBackground(getBackground());
	    rowHeader.setFixedCellWidth(100);
	    rowHeader.setFixedCellHeight(tab.getRowHeight());

	    rowHeader.setCellRenderer(new RowHeaderRenderer(tab));

	    setRowHeaderView(rowHeader);
	}
	public void setNames(){
			ListModel lm = new AbstractListModel() {
				String headers[] = ((JWatBivariateStatsTableModel)tab.getModel()).getNames();
				
				public int getSize() {
					return headers.length;
				}
				
				public Object getElementAt(int index) {
					return headers[index];
				}
			};
			JList rowHeader = new JList(lm);
			rowHeader.setBackground(getBackground());
			rowHeader.setFixedCellWidth(100);
			rowHeader.setFixedCellHeight(tab.getRowHeight());
			
			rowHeader.setCellRenderer(new RowHeaderRenderer(tab));
			
			setRowHeaderView(rowHeader);
	}
}
