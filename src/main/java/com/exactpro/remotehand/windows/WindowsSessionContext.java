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

package com.exactpro.remotehand.windows;

import com.exactpro.remotehand.sessions.SessionContext;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowsSessionContext extends SessionContext {
	
	private WindowsDriverWrapper currentDriver;
	private URL winApiDriverURL;
	private final Map<String, Object> mvelVars;
	private final CachedWebElements cachedObjects;
	private final WindowsManager windowsManager;

	public WindowsSessionContext(String sessionId, WindowsManager windowsManager) {
		super(sessionId);
		this.mvelVars = new HashMap<>();
		this.cachedObjects = new CachedWebElements();
		this.windowsManager = windowsManager;
	}

	public WindowsDriverWrapper getCurrentDriver() {
		return currentDriver;
	}

	public void setCurrentDriver(WindowsDriverWrapper currentDriver) {
		this.currentDriver = currentDriver;
	}

	public URL getWinApiDriverURL() {
		return winApiDriverURL;
	}

	public void setWinApiDriverURL(URL winApiDriverURL) {
		this.winApiDriverURL = winApiDriverURL;
	}

	public Map<String, Object> getMvelVars() {
		return mvelVars;
	}

	public CachedWebElements getCachedObjects() {
		return cachedObjects;
	}

	public WindowsManager getWindowsManager()
	{
		return windowsManager;
	}

	public static class CachedWebElements {
		private final Map<String, WebElement> cachedObjects;
		private final Map<String, List<WebElement>> cachedLists;

		public CachedWebElements() {
			this.cachedObjects = new HashMap<>();
			this.cachedLists = new HashMap<>();
		}
		
		public void storeWebElement(String id, WebElement webElement) {
			this.cachedObjects.put(id, webElement);
		}

		public void removeElements(String id) {
			if (StringUtils.isNotEmpty(id)) {
				this.cachedObjects.remove(id);
				this.cachedLists.remove(id);
			}
		}

		public void storeWebElementList(String id, List<WebElement> webElement) {
			this.cachedLists.put(id, webElement);
		}

		public WebElement getWebElement(String id) {
			return this.cachedObjects.get(id);
		}

		public List<WebElement> getWebElementList(String id) {
			return this.cachedLists.get(id);
		}
	}
}
