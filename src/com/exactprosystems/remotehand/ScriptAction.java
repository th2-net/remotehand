////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import java.util.Map;

import com.exactprosystems.remotehand.webelements.WebLocator;

public class ScriptAction
{
	private WebAction webAction		 = null;
	private WebLocator webLocator		 = null;
	private Map<String, String> params	 = null;
	
	ScriptAction(WebAction webAction, WebLocator webLocator, Map<String, String> params) throws ScriptCompileException
	{
		if (webAction == null)
			throw new ScriptCompileException("Web action tag is missing in the header");
		
		this.webAction	 = webAction;
		this.webLocator	 = webLocator;
		this.params		 = params;
	}

	public WebAction getWebAction() 
	{
		return webAction;
	}

	public WebLocator getWebLocator() 
	{
		return webLocator;
	}

	public void setWebAction(WebAction webAction) 
	{
		this.webAction = webAction;
	}

	public void setWebLocator(WebLocator webLocator) 
	{
		this.webLocator = webLocator;
	}

	public Map<String, String> getParams()
	{
		return params;
	}
	
	public void setParams(Map<String, String> params) 
	{
		this.params = params;
	}
	
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		result.append("WebAction=").append(webAction.getClass().getSimpleName()).append(";");
		if (webLocator != null)
			result.append("WebLocator=").append(webLocator.getClass().getSimpleName()).append(";");
		
		result.append("Parameters:");
		for (String param : params.keySet()) 
		{
			result.append(param).append("=");
			result.append(params.get(param)).append(";");
		}

		return result.toString();
	}
}
