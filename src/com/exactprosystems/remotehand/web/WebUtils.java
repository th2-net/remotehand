////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
	
}
