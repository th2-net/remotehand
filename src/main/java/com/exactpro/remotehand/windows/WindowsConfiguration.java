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

import com.exactpro.remotehand.Configuration;
import org.apache.commons.cli.CommandLine;

import java.util.Collections;
import java.util.Map;

public class WindowsConfiguration extends Configuration {
	private static volatile WindowsConfiguration instance;

	private static final String WINAPP_HOST = "WinAppDriverHost";
	private static final String WINAPP_PORT = "WinAppDriverPort";
	private static final String WINAPP_URL_PATH = "WinAppDriverUrlPath";
	private static final String WINAPP_EXPERIMENTAL_DRIVER = "WinAppExperimentalDriver";
	private static final String WINAPP_WAIT_FOR_APP = "WinAppWaitForApp";
	private static final String WINAPP_IMPL_WAIT = "WinAppImplicitlyTimeout";
	private static final String WINAPP_CREATE_SESSION_TIMEOUT = "WinAppCreateSessionTimeout";
	private static final String WINAPP_NEW_COMMAND_TIMEOUT = "WinAppNewCommandTimeout";
	
	private static final String WINAPP_HOST_DEFAULT = "localhost";
	private static final String WINAPP_PORT_DEFAULT = "4723";
	private static final String WINAPP_URL_PATH_DEFAULT = "";
	private static final boolean WINAPP_EXPERIMENTAL_DRIVER_DEFAULT = false;
	private static final String WINAPP_WAIT_FOR_APP_DEFAULT = "20";
	private static final Integer WINAPP_IMPL_WAIT_DEFAULT = 5;
	private static final String WINAPP_CREATE_SESSION_TIMEOUT_DEFAULT = null;
	private static final Integer WINAPP_NEW_COMMAND_TIMEOUT_DEFAULT = null;
	
	private final String winAppHost;
	private final String winAppPort;
	private final String winAppUrlPath;
	private final boolean experimentalDriver;
	private final String waitForApp;
	private final Integer implicitlyWaitTimeout;
	private final String createSessionTimeout;
	private final Integer newCommandTimeout;

	private WindowsConfiguration(CommandLine commandLine) {
		this(commandLine, Collections.emptyMap());
	}

	private WindowsConfiguration(CommandLine commandLine, Map<String, String> options) {
		super(commandLine, options);

		instance = this;

		this.winAppHost = this.loadProperty(WINAPP_HOST, WINAPP_HOST_DEFAULT);
		this.winAppPort = this.loadProperty(WINAPP_PORT, WINAPP_PORT_DEFAULT);
		this.winAppUrlPath = this.loadProperty(WINAPP_URL_PATH, WINAPP_URL_PATH_DEFAULT);
		
		this.experimentalDriver = this.loadProperty(WINAPP_EXPERIMENTAL_DRIVER,
				WINAPP_EXPERIMENTAL_DRIVER_DEFAULT, Boolean::parseBoolean);
		this.waitForApp = this.loadProperty(WINAPP_WAIT_FOR_APP, WINAPP_WAIT_FOR_APP_DEFAULT);
		this.implicitlyWaitTimeout = this.loadProperty(WINAPP_IMPL_WAIT,
				WINAPP_IMPL_WAIT_DEFAULT, WindowsConfiguration::nullableParseInt);
		this.createSessionTimeout = this.loadProperty(WINAPP_CREATE_SESSION_TIMEOUT, WINAPP_CREATE_SESSION_TIMEOUT_DEFAULT);
		this.newCommandTimeout = this.loadProperty(WINAPP_NEW_COMMAND_TIMEOUT,
				WINAPP_NEW_COMMAND_TIMEOUT_DEFAULT, WindowsConfiguration::nullableParseInt);
	}

	public String getWinAppHost() {
		return winAppHost;
	}

	public String getWinAppPort() {
		return winAppPort;
	}

	public boolean isExperimentalDriver() {
		return experimentalDriver;
	}

	public String getWaitForApp() {
		return waitForApp;
	}

	public Integer getImplicitlyWaitTimeout() {
		return implicitlyWaitTimeout;
	}

	public String getCreateSessionTimeout() {
		return createSessionTimeout;
	}

	public Integer getNewCommandTimeout() {
		return newCommandTimeout;
	}

	public String getWinAppUrlPath() {
		return winAppUrlPath;
	}

	private static Integer nullableParseInt(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		return Integer.parseInt(value);
	}

	public static void init(CommandLine commandLine)
	{
		if (instance != null)
			throw new RuntimeException("Windows configuration already exists");

		instance = new WindowsConfiguration(commandLine);
	}

	public static void init(CommandLine commandLine, Map<String, String> options)
	{
		if (instance != null)
			throw new RuntimeException("Windows configuration already exists");

		instance = new WindowsConfiguration(commandLine, options);
	}

	public static WindowsConfiguration getInstance()
	{
		return instance;
	}
}
