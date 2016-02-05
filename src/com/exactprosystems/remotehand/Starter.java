////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.exactprosystems.remotehand.http.LoginHandler;
import com.exactprosystems.remotehand.web.WebScriptAction;
import com.exactprosystems.remotehand.web.WebScriptCompiler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.exactprosystems.remotehand.http.HTTPServer;
import com.exactprosystems.remotehand.http.SessionWatcher;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Starter
{
	private static final Logger logger = Logger.getLogger(Starter.class);

	@SuppressWarnings("static-access")
	public static void main(String[] args, IRemoteHandManager manager)
	{
		Manifest mf = null;
		String version = null;
		try
		{
			mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream(JarFile.MANIFEST_NAME));
			version = mf.getMainAttributes().getValue("Implementation-Version");
		}
		catch (Exception e)
		{
			version = "local_build";
			logger.warn("Error while reading MANIFEST.MF file. Using '" + version + "' as version value", e);
		}
		
		PropertyConfigurator.configureAndWatch("log4j.properties");
		
		logger.info("Started RemoteHand "+version);

		manager.createConfiguration();

		Option enableServerMode = OptionBuilder.isRequired(false).withDescription("Work in HTTP Server mode").create("httpserver"),
				inputName = OptionBuilder.withArgName("file").hasArg().withDescription("Specify input path name.").create("input"),
				outputName = OptionBuilder.withArgName("file").hasArg().withDescription("Specify output path name.").create("output"),
				dynamicInputName = OptionBuilder.withArgName("file").hasArg().withDescription("Dynamically added input file with next commands").create("dynamicinput");

		Options options = new Options();
		options.addOption(enableServerMode);
		options.addOption(inputName);
		options.addOption(outputName);
		options.addOption(dynamicInputName);

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

		Boolean serverMode = line.hasOption(enableServerMode.getOpt());
		String input = line.getOptionValue(inputName.getOpt());
		String output = line.getOptionValue(outputName.getOpt());
		String dynInput = line.getOptionValue(dynamicInputName.getOpt());

		if (!serverMode && (input == null || output == null))
		{
			printHelp(options);
			closeApp();
		}


		if (serverMode)
		{
			LoginHandler.getHandler().setRhManager(manager);
			// starting HTTP Server
			if (HTTPServer.getServer() == null)
				logger.info("Application stopped with error");

			// starting threads watcher
			final SessionWatcher watcher;
			if ((watcher = SessionWatcher.getWatcher()) != null)
				new Thread(watcher).start();
			else
				logger.info("Thread watcher is not running");

		}
		else
		{
			logger.info("Input file: '" + input + "'");
			logger.info("Output file: '" + output + "'");
			if (dynInput != null)
				logger.info("Dynamic input file: '" + dynInput + "'");

			ActionsLauncher launcher = processAllScriptsFromDirectory(input, output, null, manager);
			if (dynInput != null)
			{
				File dynFile = new File(dynInput);
				boolean firstWait = true;
				while (true)
				{
					if (dynFile.exists())
					{
						firstWait = true;
						processAllScriptsFromDirectory(dynInput, output, launcher, manager);

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
			if (manager != null)
				manager.close();

			logger.info("Application stopped");
		}
	}

	private static ActionsLauncher processAllScriptsFromDirectory(String input, String output,
																  ActionsLauncher launcher,
																  IRemoteHandManager manager)
	{
		File[] fileList = null;
		File inputFile = new File(input), 
				outputFile = new File(output);
		if (!inputFile.exists())
		{
			logger.error("Input file or directory '" + input + "' does not exist");
			closeApp();
		}

		if (inputFile.isDirectory())
		{
			fileList = inputFile.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					boolean isMatrix = file.isFile() && file.getName().endsWith(".csv");
					return isMatrix;
				}
			});
		}
		else
			fileList = new File[] { inputFile };

		for (File scriptFile : fileList)
		{
			try
			{
				ScriptCompiler compiler = manager.createScriptCompiler();
				final List<ScriptAction> actions = compiler.build(scriptFile);

				if (launcher == null)
					launcher = manager.createActionsLauncher(null);
				String result = launcher.runActions(actions);

				TextFileWriter.getInstance().setContent(result);
				TextFileWriter.getInstance().writeFile(outputFile);
			}
			catch (Exception ex)
			{
				logger.error(ex);
			}
		}
		return launcher;
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
}
