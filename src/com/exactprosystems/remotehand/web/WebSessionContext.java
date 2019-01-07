/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.http.SessionContext;
import org.openqa.selenium.WebDriver;

import java.io.File;

/**
 * @author anna.bykova.
 */
public class WebSessionContext extends SessionContext
{
	private WebDriverManager webDriverManager;
	private WebDriver webDriver;
	private WebElementsDictionary dictionary;
	private File donwloadDir;

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

	public WebDriver getWebDriver()
	{
		return webDriver;
	}

	public void setWebDriver(WebDriver webDriver)
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

	public File getDonwloadDir() {
		return donwloadDir;
	}

	public void setDonwloadDir(File donwloadDir) {
		this.donwloadDir = donwloadDir;
	}
}
