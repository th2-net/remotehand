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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.DriverWrapper;
import com.exactpro.remotehand.ScriptExecuteException;
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
	
	private static final String APP_TOP_LEVEL_WINDOW_CAPABILITY = "appTopLevelWindow";
	
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
		return newDriver(capabilities, getImplicitlyWaitTimeout());
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
	
	public WindowsDriver<?> changeDriverForNewMainWindow(String windowHandle)
	{
		DesiredCapabilities dc = createCommonCapabilities();
		dc.setCapability(APP_TOP_LEVEL_WINDOW_CAPABILITY, windowHandle);

		WindowsDriver<?> oldDriver = driver;
		driver = newDriver(dc);
		
		logger.debug("Driver has been changed to catch new top-level window '{}'.", windowHandle);
		
		oldDriver.close();
		
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

	public Integer getImplicitlyWaitTimeout() {
		return windowsConfiguration.getImplicitlyWaitTimeout();
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
