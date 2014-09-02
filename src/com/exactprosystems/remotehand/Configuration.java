package com.exactprosystems.remotehand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
	private static final Logger logger = Logger.getLogger();
	public static volatile Configuration instance;
	private volatile Properties properties;

	public static final String PROPERTY_NOT_SET = "Property '%s' is not set. Using default value = '%s'";

	public static final String CONFIG_FILE_NAME = "config.ini";
	public static final int DEF_SRV_PORT = 8000;
	public static final char DEF_DELIMITER = ',';
	public static final int DEF_SESSION_EXPIRY_TIME = 1000 * 60 * 60; // 1 hour
	public static final Browser DEF_BROWSER = Browser.FIREFOX;
	public static final String DEF_IEDRIVER_PATH = "IEDriverServer.exe", 
			DEF_CHROMEDRIVER_PATH = "chromedriver.exe", 
			DEF_PROXY = "";

	public static final String port = "Port", 
			delimiter = "Delimiter", 
			sessionExpiryTime = "SessionExpiryTime", 
			browser = "Browser", 
			ieDriverPath = "IEDriverPath",
			chromeDriverPath = "ChromeDriverPath", 
			httpProxy = "HttpProxy", 
			sslProxy = "SslProxy", 
			ftpProxy = "FtpProxy", 
			socksProxy = "SocksProxy", 
			noProxy = "NoProxy";

	private volatile int httpServerPort;
	private volatile char scriptDelimiter;
	private volatile int sessionExpiryTimeMs;
	private volatile Browser browserToUse;
	private volatile String ieDriverFileName, chromeDriverFileName, httpProxySetting, sslProxySetting, ftpProxySetting, socksProxySetting, noProxySetting;

	private Configuration()
	{
		Properties defProperties = new Properties();

		defProperties.setProperty(port, String.valueOf(DEF_SRV_PORT));
		defProperties.setProperty(delimiter, String.valueOf(DEF_DELIMITER));
		defProperties.setProperty(sessionExpiryTime, String.valueOf(DEF_SESSION_EXPIRY_TIME));
		defProperties.setProperty(browser, DEF_BROWSER.getLabel());
		defProperties.setProperty(ieDriverPath, DEF_IEDRIVER_PATH);
		defProperties.setProperty(httpProxy, DEF_PROXY);
		defProperties.setProperty(sslProxy, DEF_PROXY);
		defProperties.setProperty(ftpProxy, DEF_PROXY);
		defProperties.setProperty(socksProxy, DEF_PROXY);
		defProperties.setProperty(noProxy, DEF_PROXY);

		properties = new Properties(defProperties);

		try
		{
			FileInputStream fs = new FileInputStream(new File(CONFIG_FILE_NAME));
			properties.load(fs);
			fs.close();
		}
		catch (IOException e)
		{
			logger.warn(String.format("File '%s' is not found or has wrong format. Using default cofiguration.", CONFIG_FILE_NAME));
		}

		try
		{
			httpServerPort = Integer.parseInt(properties.getProperty(port));
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", port, DEF_SRV_PORT));
			httpServerPort = DEF_SRV_PORT;
		}

		final String delim = getProperty(delimiter);
		if (!delim.isEmpty())
			scriptDelimiter = delim.charAt(0);
		else
		{
			logger.warn(String.format(PROPERTY_NOT_SET, delimiter, String.valueOf(DEF_DELIMITER)));
			scriptDelimiter = DEF_DELIMITER;
		}

		try
		{
			sessionExpiryTimeMs = Integer.parseInt(properties.getProperty(sessionExpiryTime));
		}
		catch (Exception ex)
		{
			logger.warn(String.format("Error while reading property '%s'. Using default value = <%s>", sessionExpiryTime, DEF_SESSION_EXPIRY_TIME));
			httpServerPort = DEF_SRV_PORT;
		}

		browserToUse = Browser.valueByLabel(properties.getProperty(browser));
		if (browserToUse == Browser.INVALID)
		{
			logger.warn(String.format("Property '%s' is not set or has invalid value. Using default value = '%s'", browser, DEF_BROWSER.getLabel()));
			browserToUse = DEF_BROWSER;
		}

		ieDriverFileName = properties.getProperty(ieDriverPath);
		if ((ieDriverFileName == null) || (ieDriverFileName.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, ieDriverPath, DEF_IEDRIVER_PATH));
			ieDriverFileName = DEF_IEDRIVER_PATH;
		}

		chromeDriverFileName = properties.getProperty(chromeDriverPath);
		if ((chromeDriverFileName == null) || (chromeDriverFileName.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, chromeDriverPath, DEF_CHROMEDRIVER_PATH));
			chromeDriverFileName = DEF_CHROMEDRIVER_PATH;
		}

		httpProxySetting = properties.getProperty(httpProxy);
		if ((httpProxySetting == null) || (httpProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, httpProxy, DEF_PROXY));
			httpProxySetting = DEF_PROXY;
		}

		sslProxySetting = properties.getProperty(sslProxy);
		if ((sslProxySetting == null) || (sslProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, sslProxy, DEF_PROXY));
			sslProxySetting = DEF_PROXY;
		}

		ftpProxySetting = properties.getProperty(ftpProxy);
		if ((ftpProxySetting == null) || (ftpProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, ftpProxy, DEF_PROXY));
			ftpProxySetting = DEF_PROXY;
		}

		socksProxySetting = properties.getProperty(socksProxy);
		if ((socksProxySetting == null) || (socksProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, socksProxy, DEF_PROXY));
			socksProxySetting = DEF_PROXY;
		}

		noProxySetting = properties.getProperty(noProxy);
		if ((noProxySetting == null) || (noProxySetting.isEmpty()))
		{
			logger.warn(String.format(PROPERTY_NOT_SET, noProxy, DEF_PROXY));
			noProxySetting = DEF_PROXY;
		}
	}

	public static Configuration getInstance()
	{
		Configuration localInstance = instance;
		if (localInstance == null)
		{
			synchronized (Configuration.class)
			{
				localInstance = instance;
				if (localInstance == null)
					instance = localInstance = new Configuration();
			}
		}
		return localInstance;
	}

	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	public char getDelimiter()
	{
		return scriptDelimiter;
	}

	public int getHttpServerPort()
	{
		return httpServerPort;
	}

	public int getSessionExpiryTimeMs()
	{
		return sessionExpiryTimeMs;
	}

	public Browser getBrowserToUse()
	{
		return browserToUse;
	}

	public String getIeDriverFileName()
	{
		return ieDriverFileName;
	}

	public String getChromeDriverFileName()
	{
		return chromeDriverFileName;
	}

	public String getHttpProxySetting()
	{
		return httpProxySetting;
	}

	public String getSslProxySetting()
	{
		return sslProxySetting;
	}

	public String getFtpProxySetting()
	{
		return ftpProxySetting;
	}

	public String getSocksProxySetting()
	{
		return socksProxySetting;
	}

	public String getNoProxySetting()
	{
		return noProxySetting;
	}
}