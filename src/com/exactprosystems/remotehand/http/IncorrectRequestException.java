package com.exactprosystems.remotehand.http;

public class IncorrectRequestException extends Exception
{
	private static final long serialVersionUID = 4L;

	public IncorrectRequestException(String message)
	{
		super(message);
	}
}