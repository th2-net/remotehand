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

import com.exactpro.remotehand.IRemoteHandManager;

import java.util.HashMap;
import java.util.Map;

public class TcpSessions
{
	private static TcpSessions instance;
	
	private final Map<String, TcpSessionHandler> sessions = new HashMap<>();
	
	public static void init()
	{
		if (instance != null)
			return;
		instance = new TcpSessions();
	}
	
	public static TcpSessions getInstance()
	{
		return instance;
	}
	
	public void addSession(String sessionId, IRemoteHandManager manager)
	{
		sessions.put(sessionId, new TcpSessionHandler(sessionId, manager));
	}
	
	public TcpSessionHandler getSession(String sessionId)
	{
		return sessions.get(sessionId);
	}
	
	public void removeSession(String sessionId)
	{
		sessions.remove(sessionId);
	}
}
