/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.requests;

public class FileUploadRequest implements RhRequest
{
	private final String fileName;
	private final byte[] contents;
	
	public FileUploadRequest(String fileName, byte[] contents)
	{
		this.fileName = fileName;
		this.contents = contents;
	}
	
	
	public String getFileName()
	{
		return fileName;
	}
	
	public byte[] getContents()
	{
		return contents;
	}
}