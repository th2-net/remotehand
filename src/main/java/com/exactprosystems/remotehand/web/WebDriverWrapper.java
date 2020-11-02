/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import java.io.File;

import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.DriverWrapper;

public class WebDriverWrapper implements DriverWrapper<WebDriver>
{
	private final WebDriver driver;
	private final File downloadDir;


	public WebDriverWrapper(WebDriver driver, File downloadDir)
	{
		this.driver = driver;
		this.downloadDir = downloadDir;
	}


	@Override
	public WebDriver getDriver()
	{
		return driver;
	}

	public File getDownloadDir()
	{
		return downloadDir;
	}
}
