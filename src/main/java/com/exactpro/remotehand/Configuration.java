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

package com.exactpro.remotehand;

import com.exactpro.remotehand.screenwriter.DefaultScreenWriter;
import com.exactpro.remotehand.screenwriter.ScreenWriter;
import com.exactpro.remotehand.screenwriter.WebpScreenWriter;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static java.lang.String.format;

public abstract class Configuration
{
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	protected static volatile Configuration instance = null;
	protected volatile Properties properties;

	public final String PROPERTY_NOT_SET = "Property '%s' is not set. Using default value = '%s'";

	public final String CONFIG_FILE_NAME = "config.ini";
	public static final Path SCREENSHOTS_DIR_PATH = Paths.get("screenshots");
	public final int DEF_PORT = 8008;
	public final char DEF_DELIMITER = ',';
	public final char DEF_TEXT_QUALIFIER = '"';
	public final int DEF_SESSION_EXPIRE = 10; // 10 minutes
	public final int DEF_DRIVER_POOL_SIZE = 1;
	public final int DEF_SEND_KEYS_MAX_RETRIES = 3;
	public final float DEF_WEBP_QUALITY_FACTOR = 100f;
	public final String DEF_FILE_STORAGE = "generated/",
			DEF_HOST = "localhost",
			DEF_WEBP_LIBRARY_PATH = "libwebp.so";

	public final String PARAM_HOST = "Host",
			PARAM_PORT = "Port",
			PARAM_DELIMITER = "Delimiter",
			PARAM_TEXT_QUALIFIER = "TextQualifier",
			PARAM_SESSIONEXPIRE = "SessionExpire",
			PARAM_FILE_STORAGE = "DefaultFileStorage",
			PARAM_DRIVER_POOL_SIZE = "WebDriverPoolSize",
			PARAM_SEND_KEYS_MAX_RETRIES = "SendKeysMaxRetries",
			PARAM_WEBP_LIBRARY_PATH = "WebpLibPath",
			PARAM_WEBP_QUALITY_FACTOR = "WebpQualityFactor",
			PARAM_WEBP_LOSSLESS_COMPRESSION = "WebpLosslessCompression",
			PARAM_SCREENSHOT_ON_ERROR = "ScreenshotOnError";

	private volatile String host;
	private volatile int port;
	private volatile char scriptDelimiter;
	private volatile char scriptTextQualifier;
	private volatile int sessionExpire;
	private volatile int driverPoolSize;
	private volatile int sendKeysMaxRetries;
	private volatile File fileStorage;
	private volatile ScreenWriter<?> defaultScreenWriter;
	
	protected boolean acceptEnvVars;
	protected String webpLibraryPath;
	protected float webpQualityFactor;
	protected boolean useWebpImageEncoder;
	protected boolean losslessCompression;
	protected boolean screenshotOnError;

	protected Map<String, String> options;

	protected Configuration(CommandLine commandLine)
	{
		this(commandLine, Collections.emptyMap());
	}
	
	protected Configuration(CommandLine commandLine, Map<String, String> options)
	{
		instance = this;
		this.options = options;
		properties = new Properties(getDefaultProperties());

		String configFileName;
		if (commandLine != null) {
			configFileName = commandLine.getOptionValue(RemoteHandStarter.CONFIG_PARAM, CONFIG_FILE_NAME);
			logger.info(String.format("Using configuration file '%s'", configFileName));
			this.acceptEnvVars = commandLine.hasOption(RemoteHandStarter.ENV_VARS_PARAM);
			this.useWebpImageEncoder = commandLine.hasOption(RemoteHandStarter.USE_WEBP_IMAGE_ENCODER_PARAM);
			logger.info("Allowed configuration options from env vars {}", acceptEnvVars);
		} else {
			configFileName = CONFIG_FILE_NAME;
			logger.info("Used default config file");
			this.acceptEnvVars = false;
			this.useWebpImageEncoder = this.loadProperty(RemoteHandStarter.USE_WEBP_IMAGE_ENCODER_PARAM, Boolean.FALSE,
					Boolean::parseBoolean);
			logger.info("Env vars is not allowed");			
		}
		
		FileInputStream fs = null;
		try
		{
			fs = new FileInputStream(new File(configFileName));
			properties.load(fs);
		}
		catch (IOException e)
		{
			logger.warn(String.format("File '%s' is not found or has wrong format. Using default configuration.", configFileName));
		}
		finally
		{
			try
			{
				if (fs != null)
					fs.close();
			}
			catch (IOException e)
			{
				logger.warn(String.format("An error occurred while closing file '%s'.", configFileName), e);
			}
		}
		
		this.host = this.loadProperty(PARAM_HOST, DEF_HOST);
		this.port = this.loadProperty(PARAM_PORT, DEF_PORT, Integer::parseInt);
		this.scriptDelimiter = this.loadProperty(PARAM_DELIMITER, DEF_DELIMITER, s -> s.charAt(0));
		this.scriptTextQualifier = this.loadProperty(PARAM_TEXT_QUALIFIER, DEF_TEXT_QUALIFIER, s -> s.charAt(0));
		this.sessionExpire = this.loadProperty(PARAM_SESSIONEXPIRE, DEF_SESSION_EXPIRE, Integer::parseInt);
		this.fileStorage = new File(this.loadProperty(PARAM_FILE_STORAGE, DEF_FILE_STORAGE));
		this.driverPoolSize = this.loadProperty(PARAM_DRIVER_POOL_SIZE, DEF_DRIVER_POOL_SIZE, Integer::parseInt);
		this.sendKeysMaxRetries = this.loadProperty(PARAM_SEND_KEYS_MAX_RETRIES, DEF_SEND_KEYS_MAX_RETRIES, Integer::parseInt);
		this.webpLibraryPath = this.loadProperty(PARAM_WEBP_LIBRARY_PATH, DEF_WEBP_LIBRARY_PATH);
		this.webpQualityFactor = this.loadProperty(PARAM_WEBP_QUALITY_FACTOR, DEF_WEBP_QUALITY_FACTOR, Float::parseFloat);
		this.losslessCompression = this.loadProperty(PARAM_WEBP_LOSSLESS_COMPRESSION, Boolean.TRUE, Boolean::parseBoolean);
		this.screenshotOnError = this.loadProperty(PARAM_SCREENSHOT_ON_ERROR, Boolean.TRUE, Boolean::parseBoolean);
		this.defaultScreenWriter = createDefaultScreenWriter();
	}

