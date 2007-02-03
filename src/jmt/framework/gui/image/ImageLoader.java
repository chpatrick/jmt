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
package jmt.framework.gui.image;

import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * <p><b>Name:</b> ImageLoader</p> 
 * <p><b>Description:</b> 
 * A class used to load images. Each subclass must implement the <code>getImageURL</code>
 * method to retrive URL of resource to be loaded. This class holds a cache of loaded images.
 * Cache size can be adjusted using the <code>maxCache</code> field.
 * </p>
 * <p><b>Date:</b> 23/gen/07
 * <b>Time:</b> 15:09:14</p>
 * @author Bertoli Marco
 * @version 1.0
 */
public abstract class ImageLoader {
    private static final String[] EXTENSIONS = new String[] {".gif", ".png", ".jpg"};
    /** Maximum number of cached elements */
    protected int maxCache = 256; 
    /** Internal caching data structure */
    protected HashMap iconCache = new HashMap();
    
    /**
     * Returns the url of a given resource
     * @param resourceName name of the resource to be retrived
     * @return url of the resource or null if it was not found
     */
    protected abstract URL getImageURL(String resourceName);
    
    /**
     * Loads an icon with specified name and caches it.
     * @param iconName name of the icon to be loaded. Extensions are automatically added, if needed.
     * @return icon if found, null otherwise
     */
    public ImageIcon loadIcon(String iconName) {
        if (iconName == null)
            return null;
        if (iconCache.containsKey(iconName))
            return (ImageIcon)iconCache.get(iconName);
        
        URL url = getImageURL(iconName);
        // If image is not found, try to add extensions
        for (int i=0; i<EXTENSIONS.length && url == null; i++) {
            url = getImageURL(iconName + EXTENSIONS[i]);
        }
        
        ImageIcon tmp = null;
        if (url != null)
            tmp = new ImageIcon(url);
        
        // Clears cache when it's bigger than maxCache. This is not needed but avoids memory leakages...
        if (iconCache.size() > maxCache)
            iconCache.clear();
        
        iconCache.put(iconName, tmp);
        return tmp;
    }
    
    /**
     * Loads an icon with specified name and caches it, then resizes it.
     * @param iconName name of the icon to be loaded. Extensions are automatically added, if needed.
     * @param size target dimension of image. a negative number means to mantain aspect ratio on that dimension
     * @return icon if found, null otherwise
     */
    public ImageIcon loadIcon(String iconName, Dimension size) {
        ImageIcon im = loadIcon(iconName);
        if(im != null) {
            Image scaled = im.getImage();
            scaled = scaled.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        else 
            return im;
    }
}
