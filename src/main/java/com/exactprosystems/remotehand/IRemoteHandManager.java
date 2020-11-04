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

package com.exactprosystems.remotehand;

import com.exactprosystems.remotehand.sessions.LogonHandler;
import com.exactprosystems.remotehand.sessions.SessionContext;

import org.apache.commons.cli.CommandLine;

import java.util.Map;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public interface IRemoteHandManager {

	ScriptCompiler createScriptCompiler ();

	Configuration createConfiguration(CommandLine commandLine);
	Configuration createConfiguration(CommandLine commandLine, Map<String, String> options);

	ActionsLauncher createActionsLauncher(ScriptProcessorThread thread);
	
	SessionContext createSessionContext(String sessionId) throws RhConfigurationException;

	LogonHandler createLogonHandler();

	void close(SessionContext sessionContext);

	IDriverManager getWebDriverManager();

	RemoteManagerType getManagerType();
}
