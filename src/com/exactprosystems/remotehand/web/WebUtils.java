////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.remotehand.web;

import java.util.Map;

/**
 * @author anna.bykova.
 */
public class WebUtils
{
	public static boolean getBooleanOrDefault(Map<String, String> params, String name, boolean defaultValue)
	{
		String value = params.get(name);
		if (value == null || value.isEmpty())
			return defaultValue;
		else 
			return WebScriptCompiler.YES.contains(value.toLowerCase());
	}
}
