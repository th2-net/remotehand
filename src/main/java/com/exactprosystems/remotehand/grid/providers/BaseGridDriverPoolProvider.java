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
