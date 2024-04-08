/*
 * Copyright 2020-2024 Exactpro (Exactpro Systems Limited)
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

import com.csvreader.CsvReader;
import com.exactpro.remotehand.sessions.SessionContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Files;

public abstract class ScriptCompiler {
	protected final String LINE_SEPARATOR = System.lineSeparator();

	public abstract List<Action> build(String scriptFile, Map<String, String> inputParams, SessionContext context) throws ScriptCompileException;

	public List<Action> build(File scriptFile, File inputParams, SessionContext context) throws IOException, ScriptCompileException {
		String script;
		try (Stream<String> linesStream = Files.lines(scriptFile.toPath())) {
			script = linesStream.collect(Collectors.joining(LINE_SEPARATOR));
		}

		Map<String, String> params = inputParams != null && inputParams.exists()
				? readParamsFromFile(inputParams)
				: null;

		return build(script, params, context);
	}

	public Map<String, String> readParamsFromFile(File inputParams) throws IOException {
		Map<String, String> params = new HashMap<>();
		CsvReader csvReader = null;
		try {
			csvReader = new CsvReader(new FileReader(inputParams));
			csvReader.setDelimiter(',');
			csvReader.setTextQualifier('"');
			while (csvReader.readRecord()) {
				String[] param = csvReader.getValues();
				params.put(param[0], param[1]);
			}
			return params;
		} finally {
			if (csvReader != null)
				csvReader.close();
		}
	}
}