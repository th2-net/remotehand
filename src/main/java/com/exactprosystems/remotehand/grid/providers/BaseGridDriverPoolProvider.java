/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.grid.providers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.exactprosystems.remotehand.DriverPoolProvider;
import com.exactprosystems.remotehand.RhConfigurationException;
import com.exactprosystems.remotehand.sessions.SessionContext;

public abstract class BaseGridDriverPoolProvider<T> implements DriverPoolProvider<T>
{
	protected final Map<String, T> driversPool = new ConcurrentHashMap<>();
	protected final Map<String, String> sessionTargetUrls;


	public BaseGridDriverPoolProvider(Map<String, String> sessionTargetUrls)
	{
		this.sessionTargetUrls = sessionTargetUrls;
	}


	@Override
	public void closeDriver(String sessionId, T driver)
	{
		sessionTargetUrls.remove(sessionId);
		driversPool.remove(sessionId);
	}


	protected abstract T createDriver(SessionContext context) throws RhConfigurationException;
}
