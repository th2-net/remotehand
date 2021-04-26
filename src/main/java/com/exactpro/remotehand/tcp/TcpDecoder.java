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

import com.exactpro.remotehand.requests.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class TcpDecoder extends ByteToMessageDecoder
{
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
	{
		if (in.readableBytes() < 4)
			return;
		
		in.markReaderIndex();
		int type = in.readInt();
		TcpRequestType requestType = TcpRequestType.byCode(type);
		if (requestType == TcpRequestType.LOGON)
		{
			out.add(new TcpRequest(null, new LogonRequest()));
			return;
		}
		
		//FIXME: here is a weakness as we assume payloadLength be not less than actual data length.
		//If actual data is longer than payloadLength, we can be in trouble
		int payloadLength = in.readInt();
		if (in.readableBytes() < payloadLength)
		{
			in.resetReaderIndex();
			return;
		}
		
		String sessionId = requestType != TcpRequestType.DOWNLOAD ? readString(in) : null;
		TcpRequest request = new TcpRequest(sessionId, readRhRequest(in, requestType));
		out.add(request);
	}
	
	
	private byte[] readBytes(ByteBuf in, int length) throws UnsupportedEncodingException
	{
		byte[] bytes = new byte[length];
		in.readBytes(bytes);
		return bytes;
	}
	
	private byte[] readBytes(ByteBuf in) throws UnsupportedEncodingException
	{
		int length = in.readInt();
		return readBytes(in, length);
	}
	
	private String readString(ByteBuf in, int length) throws UnsupportedEncodingException
	{
		byte[] bytes = readBytes(in, length);
		return new String(bytes, CharsetUtil.UTF_8);
	}
	
	private String readString(ByteBuf in) throws UnsupportedEncodingException
	{
		int length = in.readInt();
		return readString(in, length);
	}
	
	
	private RhRequest readRhRequest(ByteBuf in, TcpRequestType type) throws Exception
	{
		switch (type)
		{
		case SCRIPT : 
			String script = readString(in);
			return new ExecutionRequest(script);
		case STATUS : return new ExecutionStatusRequest();
		case FILE :
			String fileName = readString(in);
			byte[] contents = readBytes(in);
			return new FileUploadRequest(fileName, contents);
		case DOWNLOAD : 
			String fileType = readString(in),
					id = readString(in);
			return new DownloadRequest(fileType, id);
		case LOGOUT : return new LogoutRequest();
		default : 
			//Logon request type is already handled in caller method. Other types should be considered as not supported if not handled above
			throw new Exception("Unsupported request type '"+type+"'");
		}
	}
}
