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

package com.exactprosystems.remotehand.web.actions;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.RhUtils;
import com.exactprosystems.remotehand.ScriptExecuteException;
import com.exactprosystems.remotehand.web.WebAction;
import com.exactprosystems.remotehand.web.WebConfiguration;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by alexey.karpukhin on 9/18/17.
 */
public class DownloadFile extends WebAction {
	
	public static final String
			FILES_KEY = "DownloadDirFiles",
			FILE_NAME = "filename",
			LOCAL_PATH = "localpath";

	private static final Logger logger = LoggerFactory.getLogger(DownloadFile.class);

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
		File downloadDir = this.context.getDownloadDir();
		if ("snapshot".equalsIgnoreCase(type)) {
			String[] fileList = downloadDir.list();
			context.getContextData().put(FILES_KEY, fileList);
			getLogger().debug("Snapshot file list: " + Arrays.toString(fileList));
			return null;
		} else if ("download".equalsIgnoreCase(type)) {
			File file;
			boolean done;
			boolean isTemp;
			try {
				String[] list = (String[]) context.getContextData().remove(FILES_KEY);
				if (list == null)
					throw new ScriptExecuteException("No snapshot. Execute with type 'snapshot' first.");
				int iteration = 0;
				do {
					iteration++;
					file = waitForFile(list, downloadDir, 1000);
					if (file == null && isElementMandatory())
						throw new ScriptExecuteException("No new files in download dir");
					else if(file == null)
						return "";

					getLogger().debug("Selected file: " + file.getName());
					isTemp = this.isTempFile(file);
					String timeoutParam = params.get(PARAM_WAIT);
					int timeout = Integer.parseInt(isEmpty(timeoutParam) ? "10" : timeoutParam) * 1000 - 1000;
					boolean fullSearch = iteration == 1 || isTemp;
					done = this.checkSize(file, fullSearch ? timeout : 50, fullSearch ? 10 : 1);
				} while (iteration < 10 && !done && isTemp && !file.exists());
				if (!done) {
					throw new ScriptExecuteException("File wasn't downloaded.");
				}
			} catch (InterruptedException e) {
				throw new ScriptExecuteException("Interrupted.", e);
			}

			file = renameFile(file, params);

			if (getParams().containsKey(LOCAL_PATH) && RhUtils.YES.contains(getParams().get(LOCAL_PATH)))
				return "FilePath=" + file.getAbsolutePath();

			return "Downloaded_file=" + createUrl(file);
		}
		
		throw new ScriptExecuteException("Unknown actionType: " + type);
	}

	private File renameFile(File file, Map<String,String> params) throws ScriptExecuteException
	{
		String newFileName = params.get(FILE_NAME);
		if(isEmpty(newFileName))
			return file;

		String extension = FilenameUtils.getExtension(newFileName);
		if(isEmpty(extension))
			newFileName = newFileName + "." + FilenameUtils.getExtension(file.getName());

		try
		{
			Path newFile = Paths.get(file.getParent(), newFileName);
			Files.createDirectories(newFile.getParent());
			logger.trace("Old file name: {}", file.getAbsoluteFile());
			logger.trace("New file name: {}", newFile.toAbsolutePath());
			return Files.move(file.toPath(), newFile).toFile();
		} catch (IOException e)
		{
			throw new ScriptExecuteException("Cannot rename file to " + newFileName, e);
		}
	}

	private String createUrl(File file) throws ScriptExecuteException
	{
		File configDownloadDir = WebConfiguration.getInstance().getDownloadsDir();
		String filePath = file.getPath().substring(configDownloadDir.getPath().length());
		try
		{
			return URLEncoder.encode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			throw new ScriptExecuteException("Cannot create URL for path: " + filePath, e);
		}
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
				if (!ArrayUtils.contains(old, s)) {
					return new File(downloadDir, s);
				}
			}
		}
		return null;
	}
	
	protected boolean isTempFile(File file) {
		String filename = file.getName();
		return filename.endsWith(".crdownload") || filename.endsWith(".tmp");
	}

	private boolean checkSize(File file, long timeout, int iteration) throws InterruptedException {
		long size = file.length();
		long endTime = System.currentTimeMillis() + timeout;
		int count = 0;
		while (count < iteration && System.currentTimeMillis() <= endTime) {
			if (file.length() == size) {
				count++;
			} else {
				size = file.length();
				count = 0;
			}
			if (size == 0 && !file.exists())
				return false;
			if (count < iteration)
				Thread.sleep(250);
		}
		return (count == iteration);
		
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
