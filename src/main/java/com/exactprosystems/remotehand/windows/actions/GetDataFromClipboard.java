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

package com.exactprosystems.remotehand.windows.actions;

import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.windows.WindowsAction;
import com.exactprosystems.remotehand.windows.WindowsDriverWrapper;
import com.exactprosystems.remotehand.windows.WindowsSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.Map;

public class GetDataFromClipboard extends WindowsAction {

	private static final Logger loggerInstance = LoggerFactory.getLogger(GetDataFromClipboard.class);
	
	@Override
	public String run(WindowsDriverWrapper driverWrapper, Map<String, String> params, WindowsSessionContext.CachedWebElements cachedElements) throws ScriptExecuteException {
		
		try {
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			return (String) systemClipboard.getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			throw new ScriptExecuteException("Cannot get data from clipboard", e);
		}
		
	}

	@Override
	protected Logger getLoggerInstance() {
		return loggerInstance;
	}
}
