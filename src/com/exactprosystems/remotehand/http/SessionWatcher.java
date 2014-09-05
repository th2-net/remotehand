package com.exactprosystems.remotehand.http;

import java.util.HashMap;
import java.util.Map;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.Logger;

public class SessionWatcher implements Runnable
{
	private static final Logger logger = Logger.getLogger();
	private static final int SESSION_EXPIRY_TIME = Configuration.getInstance().getSessionExpiryTimeMs();
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
		if (SESSION_EXPIRY_TIME == 0)
		{
			logger.info("Session watcher is not executed due session expiry time equals 0");
			return;
		}

		logger.info("Session watcher is executed. " + Configuration.sessionExpiryTime + "=" + SESSION_EXPIRY_TIME);

		while (HTTPServer.getServer() != null)
		{
			final Long remainTime = closeHTTPSessionIfTimeOver();

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

	public long closeHTTPSessionIfTimeOver()
	{
		long timeToNextSessionEnd = SESSION_EXPIRY_TIME;

		for (SessionHandler session : timeSessions.keySet())
		{
			final Long currentTime = System.currentTimeMillis();
			final Long sessionLastAction = timeSessions.get(session)[1];
			final Long sessionEnd = sessionLastAction + SESSION_EXPIRY_TIME;
			final Long timeToEndSession = sessionEnd - currentTime;

			if (timeToEndSession < 0)
			{
				if (session != null)
				{
					logger.warn("Session " + session.getId() + " is inactive more than "+SESSION_EXPIRY_TIME+". It will be closed due to timeout");
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
