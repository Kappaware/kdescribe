/*
 * Copyright (C) 2016 BROADSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kappaware.kdescribe;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;

public class Misc {

	public static String toYamlString(Object o, YamlConfig yamlConfig) {
		StringWriter sw = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(sw, yamlConfig);
		try {
			yamlWriter.write(o);
			yamlWriter.close();
			sw.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception in YAML generation", e);
		}
		return sw.toString();
	}

	public static String printIsoDateTime(Long ts) {
		if (ts != null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(ts);
			return DatatypeConverter.printDateTime(c);
		} else {
			return null;
		}
	}

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static String printSimpleIsoDateTime(Long ts) {
		if (ts != null) {
			return dateFormat.format(new Date(ts));
		} else {
			return null;
		}
	}

	public static Long parseLong(String s) {
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static String listToString(List<String> l) {
		StringBuffer sb = new StringBuffer();
		if( l == null) {
			sb.append("null");
		} else {
		sb.append("[");
		String sep=" ";
		for(String s : l) {
			sb.append(String.format("%s'%s'", sep, s));
			sep = " ,";
		}
		sb.append("]");
		}
		return sb.toString();
	}

}
