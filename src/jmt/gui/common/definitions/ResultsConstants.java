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
  
package jmt.gui.common.definitions;

/**
 * <p>Title: Results Constants</p>
 * <p>Description: All sort of constanttext displayed into results window. This is provied here
 * for easy mantainability.</p>
 * 
 * @author Bertoli Marco
 *         Date: 26-set-2005
 *         Time: 11.06.50
 */
public interface ResultsConstants {
    public static final int BORDERSIZE = 20;
    public static final String IN_PROGRESS_IMAGE = "Measure_running";
    public static final String IN_PROGRESS_TEXT = "Simulator is still computing this measure";
    public static final String SUCCESS_IMAGE = "Measure_ok";
    public static final String SUCCESS_TEXT = "This measure was computed with the specified confidence interval (red lines) and the requested maximum relative error";
    public static final String FAILED_IMAGE = "Measure_fail";
    public static final String FAILED_TEXT = "Simulator failed to compute this measure with the specified confidence interval and maximum relative error";
    public static final String NO_SAMPLES_IMAGE = "Measure_nosamples";
    public static final String NO_SAMPLES_TEXT = "Simulator cannot compute this measure as no samples were received";
    public static final String ALL_CLASSES = "-- All --";
    public static final String ALL_STATIONS = "-- Network --";

    /**HTML formats for panels descriptions*/
    final static String HTML_START = "<html><body align=\"left\">";
    final static String HTML_END = "</body></html>";
    final static String HTML_FONT_TITLE = "<font size=\"4\"><b>";
    final static String HTML_FONT_NORM = "<font size=\"3\">";
    final static String HTML_FONT_TIT_END = "</b></font><br>";
    final static String HTML_FONT_NOR_END = "</font>";

    // Tabbed panels description
    public static final String DESCRIPTION_QUEUELENGTHS = HTML_START + HTML_FONT_TITLE + "Queue Length" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average number of customers for each chosen class at each chosen station."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_THROUGHPUTS = HTML_START + HTML_FONT_TITLE + "Throughput" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average throughput for each chosen class at each chosen station."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_RESPONSETIMES = HTML_START + HTML_FONT_TITLE + "Response Time" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average response time for each chosen class at each chosen station."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_UTILIZATIONS =  HTML_START + HTML_FONT_TITLE + "Utilization" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average utilization for each chosen class at each chosen station. If a station "
                + "has more than one server, utilization can be greater than one."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_RESIDENCETIMES = HTML_START + HTML_FONT_TITLE + "Residence Time" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average residence time for each chosen class at each chosen station. (Residence Time = Number of Visits * Response Time)"
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_QUEUETIMES =  HTML_START + HTML_FONT_TITLE + "Queue Time" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average queue time for each chosen class at each chosen station."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_SYSTEMRESPONSETIMES =  HTML_START + HTML_FONT_TITLE + "System Response Time" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average response time of the entire system for each chosen class."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_SYSTEMTHROUGHPUTS =  HTML_START + HTML_FONT_TITLE + "System Throughput" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average throughput of the entire system for each chosen class."
                + HTML_FONT_NOR_END + HTML_END;
    public static final String DESCRIPTION_CUSTOMERNUMBERS =  HTML_START + HTML_FONT_TITLE + "Customer Number" + HTML_FONT_TIT_END
                + HTML_FONT_NORM + "Average customer number for each chosen class."
                + HTML_FONT_NOR_END + HTML_END;

    // Temp measure mean label
    public static final String TEMP_MEAN = HTML_START + HTML_FONT_NORM +
            "<b>Average value: </b>" + HTML_FONT_NOR_END + HTML_END;
}
