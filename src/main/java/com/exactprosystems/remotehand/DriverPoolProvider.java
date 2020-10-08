/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import org.openqa.selenium.WebDriver;

import com.exactprosystems.remotehand.sessions.SessionContext;

public interface DriverPoolProvider<T>
{
	T getDriverWrapper(SessionContext context) throws RhConfigurationException;

	void clearDriverPool();

	void closeDriver(String sessionId, WebDriver driver);
}
