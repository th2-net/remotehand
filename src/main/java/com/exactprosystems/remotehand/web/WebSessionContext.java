/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactprosystems.remotehand.web;

import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.sessions.SessionContext;

import java.io.File;

public class WebSessionContext extends SessionContext
{
	private WebDriverManager webDriverManager;
	private WebDriverWrapper webDriver;
	private WebElementsDictionary dictionary;
	private String shutdownScript;
	
	public WebSessionContext(String sessionId)
	{
		super(sessionId);
	}
	
	
	public WebDriverManager getWebDriverManager()
	{
		return webDriverManager;
	}
	
	public void setWebDriverManager(WebDriverManager webDriverManager)
	{
		this.webDriverManager = webDriverManager;
	}
	
	
	public WebDriverWrapper getWebDriverWrapper()
	{
		return webDriver;
	}
	
	public void setWebDriverWrapper(WebDriverWrapper webDriver)
	{
		this.webDriver = webDriver;
	}
	
	
	public WebElementsDictionary getDictionary()
	{
		return dictionary;
	}
	
	public void setDictionary(WebElementsDictionary dictionary)
	{
		this.dictionary = dictionary;
	}
	
	
	public WebDriver getWebDriver()
	{
		return webDriver.getDriver();
	}
	
	public File getDownloadDir()
	{
		return webDriver.getDownloadDir();
	}
	
	
	public String getShutdownScript()
	{
		return shutdownScript;
	}
	
	public void setShutdownScript(String shutdownScript)
	{
		this.shutdownScript = shutdownScript;
	}
}
