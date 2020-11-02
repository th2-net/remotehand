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
import com.exactprosystems.remotehand.DriverWrapper;
import com.exactprosystems.remotehand.ScriptExecuteException;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class WindowsDriverWrapper implements DriverWrapper<WindowsDriver<?>>
{
	private static final Logger logger = LoggerFactory.getLogger(WindowsDriverWrapper.class);
	
	private WindowsDriver<?> driver;
	private WindowsDriver<?> rootDriver;
	private URL driverUrl;
	
	private WindowsConfiguration windowsConfiguration;

	public WindowsDriverWrapper(URL driverUrl) {
		this.driverUrl = driverUrl;
		this.windowsConfiguration = WindowsConfiguration.getInstance();
	}

	@Override
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
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "10");
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Windows");
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "WindowsPC");
		return capabilities;
	}

	public DesiredCapabilities createRootCapabilities() {
		DesiredCapabilities capabilities = this.createCommonCapabilities();
		capabilities.setCapability(MobileCapabilityType.APP, "Root");
		return capabilities;
	}

	public WindowsDriver<?> newDriver(DesiredCapabilities capabilities) {
		return newDriver(capabilities, getImplicityWaitTimeout());
	}
	
	public WindowsDriver<?> newDriver(DesiredCapabilities capabilities, Integer implTimeout) {
		WindowsDriver<WebElement> driver = new WindowsLoggingDriver<>(driverUrl, capabilities);
		if (implTimeout != null) {
			driver.manage().timeouts().implicitlyWait(implTimeout, TimeUnit.SECONDS);
		}
		return driver;
	}
	
	public WindowsDriver<?> getOrCreateRootDriver() {
		if (rootDriver == null) {
			rootDriver = this.newDriver(this.createRootCapabilities());
		}
		return rootDriver;
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

	public Integer getImplicityWaitTimeout() {
		return windowsConfiguration.getImplicityWaitTimeout();
	}
	
	public void close() {
		if (driver != null) {
			try {
				driver.close();
			} catch (Exception e) {
				logger.warn("Error while disposing driver", e);
			}
		}
		if (rootDriver != null) {
			try {
				rootDriver.close();
			} catch (Exception e) {
				logger.warn("Error while disposing ROOT driver", e);
			}
		}
	}
}
