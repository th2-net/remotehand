/******************************************************************************
 * Copyright (c) 2009-2019, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.web;

import com.exactprosystems.remotehand.Configuration;

import java.io.File;

/**
 * Created by alexey.karpukhin on 9/18/17.
 */
public class WebUtils {
	
	public static File createDownloadDirectory() {
		WebConfiguration configuration = (WebConfiguration) Configuration.getInstance();
		File downloadsDir = configuration.getDownloadsDir();
		File newFileDir = new File(downloadsDir, String.valueOf(System.currentTimeMillis()));
		newFileDir.mkdirs();
		return newFileDir;
	}

	public static void deleteDownloadDirectory(File downloadDir)
	{
		File[] tmp;
		if (downloadDir != null && (tmp = downloadDir.listFiles()) != null && tmp.length == 0) {
			downloadDir.delete();
		}
	}
	
}
