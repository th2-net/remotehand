/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.sessions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.exactprosystems.remotehand.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		if (SESSION_EXPIRE == 0)
		{
			logger.info("Session watcher is not executed because session expiry time equals 0");
			return;
		}

		logger.info("Session watcher is executed. SessionExpire (min) = " + SESSION_EXPIRE_IN_MINUTES);

		while (true)
		{
			long remainTime = closeSessionIfTimeOver();
			try
			{
				Thread.sleep(remainTime);
			}
			catch (InterruptedException e)
			{
				// do nothing
			}
		}
	}

	public long closeSessionIfTimeOver()
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
						logger.warn("Session " + session.getId() + " is inactive more than " + 
								SESSION_EXPIRE_IN_MINUTES + " minutes. " +
								"It will be closed due to timeout");
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

	public void addSession(SessionHandler session)
	{
		synchronized (timeSessions)
		{
			long start = System.currentTimeMillis();
			timeSessions.put(session, new long[]{start, start});
		}
	}
	
	public void updateSession(SessionHandler session)
	{
		synchronized (timeSessions)
		{
			timeSessions.get(session)[1] = System.currentTimeMillis();
		}
	}
}