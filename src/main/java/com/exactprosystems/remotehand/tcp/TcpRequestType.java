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

package com.exactprosystems.remotehand.tcp;

public enum TcpRequestType
{
	LOGON(0),
	SCRIPT(1),
	STATUS(2),
	FILE(10),
	DOWNLOAD(20),
	LOGOUT(99);
	
	private final int code;
	
	private TcpRequestType(int code)
	{
		this.code = code;
	}
	
	public int getCode()
	{
		return code;
	}
	
	
	public static TcpRequestType byCode(int code)
	{
		for (TcpRequestType rt : values())
		{
			if (rt.getCode() == code)
				return rt;
		}
		return SCRIPT;  //This makes protocol less strict
	}
}
