/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand;

import com.exactprosystems.clearth.connectivity.data.rhdata.RhScriptResult;
import com.exactprosystems.remotehand.http.HTTPServerMode;
import com.exactprosystems.remotehand.sessions.SessionContext;
import com.exactprosystems.remotehand.sessions.SessionWatcher;
import com.exactprosystems.remotehand.tcp.TcpClientMode;
import com.exactprosystems.remotehand.web.WebDriverManager;
import com.exactprosystems.remotehand.web.WebRemoteHandManager;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Starter
{
	private static final Logger logger = LoggerFactory.getLogger(Starter.class);
	private static final String DEFAULT_VERSION = "local_build";
	
	public static final String CLEANUP_SCRIPT_FILE = "cleanup.csv";
	public static final String CONFIG_PARAM = "config";

	@SuppressWarnings("static-access")
	public static void main(String[] args, IRemoteHandManager manager)
	{
		WebDriverManager webDriverManager = ((WebRemoteHandManager) manager).getWebDriverManager();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(webDriverManager));

		Manifest mf = null;
		String version = null;
		try
		{
			mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream(JarFile.MANIFEST_NAME));
			version = mf.getMainAttributes().getValue("Implementation-Version");
		}
		catch (Exception e)
		{
			logger.warn("Error while reading MANIFEST.MF file. Using '" + DEFAULT_VERSION + "' as version value", e);
		}

		if (version == null) {
			version = DEFAULT_VERSION;
		}
		
		PropertyConfigurator.configureAndWatch("log4j.properties");
		
		logger.info("Started RemoteHand "+version);


		Option enableServerMode = OptionBuilder.isRequired(false).withDescription("Work in HTTP Server mode").create("httpserver"),
				enableTcpClientMode = OptionBuilder.isRequired(false).withDescription("Work in TCP/IP client mode").create("tcpclient"),
				inputName = OptionBuilder.withArgName("file").hasArg().withDescription("Specify input path name.").create("input"),
				outputName = OptionBuilder.withArgName("file").hasArg().withDescription("Specify output path name.").create("output"),
				dynamicInputName = OptionBuilder.withArgName("file").hasArg().withDescription("Dynamically added input file with further commands").create("dynamicinput"),
				inputParamsName = OptionBuilder.withArgName("file").hasArg().withDescription("Specify input parameters path name.").create("inputparams");
		
		Option configFileOption = OptionBuilder.isRequired(false).withArgName("file").hasArg()
				.withDescription("Specify configuration file").create(CONFIG_PARAM);

		Options options = new Options();
		options.addOption(enableServerMode);
		options.addOption(enableTcpClientMode);
		options.addOption(inputName);
		options.addOption(outputName);
		options.addOption(dynamicInputName);
		options.addOption(inputParamsName);
		options.addOption(configFileOption);

		for (Option additional : manager.getAdditionalOptions()) {
			options.addOption(additional);
		}

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

		boolean httpServerMode = line.hasOption(enableServerMode.getOpt()),
				tcpClientMode = line.hasOption(enableTcpClientMode.getOpt());
		String input = line.getOptionValue(inputName.getOpt());
		String output = line.getOptionValue(outputName.getOpt());
		String dynInput = line.getOptionValue(dynamicInputName.getOpt());
		String inputParams = line.getOptionValue(inputParamsName.getOpt());

		if (Configuration.getInstance() == null) {
			manager.createConfiguration(line);
		}

		if (!httpServerMode && !tcpClientMode && (input == null || output == null))
		{
			printHelp(options);
			closeApp();
		}

		//In any mode RemoteHand handles requests from ClearTH.
		//Requests flow should start with "logon" request, for which RemoteHand will assign a sessionID and return it to ClearTH for further reference.

		if (httpServerMode)
		{
			//In HTTP server mode RemoteHand waits for requests from ClearTH. 
			//Once logon request is received, RemoteHand adds HttpSessionHandler to HTTP server, binding it to URL equal to sessionID.
			//ClearTH sends further requests to that URL directly.
			//This way multiple simultaneous sessions are handled by HTTP server nature.
			
			if (!HTTPServerMode.init(manager.createLogonHandler()))
				logger.info("Application stopped with error");
			else
				webDriverManager.initDriverPool();
			startSessionWatcher();
		}
		else if (tcpClientMode)
		{
			//In TCP client mode RemoteHand connects to ClearTH, telling that it is ready to serve.
			//ClearTH sends logon request before sending commands to execute.
			//Once logon request is received, RemoteHand adds TcpSessionHandler to session pool. 
			//Further requests from ClearTH must contain sessionID. Requests must have the following structure:
			
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
		else
		{
			logger.info("Input file: '" + input + "'");
			logger.info("Output file: '" + output + "'");
			if (dynInput != null)
				logger.info("Dynamic input file: '" + dynInput + "'");
			if (inputParams != null)
				logger.info("Input params file: '" + inputParams + "'");

			ActionsLauncher launcher = manager.createActionsLauncher(null);
			ScriptCompiler compiler = manager.createScriptCompiler();

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

			webDriverManager.initDriverPool();

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
	}

	private static void processAllScriptsFromDirectory(String input, String output, String inputParams,
			ActionsLauncher launcher, ScriptCompiler compiler, SessionContext sessionContext)
	{
		File[] fileList;
		File inputFile = new File(input), 
				outputFile = new File(output);
		if (!inputFile.exists())
		{
			logger.error("Input file or directory '" + input + "' does not exist");
			closeApp();
		}

		File inputParamsFile = null;
		if (inputParams != null)
			inputParamsFile = new File(inputParams);

		if (inputFile.isDirectory())
		{
			fileList = inputFile.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					return file.isFile() && file.getName().endsWith(".csv");
				}
			});
		}
		else
			fileList = new File[] { inputFile };

		for (File scriptFile : fileList)
		{
			try
			{
				final List<Action> actions = compiler.build(scriptFile, inputParamsFile, sessionContext);
				RhScriptResult result = launcher.runActions(actions, sessionContext);

				TextFileWriter.getInstance().setContent(resultToText(result));
				TextFileWriter.getInstance().writeFile(outputFile);
			}
			catch (Exception ex)
			{
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
			logger.info("Thread watcher is not running");
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
		for (String line : result.getTextOutput())
		{
			sb.append(line).append("\r\n");
		}
		for (String line : result.getEncodedOutput())
		{
			sb.append(line).append("\r\n");
		}
		return sb.toString();
	}

	private static class ShutdownHook extends Thread
	{
		private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

		private final WebDriverManager manager;

		public ShutdownHook(WebDriverManager manager)
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
