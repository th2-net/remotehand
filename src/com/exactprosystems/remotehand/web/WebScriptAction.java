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

import com.exactprosystems.remotehand.ScriptAction;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.web.webelements.WebLocator;
import org.openqa.selenium.WebDriver;


public class WebScriptAction extends ScriptAction
{
	private WebDriver webdriver = null;
	private WebLocator webLocator = null;
	private Map<String, String> params = null;
	
	WebScriptAction(WebAction webAction, WebDriver webDriver, WebLocator webLocator,
			Map<String, String> params) throws ScriptCompileException
	{
		super(webAction);
		if (webAction == null)
			throw new ScriptCompileException("Web action tag is missing in the header");

		this.webLocator = webLocator;
		this.params = params;
		this.webdriver = webDriver;
	}


	public WebLocator getWebLocator() 
	{
		return webLocator;
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

	public WebDriver getDriver () {
		return webdriver;
	}
	
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		result.append("WebAction=").append(action.getClass().getSimpleName()).append(";");
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