	protected ScreenWriter<?> createDefaultScreenWriter() {
		if (useWebpImageEncoder) {
			WebpScreenWriter webpScreenWriter = new WebpScreenWriter(webpQualityFactor, losslessCompression);
			logger.info("Enabled webp image encoder. Encoder version: {}", Integer.toHexString(webpScreenWriter.getEncoderVersion()));
			return webpScreenWriter;
		} else {
			return new DefaultScreenWriter();
		}
	}

	protected Properties getDefaultProperties()
	{
		Properties defProperties = new Properties();
		defProperties.setProperty(PARAM_PORT, String.valueOf(DEF_PORT));
		defProperties.setProperty(PARAM_DELIMITER, String.valueOf(DEF_DELIMITER));
		defProperties.setProperty(PARAM_TEXT_QUALIFIER, String.valueOf(DEF_TEXT_QUALIFIER));
		defProperties.setProperty(PARAM_SESSIONEXPIRE, String.valueOf(DEF_SESSION_EXPIRE));
		return defProperties;
	}

	protected String getENVName(String name) {
		final StringBuilder sb = new StringBuilder();
		sb.append("RH");
		name.chars().forEach(value -> {
			if (Character.isUpperCase(value)) {
				sb.append('_').append((char) value);
			} else {
				sb.append(Character.toUpperCase((char) value));
			}
		});
		return sb.toString();
	}

	protected <T> T loadProperty(String name, T defaultValue, Function<String, T> function)
	{
		return loadProperty(name, defaultValue, function, true);
	}

	protected <T> T loadProperty(String name, T defaultValue, Function<String, T> function, boolean writeToLog)
	{
		String stringProp = this.loadProperty(name, "", false);
		if (stringProp.isEmpty()) {
			if (writeToLog)
				logger.warn(format(PROPERTY_NOT_SET, name, defaultValue));
			return defaultValue;
		}
		try {
			T apply = function.apply(stringProp);
			if (writeToLog) {
				logger.info("{} = {}", name, defaultValue);
			}
			return apply;
		} catch (Exception e) {
			if (writeToLog)
				logger.warn("Cannot parse property" + name + ". Using default value: " + defaultValue.toString(), e);
			return defaultValue;
		}
	}

	protected String loadProperty(String name, String defaultValue)
	{
		return loadProperty(name, defaultValue, true);
	}
	
	protected String loadProperty(String name, String defaultValue, boolean writeToLog)
	{
		String property = null;
		boolean notSet = true;
		if (acceptEnvVars) {
			String envVarName = this.getENVName(name);
			property = System.getenv(envVarName);
			notSet = property == null || property.isEmpty();
			if (notSet && writeToLog) {
				logger.warn("Env property is not set {}", name);
			}
		}
		if (notSet && !options.isEmpty()) {
			property = options.get(name);
			notSet = property == null || property.isEmpty();
			if (notSet && writeToLog) {
				logger.warn("Property from options is not set {}", name);
			}
		}
		if (notSet) {				
			property = properties.getProperty(name, "");
		}
		if (property.isEmpty() && defaultValue != null)
		{
			if (writeToLog)
				logger.warn(format(PROPERTY_NOT_SET, name, defaultValue));
			property = defaultValue;
		}
		else if (writeToLog)
			logger.info("{} = {}", name, property);
		return property;
	}

	public static Configuration getInstance()
	{
		return instance;
	}


	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	public char getDelimiter()
	{
		return scriptDelimiter;
	}
	
	public char getTextQualifier()
	{
		return scriptTextQualifier;
	}
	
	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public int getSessionExpire()
	{
		return sessionExpire;
	}

	public File getFileStorage() {
		return fileStorage;
	}

	public int getDriverPoolSize()
	{
		return driverPoolSize;
	}

	public int getSendKeysMaxRetries()
	{
		return sendKeysMaxRetries;
	}

	public String getWebpLibraryPath() {
		return webpLibraryPath;
	}

	public float getWebpQualityFactor() {
		return webpQualityFactor;
	}

	public ScreenWriter<?> getDefaultScreenWriter() {
		return defaultScreenWriter;
	}

	public boolean isScreenshotOnError() {
		return screenshotOnError;
	}
}
