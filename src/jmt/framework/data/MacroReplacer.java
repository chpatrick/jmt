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
package jmt.framework.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p><b>Name:</b> MacroReplacer</p> 
 * <p><b>Description:</b> 
 * This class is used to replace macros basing on variable values
 * </p>
 * <p><b>Date:</b> 25/jun/2012
 *    <b>Time:</b> 19:57:49</p>
 * @author Bertoli Marco
 * @version 1.0
 */
public class MacroReplacer {
	private static final String TOKEN = "${"; 
	private static final Pattern MACRO_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
	
	/**
	 * Replace system variables inside the given string.
	 * Variables are expressed as <code>${variable}</code>
	 * @param str the string to replace
	 * @return replaced string
	 */
	public static String replaceSystem(String str) {
		if (str.indexOf(TOKEN) < 0) {
			return str;
		}
		
		StringBuffer ret = new StringBuffer(str.length());
		Matcher m = MACRO_PATTERN.matcher(str);
		while (m.find()) {
			String value = System.getProperty(m.group(1));
			if (value == null) {
				value = m.group(0);
			}
			m.appendReplacement(ret, Matcher.quoteReplacement(value));
		}
		m.appendTail(ret);
		return ret.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(replaceSystem("Test: ${user.home}/JMT/"));
		System.out.println(replaceSystem("Test2: ${user.dir}/JMT/"));
		System.out.println(replaceSystem("Test3: ${user.dirko}/JMT/"));
	}
}
