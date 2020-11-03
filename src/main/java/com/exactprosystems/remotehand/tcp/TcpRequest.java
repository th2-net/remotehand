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

import com.exactprosystems.remotehand.requests.RhRequest;

public class TcpRequest
{
	private final String sessionId;
	private final RhRequest request;
	
	public TcpRequest(String sessionId, RhRequest request)
	{
		this.sessionId = sessionId;
		this.request = request;
	}
	
	
	public String getSessionId()
	{
		return sessionId;
	}
	
	public RhRequest getRequest()
	{
		return request;
	}
}
