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

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.exactprosystems.remotehand.requests.DownloadRequest;
import com.exactprosystems.remotehand.requests.ExecutionRequest;
import com.exactprosystems.remotehand.requests.ExecutionStatusRequest;
import com.exactprosystems.remotehand.requests.FileUploadRequest;
import com.exactprosystems.remotehand.requests.LogonRequest;
import com.exactprosystems.remotehand.requests.LogoutRequest;
import com.exactprosystems.remotehand.requests.RhRequest;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

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