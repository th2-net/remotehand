package com.exactprosystems.remotehand.http;

public class ThreadBusyException extends Exception
{
	private static final long serialVersionUID = 3L;

	public ThreadBusyException(String message)
	{
		super(message);
	}
}
