/******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.uiautomation;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.ScriptExecuteException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by alexey.karpukhin on 2/12/16.
 */
public class UIAAction extends Action {

	private String scriptName;
	private File matrixName;
	private File directory;

	public void init(String scriptName, File matrixName, File directory) {
		this.scriptName = scriptName;
		this.matrixName = matrixName;
		this.directory = directory;
	}

	@Override
	public String execute() throws ScriptExecuteException {
		try {
			final Process proc = new ProcessBuilder(
					new File(directory, scriptName).getAbsolutePath(),
					matrixName.getName()).start();

			Thread closeChildProcess = new Thread() {
				public void run() {
					proc.destroy();
				}
			};

			Runtime.getRuntime().addShutdownHook(closeChildProcess);

			InputStream os = proc.getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(os, writer);
			matrixName.deleteOnExit();
			System.out.println("Result: ");
			System.out.println(writer.toString());
			return writer.toString();
		} catch (IOException e) {
			throw new ScriptExecuteException("Exception during action execution. " + e.getMessage());
		}
	}
}
