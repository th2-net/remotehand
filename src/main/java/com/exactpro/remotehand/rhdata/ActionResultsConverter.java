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

package com.exactpro.remotehand.rhdata;

import com.exactpro.remotehand.ActionResult;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.List;
import java.util.stream.Collectors;

public class ActionResultsConverter extends StdConverter<List<ActionResult>, List<String>> {

	@Override
	public List<String> convert(List<ActionResult> value) {
		return value.stream().filter(ActionResult::hasData).map(v -> {
			if (v.getId() == null)
				return v.getData();
			else
				return v.getId() + "=" + v.getData();
		}).collect(Collectors.toList());
	}
}
