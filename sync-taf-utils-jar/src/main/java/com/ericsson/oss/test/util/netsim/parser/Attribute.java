/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.test.util.netsim.parser;

import java.util.HashMap;
import java.util.Map;

public class Attribute {

	final private int indentation;
	final private String name;
	private Object value = null;
	
	public Attribute(final int indentation, final String name, final Object value) {
		this.indentation = indentation;
		this.name = name;
		this.value = value;		
	}

	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public void addValueToMap(final String name, final Object value) {
		if(this.value instanceof String && !Utils.isEmpty((String)this.value)) {
			throw new IllegalStateException("cannot override a non-empty string value with a map!  existing value: " + this.value + ", new value: " + value);
		}
		
		if(this.value == null || !(this.value instanceof Map)) {
			this.value = new HashMap<String, Object>();
		}		
		((Map<String, Object>)this.value).put(name, value);
	}
	
	public void setValue(final Object value) {
		this.value = value;
	}
	
	public int getIndentation() {
		return indentation;
	}
}
