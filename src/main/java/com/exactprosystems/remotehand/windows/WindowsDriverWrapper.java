/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.ScriptExecuteException;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class WindowsDriverWrapper {
	
	private WindowsDriver<?> driver;
	private URL driverUrl;
	
	private WindowsConfiguration windowsConfiguration;

	public WindowsDriverWrapper(URL driverUrl) {
		this.driverUrl = driverUrl;
		this.windowsConfiguration = (WindowsConfiguration) Configuration.getInstance();
	}

	public WindowsDriver<?> getDriver() throws ScriptExecuteException {
		if (driver == null) {
			throw new ScriptExecuteException("Driver was not created. Driver creating action was not performed");
		}
		return driver;
	}

	public WindowsDriver<?> getDriverNullable() {
		return driver;
	}

	public void setDriver(WindowsDriver<?> driver) {
		this.driver = driver;
	}

	public URL getDriverUrl() {
		return driverUrl;
	}

	public DesiredCapabilities createCommonCapabilities() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("platformVersion", "10");
		capabilities.setCapability("platformName", "Windows");
		capabilities.setCapability("deviceName", "WindowsPC");
		return capabilities;
	}
	
	public WindowsDriver<?> newDriver(DesiredCapabilities capabilities) {
		WindowsDriver<WebElement> driver = new WindowsDriver<>(driverUrl, capabilities);
		if (windowsConfiguration.getImplicityWaitTimeout() != null) {
			driver.manage().timeouts().implicitlyWait(windowsConfiguration.getImplicityWaitTimeout(), TimeUnit.SECONDS);
		}
		return driver;
	}

	public boolean isExperimentalDriver() {
		return windowsConfiguration.isExperimentalDriver();
	}

	public String getWaitForApp() {
		return windowsConfiguration.getWaitForApp();
	}

	public String getCreateSessionTimeout() {
		return windowsConfiguration.getCreateSessionTimeout();
	}

	public Integer getNewCommandTimeout() {
		return windowsConfiguration.getNewCommandTimeout();
	}

	public void close() {
		WindowsDriver<?> driverNullable = getDriverNullable();
		if (driverNullable != null)
			driverNullable.close();
	}
}
