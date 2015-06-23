////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.http;

import java.util.HashMap;
import java.util.Map;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.Logger;

public class SessionWatcher implements Runnable
{
	private static final Logger logger = Logger.getLogger();
	private static final long SESSION_EXPIRE = Configuration.getInstance().getSessionExpire()*60*1000;  //In configuration it is set in minutes, we need it in milliseconds 
	private static volatile SessionWatcher watcher = null;
	private static volatile Map<SessionHandler, long[]> timeSessions = new HashMap<SessionHandler, long[]>();

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
			logger.info("Session watcher is not executed due to session expiry time equals 0");
			return;
		}

		logger.info("Session watcher is executed. " + Configuration.PARAM_SESSIONEXPIRE + "=" + SESSION_EXPIRE);

		while (HTTPServer.getServer() != null)
		{
			long remainTime = closeHttpSessionIfTimeOver();
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

	public long closeHttpSessionIfTimeOver()
	{
		long timeToNextSessionEnd = SESSION_EXPIRE;

		for (SessionHandler session : timeSessions.keySet())
		{
			long currentTime = System.currentTimeMillis();
			long sessionLastAction = timeSessions.get(session)[1];
			long sessionEnd = sessionLastAction + SESSION_EXPIRE;
			long timeToEndSession = sessionEnd - currentTime;

			if (timeToEndSession < 0)
			{
				if (session != null)
				{
					logger.warn("Session " + session.getId() + " is inactive more than "+SESSION_EXPIRE+" milliseconds. It will be closed due to timeout");
					session.close();
				}
			}
			else if (timeToNextSessionEnd > timeToEndSession)
				timeToNextSessionEnd = timeToEndSession;
		}
		return timeToNextSessionEnd;
	}

	public void addSession(SessionHandler session)
	{
		long start = System.currentTimeMillis();
		timeSessions.put(session, new long[] { start, start });
	}

	public void removeSession(SessionHandler session)
	{
		timeSessions.remove(session);
	}
	
	public void updateSession(SessionHandler session)
	{
		synchronized (session)
		{
			timeSessions.get(session)[1] = System.currentTimeMillis();
		}
	}
}
