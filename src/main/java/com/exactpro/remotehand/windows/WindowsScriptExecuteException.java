/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
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

import com.exactpro.remotehand.ScriptExecuteException;
import org.openqa.selenium.WebDriverException;

public class WindowsScriptExecuteException extends ScriptExecuteException {
	private WebDriverException driverException;


	public WindowsScriptExecuteException(String message) {
		super(message);
	}

	public WindowsScriptExecuteException(String message, WebDriverException driverException) {
		this(message);
		this.driverException = driverException;
	}


	public void setDriverException(WebDriverException driverException) {
		this.driverException = driverException;
	}

	public WebDriverException getDriverException() {
		return driverException;
	}

	public boolean hasDriverException() {
		return driverException != null;
	}
}
