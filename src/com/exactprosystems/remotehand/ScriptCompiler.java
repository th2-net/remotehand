////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems
//  Quality Assurance & Related Software Development for Innovative Trading Systems.
//  London Stock Exchange Group.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.remotehand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by alexey.karpukhin on 2/1/16.
 */
public abstract class ScriptCompiler {

	public final String LINE_SEPARATOR = "line.separator";

	public abstract List<Action> build(String scriptFile) throws ScriptCompileException;

	public List<Action> build(File scriptFile) throws IOException, ScriptCompileException
	{
		BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
		StringBuffer sb = new StringBuffer();
		try
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line).append(System.getProperty(LINE_SEPARATOR));
			}
		}
		finally
		{
			if (reader != null)
				reader.close();
		}

		String script = sb.toString();

		return build(script);
	}

}
