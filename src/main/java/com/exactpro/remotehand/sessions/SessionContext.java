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

package com.exactpro.remotehand.sessions;

import java.util.HashMap;
import java.util.Map;

public abstract class SessionContext
{
	private final String sessionId;
	private final Map<String, Object> contextData;
	
	public SessionContext(String sessionId)
	{
		this.sessionId = sessionId;
		this.contextData = new HashMap<String, Object>();
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public Map<String, Object> getContextData() {
		return contextData;
	}
}
