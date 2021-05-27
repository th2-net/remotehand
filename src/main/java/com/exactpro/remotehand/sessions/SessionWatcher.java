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

package com.exactpro.remotehand.sessions;

import com.exactpro.remotehand.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Watches for session expiration by inactivity time
 */
public class SessionWatcher implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(SessionWatcher.class);
	private static final long SESSION_EXPIRE_IN_MINUTES = Configuration.getInstance().getSessionExpire();
	private static final long SESSION_EXPIRE = SESSION_EXPIRE_IN_MINUTES * 60 * 1000;  //In configuration it is set in minutes, we need it in milliseconds 
	private static volatile SessionWatcher watcher = null;
	private final Map<SessionHandler, long[]> timeSessions = new HashMap<SessionHandler, long[]>();

	public static SessionWatcher getWatcher()
	{
		if (SESSION_EXPIRE_IN_MINUTES < 1)
		{
			logger.info("Session watcher is not created: session expiry time less or equal to 0");
			return null;
		}
		
		SessionWatcher localInstance = watcher;
		if (localInstance == null)
		{
			synchronized (SessionWatcher.class)
			{
				localInstance = watcher;
				if (localInstance == null)
					watcher = localInstance = new SessionWatcher();
			}
		}
		return localInstance;
	}

	@Override
	public void run()
	{
		logger.info("Session watcher thread is running. Session expire time: {} min", SESSION_EXPIRE_IN_MINUTES);

		while (true)
		{
			long remainTime = closeSessionIfTimeOver();
			try
			{
				Thread.sleep(remainTime);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
	}

	private long closeSessionIfTimeOver()
	{
		long timeToNextSessionEnd = SESSION_EXPIRE;
		synchronized (timeSessions)
		{
			Iterator<Map.Entry<SessionHandler, long[]>> iterator = timeSessions.entrySet().iterator();
			while (iterator.hasNext())
			{
				Map.Entry<SessionHandler, long[]> entry = iterator.next();
				long currentTime = System.currentTimeMillis();
				long sessionLastAction = entry.getValue()[1];
				long sessionEnd = sessionLastAction + SESSION_EXPIRE;
				long timeToEndSession = sessionEnd - currentTime;

				if (timeToEndSession < 0)
				{
					SessionHandler session = entry.getKey();
					if (session != null)
					{
						logger.warn("Session {} is inactive more than {} minutes. It will be closed due to timeout",
								session.getId(), SESSION_EXPIRE_IN_MINUTES);
						session.close();
						iterator.remove();
					}
				}
				else if (timeToNextSessionEnd > timeToEndSession)
					timeToNextSessionEnd = timeToEndSession;
			}
		}
		return timeToNextSessionEnd;
	}

	public static void watchSession(SessionHandler session)
	{
		if (watcher != null)
			watcher.addSession(session);
		else
			logger.trace("Unable to watch session: session watcher was not created");
	}
	
	private void addSession(SessionHandler session)
	{
		synchronized (timeSessions)
		{
			long start = System.currentTimeMillis();
			timeSessions.put(session, new long[]{start, start});
		}
	}
	
	public static void updateSession(SessionHandler session)
	{
		if (watcher != null)
			watcher.updateSessionTime(session);
		else
			logger.trace("Unable to update session: session watcher was not created");
	}
	
	private void updateSessionTime(SessionHandler session)
	{
		synchronized (timeSessions)
		{
			timeSessions.get(session)[1] = System.currentTimeMillis();
		}
	}
}
