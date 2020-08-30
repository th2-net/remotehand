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
import org.apache.commons.cli.CommandLine;

public class WindowsConfiguration extends Configuration {
	
	private static final String WINAPP_HOST = "WinAppDriverHost";
	private static final String WINAPP_PORT = "WinAppDriverPort";
	private static final String WINAPP_EXPERIMENTAL_DRIVER = "WinAppExperimentalDriver";
	private static final String WINAPP_WAIT_FOR_APP = "WinAppWaitForApp";
	private static final String WINAPP_IMPL_WAIT = "WinAppImplicitlyTimeout";
	private static final String WINAPP_CREATE_SESSION_TIMEOUT = "WinAppCreateSessionTimeout";
	private static final String WINAPP_NEW_COMMAND_TIMEOUT = "WinAppNewCommandTimeout";
	
	private static final String WINAPP_HOST_DEFAULT = "localhost";
	private static final String WINAPP_PORT_DEFAULT = "4723";
	private static final boolean WINAPP_EXPERIMENTAL_DRIVER_DEFAULT = true;
	private static final String WINAPP_WAIT_FOR_APP_DEFAULT = "20";
	private static final Integer WINAPP_IMPL_WAIT_DEFAULT = 5;
	private static final String WINAPP_CREATE_SESSION_TIMEOUT_DEFAULT = null;
	private static final Integer WINAPP_NEW_COMMAND_TIMEOUT_DEFAULT = null;
	
	private final String winAppHost;
	private final String winAppPort;
	private final boolean experimentalDriver;
	private final String waitForApp;
	private final Integer implicityWaitTimeout;
	private final String createSessionTimeout;
	private final Integer newCommandTimeout;
	
	protected WindowsConfiguration(CommandLine commandLine) {
		super(commandLine);
		
		this.winAppHost = this.loadProperty(WINAPP_HOST, WINAPP_HOST_DEFAULT);
		this.winAppPort = this.loadProperty(WINAPP_PORT, WINAPP_PORT_DEFAULT);
		
		this.experimentalDriver = this.loadProperty(WINAPP_EXPERIMENTAL_DRIVER,
				WINAPP_EXPERIMENTAL_DRIVER_DEFAULT, Boolean::parseBoolean);
		this.waitForApp = this.loadProperty(WINAPP_WAIT_FOR_APP, WINAPP_WAIT_FOR_APP_DEFAULT);
		this.implicityWaitTimeout = this.loadProperty(WINAPP_IMPL_WAIT,
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

	public Integer getImplicityWaitTimeout() {
		return implicityWaitTimeout;
	}

	public String getCreateSessionTimeout() {
		return createSessionTimeout;
	}

	public Integer getNewCommandTimeout() {
		return newCommandTimeout;
	}

	private static Integer nullableParseInt(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		return Integer.parseInt(value);
	}
	
	
}
