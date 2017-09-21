////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.web.actions;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.jetty.util.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by alexey.karpukhin on 9/18/17.
 */
public class DownloadFile extends WebAction {
	
	public static final String FILES_KEY = "DownloadDirFiles";

	private static final Logger logger = Logger.getLogger(DownloadFile.class);

	@Override
	public boolean isNeedLocator() {
		return false;
	}

	@Override
	public boolean isCanWait() {
		return false;
	}

	@Override
	public String run(WebDriver webDriver, By webLocator, Map<String, String> params) throws ScriptExecuteException {
		String type = params.get("actiontype");
		File downloadDir = this.context.getDonwloadDir();
		if ("snapshot".equalsIgnoreCase(type)) {
			String[] fileList = downloadDir.list();
			context.getContextData().put(FILES_KEY, fileList);
			getLogger().debug("Snapshot file list: " + Arrays.toString(fileList));
			return null;
		} else if ("download".equalsIgnoreCase(type)) {
			File file;
			try {
				String[] list = (String[]) context.getContextData().get(FILES_KEY);
				if (list == null)
					throw new ScriptExecuteException("Doesn't have a snapshot. Execute with type 'snapshot' first.");
				file = waitForFile(list, downloadDir, 1000);
				if (file == null)
					throw new ScriptExecuteException("No new files in download dir");
				getLogger().debug("Selected file: " + file.getName());
				boolean isTemp = this.isTempFile(file);
				String timeoutParam = params.get(PARAM_WAIT);
				Integer timout = Integer.parseInt(StringUtils.isEmpty(timeoutParam) ? "10" : timeoutParam) * 1000 - 1000;
				boolean done = this.checkSize(file, timout);
				if (!done) {
					if (isTemp && !file.exists())
						file = waitForFile(list, downloadDir, 50);
					else
						throw new ScriptExecuteException("File wasn't downloaded.");
				}
			} catch (InterruptedException e) {
				throw new ScriptExecuteException("Interrupted.", e);
			}
			if (file == null)
				throw new ScriptExecuteException("No new files in download dir");
			File configDownloadDir = ((WebConfiguration) Configuration.getInstance()).getDownloadsDir();
			return "Downloaded_file=" + file.getPath().substring(configDownloadDir.getPath().length());
		}
		
		throw new ScriptExecuteException("Unknown actionType: " + type);
	}
	
	@SuppressWarnings("ConstantConditions")
	private File waitForFile(String[] old, File downloadDir, long timeout) throws InterruptedException {
		long endTime = System.currentTimeMillis() + timeout;
		String[] tmp;
		while (old.length == (tmp = downloadDir.list()).length && System.currentTimeMillis() <= endTime)
			Thread.sleep(100);
		if (old.length != tmp.length) {
			logger.debug("Found files: " + Arrays.toString(tmp));
			for (String s : tmp) {
				if (Arrays.binarySearch(old, s) == -1) {
					return new File(downloadDir, s);
				}
			}
		}
		return null;
	}
	
	protected boolean isTempFile(File file) {
		return file.getName().endsWith(".crdownload");
	}

	private boolean checkSize(File file, long timeout) throws InterruptedException {
		long size = file.length();
		long endTime = System.currentTimeMillis() + timeout;
		int count = 0;
		while (count < 10 && System.currentTimeMillis() <= endTime) {
			if (file.length() == size) {
				count++;
			} else {
				size = file.length();
				count = 0;
			}
			if (size == 0 && !file.exists())
				return false;
			if (count < 10)
				Thread.sleep(250);
		}
		return (count == 10);
		
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
