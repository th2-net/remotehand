/*
 * Copyright 2020-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactpro.remotehand.tcp;

import com.exactpro.remotehand.sessions.SessionExchange;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
