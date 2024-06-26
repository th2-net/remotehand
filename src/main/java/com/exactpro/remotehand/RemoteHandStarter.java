/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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

package com.exactpro.remotehand;

import com.exactpro.remotehand.grid.GridRemoteHandManager;
import com.exactpro.remotehand.http.HTTPServerMode;
import com.exactpro.remotehand.http.HttpLogonHandler;
import com.exactpro.remotehand.rhdata.RhScriptResult;
import com.exactpro.remotehand.sessions.SessionContext;
import com.exactpro.remotehand.sessions.SessionWatcher;
import com.exactpro.remotehand.tcp.TcpClientMode;
import com.exactpro.remotehand.utils.RhUtils;
import com.exactpro.remotehand.web.WebDriverPoolProvider;
import com.exactpro.remotehand.web.WebRemoteHandManager;
import com.exactpro.remotehand.windows.WindowsDriverPoolProvider;
import com.exactpro.remotehand.windows.WindowsRemoteHandManager;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class RemoteHandStarter
{
	private static final Logger logger = LoggerFactory.getLogger(RemoteHandStarter.class);
	private static final String DEFAULT_VERSION = "local_build";
	public static final String CLEANUP_SCRIPT_FILE = "cleanup.csv";
	public static final String CONFIG_PARAM = "config";

	private static final String ENABLE_SERVER_MODE_PARAM = "enableServerMode",
			ENABLE_TCP_CLIENT_MODE_PARAM = "enableTcpClientMode",
			INPUT_NAME_PARAM = "inputName",
			OUTPUT_NAME_PARAM = "outputName",
			DYNAMIC_INPUT_NAME_PARAM = "dynamicInputName",
			INPUT_NAME_PARAMS = "inputParamsName",
			CONFIG_FILE_OPTIONS_PARAM = "configFileOption", 
			WINDOWS_OPTIONS_PARAM = "windowsMode",
			GRID_MODE_OPTIONS_PARAM = "grid";
	public static final String ENV_VARS_PARAM = "enableEnvVars",
			USE_WEBP_IMAGE_ENCODER_PARAM = "useWebpImageEncoder";


	public static void main(String[] args)
	{
		String version = getVersion();
		logger.info("Started RemoteHand " + version);

		Map<String, Option> optionMap = createOptionMap();
		Options options = createOptions(optionMap);
		CommandLine line = getCommandLine(args, options);

		if (line.hasOption(optionMap.get(GRID_MODE_OPTIONS_PARAM).getOpt()))
		{
			startGridMode(line);
			return;
		}

		IRemoteHandManager manager = null;
		if (line.hasOption(optionMap.get(WINDOWS_OPTIONS_PARAM).getOpt())) {
			manager = new WindowsRemoteHandManager(new WindowsDriverPoolProvider());
		} else {
			manager = new WebRemoteHandManager(new WebDriverPoolProvider());
		}
		
		IDriverManager webDriverManager = manager.getWebDriverManager();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(webDriverManager));

		boolean httpServerMode = line.hasOption(optionMap.get(ENABLE_SERVER_MODE_PARAM).getOpt()),
				tcpClientMode = line.hasOption(optionMap.get(ENABLE_TCP_CLIENT_MODE_PARAM).getOpt());

		String input = line.getOptionValue(optionMap.get(INPUT_NAME_PARAM).getOpt());
		String output = line.getOptionValue(optionMap.get(OUTPUT_NAME_PARAM).getOpt());

		if (Configuration.getInstance() == null) {
			manager.createConfiguration(line);
		}

		if (!httpServerMode && !tcpClientMode && (input == null || output == null))
		{
			printHelp(options);
			closeApp();
		}

		//In any mode RemoteHand handles requests from external application.
		//Requests flow should start with "logon" request, for which RemoteHand will assign a sessionID and return it to the application for further reference.

		if (httpServerMode)
		{
			startHttpServerMode(manager, webDriverManager);
		}
		else if (tcpClientMode)
		{
			startTcpClientMode(manager, webDriverManager, version);
		}
		else
		{
			startLocalMode(manager, webDriverManager, optionMap, line, input, output);
		}
	}

	private static void startGridMode(CommandLine commandLine)
	{
		GridRemoteHandManager gridManager = new GridRemoteHandManager();
		gridManager.createConfigurations(commandLine);
		HTTPServerMode.init(gridManager.createLogonHandler());
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(gridManager));
	}

	private static void startLocalMode(
			IRemoteHandManager manager,
			IDriverManager webDriverManager,
			Map<String, Option> optionMap,
			CommandLine line, String input,
			String output)
	{
		logger.info("Input file: '" + input + "'");
		logger.info("Output file: '" + output + "'");

		String dynInput = getDynamicInput(optionMap, line);
		String inputParams = getInputParams(optionMap, line);

		ActionsLauncher launcher = manager.createActionsLauncher(null);
		ScriptCompiler compiler = manager.createScriptCompiler();
		webDriverManager.initDriverPool();
		SessionContext sessionContext = getSessionContext(manager);


		processAllScriptsFromDirectory(input, output, inputParams, launcher, compiler, sessionContext);
		if (dynInput != null)
		{
			File dynFile = new File(dynInput);
			boolean firstWait = true;
			while (true)
			{
				if (dynFile.exists())
				{
					firstWait = true;
					processAllScriptsFromDirectory(dynInput, output, inputParams, launcher, compiler, sessionContext);

					if (dynFile.isFile())
					{
						if (!dynFile.delete())
							logger.warn("Could not delete dynamic input file '" + dynInput + "', commands from it will be executed again in 2 seconds");
					}
					else if (dynFile.isDirectory())
					{
						try
						{
							FileUtils.deleteDirectory(dynFile);
						}
						catch (IOException e)
						{
							logger.warn("Could not delete dynamic input directory '" + dynInput + "', commands from it will be executed again in 2 seconds");
						}
					}
				}
				else
				{
					if (firstWait)
					{
						firstWait = false;
						logger.info("Waiting for dynamic input file '" + dynInput + "'");
					}
				}

				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
					logger.info("Wait for next dynamic input interrupted");
				}
			}
		}
		manager.close(sessionContext);

		logger.info("Application stopped");
	}

	private static void startTcpClientMode(IRemoteHandManager manager, IDriverManager webDriverManager, String version)
	{
		//In TCP client mode RemoteHand connects to another application, telling that it is ready to serve.
		//Application sends logon request before sending commands to execute.
		//Once logon request is received, RemoteHand adds TcpSessionHandler to session pool.
		//Further requests from application must contain sessionID. Requests must have the following structure:

		//int requestType
		//int totalSize
		//int sessionIdLength
		//String sessionId
		//...other values depending on request type...

		//This way multiple simultaneous sessions can be handled by directing particular request to corresponding TcpSessionHandler.

		if (!TcpClientMode.init(version, manager))
			logger.info("Application stopped with error");
		else
			webDriverManager.initDriverPool();
		startSessionWatcher();
	}

	private static void startHttpServerMode(IRemoteHandManager manager, IDriverManager webDriverManager)
	{
		//In HTTP server mode RemoteHand waits for requests from external application.
		//Once logon request is received, RemoteHand adds HttpSessionHandler to HTTP server, binding it to URL equal to sessionID.
		//The application sends further requests to that URL directly.
		//This way multiple simultaneous sessions are handled by HTTP server nature.

		if (!HTTPServerMode.init((HttpLogonHandler)manager.createLogonHandler()))
			logger.info("Application stopped with error");
		else
			webDriverManager.initDriverPool();
		startSessionWatcher();
	}

	private static String getInputParams(Map<String, Option> optionMap, CommandLine line)
	{
		String inputParams = line.getOptionValue(optionMap.get(INPUT_NAME_PARAMS).getOpt());
		if (inputParams != null)
		{
			logger.info("Input params file: '" + inputParams + "'");
		}

		return inputParams;
	}

	private static String getDynamicInput(Map<String, Option> optionMap, CommandLine line)
	{
		String dynInput = line.getOptionValue(optionMap.get(DYNAMIC_INPUT_NAME_PARAM).getOpt());
		if (dynInput != null)
		{
			logger.info("Dynamic input file: '" + dynInput + "'");
		}

		return dynInput;
	}

	private static SessionContext getSessionContext(IRemoteHandManager manager)
	{
		SessionContext sessionContext = null;

		try
		{
			sessionContext = manager.createSessionContext(RhUtils.SESSION_FOR_FILE_MODE);
		}
		catch (RhConfigurationException e)
		{
			logger.error("Unable to initialize application.", e);
			closeApp();
		}

		return sessionContext;
	}

	private static CommandLine getCommandLine(String[] args, Options options)
	{
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;

		try
		{
			// parse the command line arguments
			line = parser.parse(options, args);
		}
		catch (ParseException exp)
		{
			// oops, something went wrong
			System.out.println("Incorrect parameters: " + exp.getMessage());
			closeApp();
		}

		return line;
	}

	private static String getVersion()
	{
		String version = null;
		try
		{
			Manifest mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream(JarFile.MANIFEST_NAME));
			version = mf.getMainAttributes().getValue("Implementation-Version");
		}
		catch (Exception e)
		{
			logger.warn("Error while reading MANIFEST.MF file. Using '" + DEFAULT_VERSION + "' as version value", e);
		}

		if (version == null)
		{
			version = DEFAULT_VERSION;
		}

		return version;
	}

	@SuppressWarnings("static-access")
	private static Map<String, Option> createOptionMap()
	{
		Map<String, Option> optionMap = new HashMap<>(11);

		Option enableServerMode = OptionBuilder
				.isRequired(false)
				.withDescription("Work in HTTP Server mode")
				.create("httpserver");
		optionMap.put(ENABLE_SERVER_MODE_PARAM, enableServerMode);

		Option enableTcpClientMode = OptionBuilder
				.isRequired(false)
				.withDescription("Work in TCP/IP client mode")
				.create("tcpclient");
		optionMap.put(ENABLE_TCP_CLIENT_MODE_PARAM, enableTcpClientMode);

		Option inputName = OptionBuilder
				.withArgName("file")
				.hasArg()
				.withDescription("Specify input path name.")
				.create("input");
		optionMap.put(INPUT_NAME_PARAM, inputName);

		Option outputName = OptionBuilder
				.withArgName("file")
				.hasArg()
				.withDescription("Specify output path name.")
				.create("output");
		optionMap.put(OUTPUT_NAME_PARAM, outputName);

		Option dynamicInputName = OptionBuilder
				.withArgName("file")
				.hasArg()
				.withDescription("Dynamically added input file with further commands")
				.create("dynamicinput");
		optionMap.put(DYNAMIC_INPUT_NAME_PARAM, dynamicInputName);

		Option inputParamsName = OptionBuilder
				.withArgName("file")
				.hasArg()
				.withDescription("Specify input parameters path name.")
				.create("inputparams");
		optionMap.put(INPUT_NAME_PARAMS, inputParamsName);

		Option configFileOption = OptionBuilder
				.isRequired(false)
				.withArgName("file")
				.hasArg()
				.withDescription("Specify configuration file")
				.create(CONFIG_PARAM);
		optionMap.put(CONFIG_FILE_OPTIONS_PARAM, configFileOption);

		Option windowsMode = OptionBuilder
				.isRequired(false)
				.withDescription("Windows mode. Works with Windows Application Driver")
				.create("windowsMode");
		optionMap.put(WINDOWS_OPTIONS_PARAM, windowsMode);

		Option gridMode = OptionBuilder
				.isRequired(false)
				.withDescription("Work in Grid mode using remote Windows Application Driver and remote Web Driver")
				.create(GRID_MODE_OPTIONS_PARAM);
		optionMap.put(GRID_MODE_OPTIONS_PARAM, gridMode);

		Option envVarsMode = OptionBuilder
				.isRequired(false)
				.withDescription("Enables environment variables. Example: to option SessionExpire (in ini file) " + 
						"option will be RH_SESSION_EXPIRE").create(ENV_VARS_PARAM);
		optionMap.put(ENV_VARS_PARAM, envVarsMode);

		Option useWebpImageEncoder = OptionBuilder
				.isRequired(false)
				.withDescription("Enables WebP image encoder").create(USE_WEBP_IMAGE_ENCODER_PARAM);
		optionMap.put(USE_WEBP_IMAGE_ENCODER_PARAM, useWebpImageEncoder);

		return optionMap;
	}

	private static Options createOptions(Map<String, Option> optionMap)
	{
		Options options = new Options();

		for (Option option : optionMap.values())
		{
			options.addOption(option);
		}

		return options;
	}

	private static void processAllScriptsFromDirectory(
			String input,
			String output,
			String inputParams,
			ActionsLauncher launcher,
			ScriptCompiler compiler,
			SessionContext sessionContext
	) {
		File inputFile = new File(input);
		File outputFile = new File(output);
		if (!inputFile.exists()) {
			logger.error("Input file or directory '" + input + "' does not exist");
			closeApp();
		}

		File inputParamsFile = null;
		if (inputParams != null)
			inputParamsFile = new File(inputParams);

		File[] fileList = inputFile.isDirectory() ?
				inputFile.listFiles(file -> file.isFile() && file.getName().endsWith(".csv")) :
				new File[] { inputFile };

		for (File scriptFile : fileList) {
			try {
				final List<Action> actions = compiler.build(scriptFile, inputParamsFile, sessionContext);
				RhScriptResult result = launcher.runActions(actions, sessionContext);

				TextFileWriter.getInstance().setContent(resultToText(result));
				TextFileWriter.getInstance().writeFile(outputFile);
			} catch (Exception ex) {
				logger.error("An error occurred", ex);
				cleanUpAfterFail(launcher, compiler, sessionContext);
			}
		}
	}

	private static void printHelp(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar remotehand.jar", options);
	}

	private static void closeApp()
	{
		logger.info("Application stopped");
		System.exit(1);
	}
	
	private static void startSessionWatcher()
	{
		SessionWatcher watcher;
		if ((watcher = SessionWatcher.getWatcher()) != null)
			new Thread(watcher).start();
		else
			logger.warn("Session watcher thread is not running");
	}
	
	private static void cleanUpAfterFail(ActionsLauncher launcher, ScriptCompiler compiler, SessionContext context)
	{
		File file = new File(CLEANUP_SCRIPT_FILE);
		if (file.exists())
		{
			try
			{
				List<Action> actions = compiler.build(file, null, context);
				launcher.runActions(actions, context);
			}
			catch (Exception e)
			{
				logger.error("An error occurred while running cleanup script.", e);
			}
		}
		else 
			logger.info("Cleanup script doesn't exist.");
	}
	
	private static String resultToText(RhScriptResult result)
	{
		StringBuilder sb = new StringBuilder();
		for (ActionResult line : result.getActionResults())
		{
			sb.append(line).append("\r\n");
		}
		for (ActionResult line : result.getEncodedOutput())
		{
			sb.append(line).append("\r\n");
		}
		return sb.toString();
	}

	private static class ShutdownHook extends Thread
	{
		private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

		private final IDriverManager manager;

		public ShutdownHook(IDriverManager manager)
		{
			this.manager = manager;
			setName("ShutdownHook");
		}

		@Override
		public void run()
		{
			logger.info("Driver pool clearing...");
			manager.clearDriverPool();
		}

	}
}
