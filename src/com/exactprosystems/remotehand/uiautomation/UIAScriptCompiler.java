////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2017, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary 
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand.uiautomation;

import com.exactprosystems.remotehand.Action;
import com.exactprosystems.remotehand.Configuration;
import com.exactprosystems.remotehand.ScriptCompileException;
import com.exactprosystems.remotehand.ScriptCompiler;
import com.exactprosystems.remotehand.http.SessionContext;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;

/**
 * Created by alexey.karpukhin on 2/11/16.
 */
public class UIAScriptCompiler extends ScriptCompiler {

	private static final Logger logger = Logger.getLogger(UIAScriptCompiler.class);

	public static final String COMMENT_INDICATOR = "//";
	public static final String oldLineSeporator = "&#13";

	private File tempDir;

	@Override
	public List<Action> build(String script, SessionContext context) throws ScriptCompileException {
		logger.info("Compiling script...");

		tempDir = ((UIAConfiguration)Configuration.getInstance()).getScriptDir();

		String scriptName = script.substring(0, script.indexOf(oldLineSeporator));

		script = script.substring(script.indexOf(oldLineSeporator)+oldLineSeporator.length());

		script = script.replace(oldLineSeporator, System.getProperty(LINE_SEPARATOR));

		File f = new File(tempDir, "matrix_" + System.currentTimeMillis() + ".csv" );
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(script);
		} catch (Exception e) {
			throw new ScriptCompileException("Cannot write matrix file" , e);
		} finally {
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
			} catch (Exception e) {} //ignore
		}

		UIAAction action = new UIAAction();
		action.init(scriptName, f, tempDir);

		return Collections.singletonList((Action) action);
//		return null;
	}
}
