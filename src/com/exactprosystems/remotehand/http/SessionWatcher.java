package com.exactprosystems.remotehand.http;

import java.util.HashMap;
import java.util.Map;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.Logger;

public class SessionWatcher implements Runnable
{
	private Logger logger = Logger.getLogger();

	private static final int SESSION_EXPIRY_TIME = Configuration.getInstance().getSessionExpiryTimeMs();

	private static volatile SessionWatcher watcher = null;

	private static volatile Map<SessionHandler, Long> timeSessions = new HashMap<SessionHandler, Long>();

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
			final Long sessionStartAt = timeSessions.get(session);
			final Long sessionEndAt = sessionStartAt + SESSION_EXPIRY_TIME;
			final Long timeToEndSession = sessionEndAt - currentTime;

			if (sessionEndAt < currentTime)
			{
				if (session != null)
				{
					logger.warn("Working time for session " + session.getId() + " is up. It will be closed");
					session.close();
				}
				timeSessions.remove(session);
			}
			else if (timeToNextSessionEnd > timeToEndSession)
				timeToNextSessionEnd = timeToEndSession;
		}
		return timeToNextSessionEnd;
	}

	public void addSession(SessionHandler session)
	{
		timeSessions.put(session, System.currentTimeMillis());
	}

	public void removeSession(SessionHandler session)
	{
		timeSessions.remove(session);
	}
}
