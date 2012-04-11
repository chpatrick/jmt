/**    
 * Copyright (C) 2012, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.gui.common.classSwitch;

import java.util.HashMap;

public class ClassSwitch {

	private Object classInKey;
	private HashMap<Object, Float> rowValues;

	public ClassSwitch() {
		this.classInKey = null;
		this.rowValues = new HashMap<Object, Float>();
	}

	public String getName() {
		return "Class Switch";
	}

	/**
	 * The first time this method is invoked classInKey field
	 * is populate, moreover the <classInKey, classInKey> element
	 * is created (in order to create the I matrix).
	 * @param classInKey
	 * @param classOutKey
	 * @return
	 */
	public float getValue(Object classInKey, Object classOutKey) {
		if (this.classInKey == null) {
			this.classInKey = classInKey;
			this.rowValues.put(classInKey, new Float(1.0f));
		}
		//We search for classOutKey, if we find the relative value
		//is returned else we return 0.
		if(!this.rowValues.containsKey(classOutKey)) {
			return 0.0f;
		}
		return this.rowValues.get(classOutKey);
	}
	
	public void setValue(Object classInKey, Object classOutKey, float val) {
		if (this.classInKey == null) {
			this.classInKey = classInKey;
			this.rowValues.put(classInKey, new Float(1.0f));
		}
		this.rowValues.put(classOutKey, val);
	}

	@Override
	public ClassSwitch clone() {
		return new ClassSwitch();
	}


}
