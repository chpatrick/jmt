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
  
package jmt.framework.data;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: orsotronIII
 * Date: 28-giu-2005
 * Time: 17.07.42
 * This class is an optimized version of an {@link java.util.HashMap}. Optimization
 * is focused on multiple acesses. This implementation provides a single-element cache
 * for speed-up of data search operations, in particular the most recently requested
 * element is stored to assure a faster search.
 */
public class CachedHashMap extends HashMap{

    //Most recently used key and relative value.
    private Object mruValue,
    mruKey;

    /**Returns requested object. If this object was requested before, search
     * operation is faster.*/
    public Object get(Object key){
        if(key != null){
            if(key.equals(mruKey)){
                return mruValue;
            }
        }
        mruKey = key;
        mruValue = super.get(key);
        return mruValue;
    }


    /**Method overridden to avoid aliasing between currently stored value and
     * superclass matching.*/
    public Object put(Object key, Object value){
        if(key!=null){
            if(key.equals(mruKey)){
                mruValue = value;
            }
        }
        return super.put(key, value);
    }

}
