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

package com.exactprosystems.remotehand.windows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exactprosystems.remotehand.ScriptExecuteException;

import static java.lang.String.format;

public class WindowsManager
{
	private static final Logger log = LoggerFactory.getLogger(WindowsManager.class);
	
	// <Window Name, <RH session ID, NativeWindowHandle>>
	private final Map<String, WindowInfo> windowInfoByName = new HashMap<>();
	
	
	public synchronized String findWindowForSession(String name, String rhSessionId, Set<String> allNativeHandles)
			throws ScriptExecuteException
	{
		WindowInfo info = windowInfoByName.get(name);
		if (info == null)
		{
			info = new WindowInfo(name);
			windowInfoByName.put(name, info);
		}
		
		return info.findWindowForSession(rhSessionId, allNativeHandles);
	}
	
	public synchronized void removeSession(String rhSessionId)
	{
		for (WindowInfo info : windowInfoByName.values())
		{
			info.removeSession(rhSessionId);
		}
	}
	
	
	private static class WindowInfo
	{
		private final String name;
		// <RH session ID, NativeWindowHandle>
		private final Map<String, String> nativeHandleBySessionId = new HashMap<>();
		
		private WindowInfo(String name)
		{
			this.name = name;
		}
		
		private String findWindowForSession(String rhSessionId, Set<String> allNativeHandles)
				throws ScriptExecuteException
		{
			log.debug("Trying to find free window '{}' for session '{}'. Busy NativeWindowHandles by session id: {}.",
					name, rhSessionId, nativeHandleBySessionId);
			
			String savedHandle = nativeHandleBySessionId.get(rhSessionId);
			if ((savedHandle != null) && allNativeHandles.contains(savedHandle))
			{
				log.debug("Window '{}' [{}] has been previously opened by session '{}.", name, savedHandle, rhSessionId);
				return savedHandle;
			}
			
			Set<String> freeHandles = new HashSet<>(allNativeHandles);
			freeHandles.removeAll(nativeHandleBySessionId.values());
			
			if (freeHandles.isEmpty())
				throw new ScriptExecuteException(format("Unable to find free window '%s' for session '%s'. " +
						"All windows are used by other sessions.", name, rhSessionId));
			
			if (freeHandles.size() > 1)
				throw new ScriptExecuteException(format("Unable to select free window '%s' for session '%s'. More " +
						"than one free window have been found: %s.", name, rhSessionId, freeHandles));
			
			String freeHandle = freeHandles.iterator().next();
			nativeHandleBySessionId.put(rhSessionId, freeHandle);
			
			if (savedHandle != null)
				log.debug("Saved NativeWindowHandle for window '{}' for session '{}' has been replaced " +
						"from '{}' to '{}'. Probably previous window was closed.", 
						name, rhSessionId, savedHandle, freeHandle);
			
			return freeHandle;
		}
		
		private void removeSession(String rhSessionId)
		{
			nativeHandleBySessionId.remove(rhSessionId);
		}
	}
}
