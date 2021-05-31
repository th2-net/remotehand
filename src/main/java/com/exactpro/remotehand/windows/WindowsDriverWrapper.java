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

import com.exactpro.remotehand.DriverCloseable;
import com.exactpro.remotehand.ScriptExecuteException;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class WindowsDriverWrapper implements DriverCloseable
{
	private static final Logger logger = LoggerFactory.getLogger(WindowsDriverWrapper.class);
	
	private WindowsDriver<?> driverExp;
	private WindowsDriver<?> driverNotExp;
	private WindowsDriver<?> rootDriverExp;
	private WindowsDriver<?> rootDriverNotExp;
	
	private URL driverUrl;
	private WindowsConfiguration windowsConfiguration;

	public WindowsDriverWrapper(URL driverUrl) {
		this.driverUrl = driverUrl;
		this.windowsConfiguration = WindowsConfiguration.getInstance();
	}

	public WindowsDriver<?> getDriver(boolean root, boolean experimental) throws ScriptExecuteException {
		WindowsDriver<?> driver;
		if (root) {
			driver = experimental ? rootDriverExp : rootDriverNotExp;
			if (driver == null) {
				logger.debug("Creating experimental-{} root driver", experimental);
				driver = createRootDriver(experimental);
			}
		} else {
			driver = experimental ? driverExp : driverNotExp;
			if (driver == null) {
				logger.debug("Creating experimental-{} driver from other driver handleId", experimental);
				WindowsDriver<?> opposite = experimental ? driverNotExp : driverExp;
				if (opposite == null) {
					throw new ScriptExecuteException("Driver was not created. Driver creating action was not performed");
				}
				driver = createFromHandle(opposite.getWindowHandle(), experimental);
			}
		}
		return driver;
	}
	
	private WindowsDriver<?> createFromHandle(String windowHandle, boolean experimental) throws ScriptExecuteException {
		DesiredCapabilities capabilities = this.createCommonCapabilities();
		capabilities.setCapability(WADCapabilityType.APP_TOP_LEVEL, windowHandle);
		this.setExperimentalCapability(capabilities, experimental);
		WindowsDriver<?> windowsDriver = this.newDriver(capabilities);
		if (experimental) {
			this.driverExp = windowsDriver;
		} else {
			this.driverNotExp = windowsDriver;
		}
		return windowsDriver;
	}

	private WindowsDriver<?> createRootDriver(boolean experimental) {
		DesiredCapabilities rootCapabilities = this.createRootCapabilities();
		this.setExperimentalCapability(rootCapabilities, experimental);
		WindowsDriver<?> windowsDriver = newDriver(rootCapabilities);
		if (experimental) {
			this.rootDriverExp = windowsDriver;
		} else {
			this.rootDriverNotExp = windowsDriver;
		}
		return windowsDriver;
	}

	public WindowsDriver<?> createDriver(DesiredCapabilities capabilities, boolean experimental) {
		return this.createDriver(capabilities, experimental, getImplicitlyWaitTimeout());
	}
	
	public WindowsDriver<?> createDriver(DesiredCapabilities capabilities, boolean experimental, Integer timeout) {
		logger.debug("Creating experimental-{} driver with specified capabilities", experimental);
		logger.trace("Specified capabilities: {}", capabilities);
		this.resetWindowDrivers();
		this.setExperimentalCapability(capabilities, experimental);
		WindowsDriver<?> windowsDriver = this.newDriver(capabilities, timeout);
		if (experimental) {
			this.driverExp = windowsDriver;
		} else {
			this.driverNotExp = windowsDriver;
		}
		return windowsDriver;
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
	
	private void closeDriver(WindowsDriver<?> driver, String name) {
		if (driver == null)
			return;
		
		try {
			driver.close();
		} catch (Exception e) {
			logger.warn("Error while disposing driver " + name, e);
		}
	}
	
	public void resetWindowDrivers() {
		this.closeDriver(this.driverExp, "experimental");
		this.closeDriver(this.driverNotExp, "not experimental");
		this.driverExp = null;
		this.driverNotExp = null;
	}
	
	public void switchDriversTo(String handle) throws ScriptExecuteException {
		if (driverExp != null) {
			this.driverExp.switchTo().window(handle);
			String switchedHandle = this.driverExp.getWindowHandle();
			if (!handle.equals(switchedHandle)) {
				logger.error("Tried to change window handle for experimental WAD to {} but current is {}", handle, switchedHandle);
				throw new ScriptExecuteException("Couldn't change current window for experimental driver");
			}
		}
		if (driverNotExp != null) {
			this.driverNotExp.switchTo().window(handle);
			String switchedHandle = this.driverNotExp.getWindowHandle();
			if (!handle.equals(switchedHandle)) {
				logger.error("Tried to change window handle for not-experimental WAD to {} but current is {}", handle, switchedHandle);
				throw new ScriptExecuteException("Couldn't change current window for not-experimental driver");
			}
		}
	}
	
	public void restartDriver(boolean root, boolean experimental) throws ScriptExecuteException {
		logger.debug("Restarting driver root: {} experimental: {}", root, experimental);
		if (root) {
			WindowsDriver<?> driver = experimental ? this.rootDriverExp : this.rootDriverNotExp;
			closeDriver(driver, "root exp: " + experimental);
			this.createRootDriver(experimental);
		} else {
			WindowsDriver<?> driver = experimental ? this.driverExp : this.driverNotExp;
			String handle = driver.getWindowHandle();
			closeDriver(driver, "exp: " + experimental);
			this.createFromHandle(handle, experimental);
		}
	}
	
	private void setExperimentalCapability(DesiredCapabilities capabilities, boolean value) {
		capabilities.setCapability(WADCapabilityType.EXPERIMENTAL_DRIVER, Boolean.toString(value));
	}
	
	@Override
	public void close() {
		this.closeDriver(this.driverExp, "experimental");
		this.closeDriver(this.driverNotExp, "not experimental");
		this.closeDriver(this.rootDriverExp, "root experimental");
		this.closeDriver(this.rootDriverNotExp, "root not experimental");
	}
}
