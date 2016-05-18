package com.exactprosystems.remotehand.web.actions;

public class SendKeysToActive extends SendKeys
{
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
}
