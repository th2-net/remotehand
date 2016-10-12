package com.exactprosystems.remotehand.web.actions;

import org.apache.log4j.Logger;

public class SendKeysToActive extends SendKeys
{
	private static final Logger logger = Logger.getLogger(SendKeysToActive.class);
	
	@Override
	public boolean isNeedLocator()
	{
		return false;
	}

	@Override
	public boolean isCanWait()
	{
		return false;
	}

	@Override
	protected Logger getLogger()
	{
		return logger;
	}
}
