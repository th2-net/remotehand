/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.tcp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.exactprosystems.remotehand.sessions.SessionExchange;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

public class TcpSessionExchange implements SessionExchange
{
	private static int MANDATORY_SIZE = 4+4;  //4 for code, 4 for length
	private final ChannelHandlerContext ctx;
	
	public TcpSessionExchange(ChannelHandlerContext ctx)
	{
		this.ctx = ctx;
	}
	
	@Override
	public void sendResponse(int code, String message) throws IOException
	{
		byte[] bytes = message.getBytes(CharsetUtil.UTF_8);
		ByteBuf buffer = ctx.alloc().buffer(MANDATORY_SIZE+bytes.length);
		buffer.writeInt(code);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
		ctx.writeAndFlush(buffer);
	}
	
	@Override
	public void sendFile(int code, File f, String type, String name) throws IOException
	{
		//FIXME: probably, we need to make protocol more complex to handle files of any size. Also it would be better to avoid having the whole file in memory
		long size = f.length();
		if (size+MANDATORY_SIZE >= Integer.MAX_VALUE)
			throw new IOException("File size too big ("+size+")");
		int intSize = (int)size;
		ByteBuf buffer = ctx.alloc().buffer(MANDATORY_SIZE+intSize);
		buffer.writeInt(code);
		buffer.writeInt(intSize);
		buffer.writeBytes(Files.readAllBytes(f.toPath()));
		ctx.writeAndFlush(buffer);
	}
	
	@Override
	public String getRemoteAddress()
	{
		return ctx.channel().remoteAddress().toString();
	}
}