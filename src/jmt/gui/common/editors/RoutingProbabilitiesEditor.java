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
  
package jmt.gui.common.editors;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.panels.WarningScrollTable;
import jmt.gui.common.routingStrategies.RoutingStrategy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: orsotronIII
 * Date: 4-lug-2005
 * Time: 11.52.41
 * Modified by Bertoli Marco 7-oct-2005
 */
public class RoutingProbabilitiesEditor extends JSplitPane implements CommonConstants {

    private HashMap routingProbs;
    private StationDefinition stations;
    private Object stationKey, classKey;

    private WarningScrollTable rtPane;
    private JTextArea descrTextPane = new JTextArea("");
    private JScrollPane descrPane = new JScrollPane();
    private RoutingTable routingTable = new RoutingTable();
    private JTextArea noOptLabel = new JTextArea("No options available for this routing strategy");
    private JScrollPane noOptLabelPanel = new JScrollPane(noOptLabel);

    public RoutingProbabilitiesEditor(StationDefinition sd,
                                      Object stationKey,
                                      Object classKey){
        super();
        super.setOrientation(JSplitPane.VERTICAL_SPLIT);
        super.setDividerSize(3);
        super.setBorder(new EmptyBorder(0,0,0,0));
        this.setResizeWeight(.5);
        initComponents();
        setData(sd, stationKey, classKey);
    }

    private void initComponents(){
        rtPane = new WarningScrollTable(routingTable, WARNING_ROUTING);
        noOptLabelPanel.setBorder(new TitledBorder(new EtchedBorder(), "Routing Options"));
        noOptLabel.setOpaque(false);
        noOptLabel.setEditable(false);
        noOptLabel.setLineWrap(true);
        noOptLabel.setWrapStyleWord(true);
        rtPane.setBorder(new TitledBorder(new EtchedBorder(), "Routing Options"));
        descrTextPane.setOpaque(false);
        descrTextPane.setEditable(false);
        descrTextPane.setLineWrap(true);
        descrTextPane.setWrapStyleWord(true);
        descrPane.setBorder(new TitledBorder(new EtchedBorder(), "Description"));
        descrPane.setViewportView(descrTextPane);
        setLeftComponent(descrPane);
    }

    public void setData(StationDefinition sd,
                        Object stationKey,
                        Object classKey){
/*        if(stationKey==null||classKey==null||sd==null){
            emptyPane();
            return;
        }
*/        RoutingStrategy rs =
                (RoutingStrategy)sd.getRoutingStrategy(stationKey, classKey);
        if(rs==null){
            emptyPane();
        }else{
            descrTextPane.setText(rs.getDescription());
            if(rs.getValues() == null){
                emptyPane();
            }else{
                createDetails(rs, sd, stationKey, classKey);
            }
        }
        doLayout();
    }

    private void emptyPane(){
        setRightComponent(noOptLabelPanel);
        routingProbs=null;
    }

    private void createDetails(RoutingStrategy rs,
                               StationDefinition sd,
                               Object stationKey,
                               Object classKey){
        routingProbs = rs.getValues();
        stations = sd;
        this.stationKey = stationKey;
        this.classKey = classKey;
        setupRouting();
        setRightComponent(rtPane);
    }


    /**sets up all of the entries in routing table from output connections for specified
     * station*/
    protected void setupRouting(){
        if(stationKey==null||classKey==null
                ||stations==null||routingProbs==null) return;
        //fetching output-connected stations list
        Vector output = stations.getForwardConnections(stationKey);
        //saving all entries of routing strategy in a temporary data structure
        HashMap temp = new HashMap(routingProbs);
        routingProbs.clear();
        for(int i=0; i<output.size(); i++){
            //add old entries to map only if they are still in the current connection set
            Object currentKey = output.get(i);
            if(temp.containsKey(currentKey)){
                routingProbs.put(currentKey, temp.get(currentKey));
            }else{
                //if connection set contains new entries, set them to 0 by default
                routingProbs.put(currentKey, new Double(0.0));
            }
        }

    }

    public void stopEditing(){
        if (routingTable.getCellEditor() != null)
            routingTable.getCellEditor().stopCellEditing();
    }


    protected class RoutingTable extends JTable{

        public RoutingTable(){
            super();
            setModel(new RoutingTableModel());
            setDefaultEditor(Object.class, new jmt.gui.exact.table.ExactCellEditor());
            sizeColumns();
        }

        private void sizeColumns(){
            for(int i=0; i<getColumnCount(); i++){
                getColumnModel().getColumn(i).setMinWidth(
                        ((RoutingTableModel)getModel()).columnSizes[i]);
            }
        }

    }

    protected class RoutingTableModel extends AbstractTableModel{

        private String[] columnNames = new String[]{"Destination", "Probability"};
        private Class[] columnClasses = new Class[]{String.class, Object.class};
        public int[] columnSizes = new int[]{80, 60};

        public int getRowCount() {
            if(stationKey !=null) {
                return stations.getForwardConnections(stationKey).size();
            }else return 0;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public Class getColumnClass(int columnIndex) {
            return columnClasses[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex==1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if(routingProbs==null) return null;
            if(columnIndex==0){
                return stations.getStationName(indexToKey(rowIndex));
            }else if(columnIndex == 1){
                return routingProbs.get(indexToKey(rowIndex));
            }else return null;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(columnIndex == 1){
                try{
                    Double value = Double.valueOf((String)aValue);
                    if (value.doubleValue() >= 0)
                        routingProbs.put(indexToKey(rowIndex), value);
                }catch(NumberFormatException e){}
            }
        }

        //retrieves station search key from index in table
        private Object indexToKey(int index){
            if(stationKey == null) return null;
            return stations.getForwardConnections(stationKey).get(index);
        }
    }

}
