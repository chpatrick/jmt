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
package jmt.framework.gui.components;

import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicHTML;

import jmt.framework.net.BareBonesBrowserLaunch;

/**
 * <p><b>Name:</b> HtmlPanel</p> 
 * <p><b>Description:</b> 
 * An HTML browser that shows a given URL. Links are opened in the same window
 * if have the same path, otherwise will be opened in system browser.
 * </p>
 * <p><b>Date:</b> 29/gen/07
 * <b>Time:</b> 17:37:49</p>
 * @author Bertoli Marco
 * @version 1.0
 */
public class HtmlPanel extends JEditorPane {
    /**
     * Builds a new HtmlPanel
     */
    public HtmlPanel() {
        this(null);
    }
    
    /**
     * Builds a new HtmlPanel that shows given url
     * @param url url of the page to be displayed
     */
    public HtmlPanel(URL url) {
        init();
        if (url != null) {
            setURL(url);
            putClientProperty(BasicHTML.documentBaseKey, url.getPath());
            // The following line fixes a problem with incorrectly drawn text inside tables.
            setCaretPosition(0);
        }
    }
    
    /**
     * Sets the page to be displayed in the panel
     * @param url url of the page to be displayed
     */
    public void setURL(URL url) {
        try {
            setPage(url);
        } catch (IOException e1) {
            setText("<html><em>Page Unavailable</em></html>");
        }
        repaint();
    }
    
    /**
     * Initialize this component
     */
    private void init() {
        // By default disable editing
        setEditable(false);
        setContentType("text/html");
        
        // Adds hyperlink listener
        this.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED){
                    // An hyperlink is activated
                    if (getPage() != null && e.getURL().getPath() != null && 
                            e.getURL().getPath().equals(getPage().getPath()))
                        setURL(e.getURL());
                    else
                        // Open external links in default browser
                        BareBonesBrowserLaunch.openURL(e.getURL().toString());
                }
            }
        });
    }
}
